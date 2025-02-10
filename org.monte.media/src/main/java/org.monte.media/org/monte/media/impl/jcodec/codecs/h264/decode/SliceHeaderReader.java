package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnit;
import org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.PictureParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.PredictionWeightTable;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarking;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarking.InstrType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarking.Instruction;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarkingIDR;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.IntArrayList;
import org.monte.media.impl.jcodec.common.io.BitReader;

import java.util.ArrayList;

import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readBool;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readSE;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readU;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readUEtrace;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet.getPicHeightInMbs;
import static org.monte.media.impl.jcodec.common.model.ColorSpace.MONO;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * <p>
 * Reads header of the coded slice
 *
 * @author The JCodec project
 */
public class SliceHeaderReader {
    private SliceHeaderReader() {
    }

    public static SliceHeader readPart1(BitReader _in) {

        SliceHeader sh = new SliceHeader();
        sh.firstMbInSlice = readUEtrace(_in, "SH: first_mb_in_slice");
        int shType = readUEtrace(_in, "SH: slice_type");
        sh.sliceType = SliceType.fromValue(shType % 5);
        sh.sliceTypeRestr = (shType / 5) > 0;

        sh.picParameterSetId = readUEtrace(_in, "SH: pic_parameter_set_id");

        return sh;
    }

    public static SliceHeader readPart2(SliceHeader sh, NALUnit nalUnit, SeqParameterSet sps, PictureParameterSet pps,
                                        BitReader _in) {
        sh.pps = pps;
        sh.sps = sps;

        sh.frameNum = readU(_in, sps.log2MaxFrameNumMinus4 + 4, "SH: frame_num");
        if (!sps.frameMbsOnlyFlag) {
            sh.fieldPicFlag = readBool(_in, "SH: field_pic_flag");
            if (sh.fieldPicFlag) {
                sh.bottomFieldFlag = readBool(_in, "SH: bottom_field_flag");
            }
        }
        if (nalUnit.type == NALUnitType.IDR_SLICE) {
            sh.idrPicId = readUEtrace(_in, "SH: idr_pic_id");
        }
        if (sps.picOrderCntType == 0) {
            sh.picOrderCntLsb = readU(_in, sps.log2MaxPicOrderCntLsbMinus4 + 4, "SH: pic_order_cnt_lsb");
            if (pps.picOrderPresentFlag && !sps.fieldPicFlag) {
                sh.deltaPicOrderCntBottom = readSE(_in, "SH: delta_pic_order_cnt_bottom");
            }
        }
        sh.deltaPicOrderCnt = new int[2];
        if (sps.picOrderCntType == 1 && !sps.deltaPicOrderAlwaysZeroFlag) {
            sh.deltaPicOrderCnt[0] = readSE(_in, "SH: delta_pic_order_cnt[0]");
            if (pps.picOrderPresentFlag && !sps.fieldPicFlag)
                sh.deltaPicOrderCnt[1] = readSE(_in, "SH: delta_pic_order_cnt[1]");
        }
        if (pps.redundantPicCntPresentFlag) {
            sh.redundantPicCnt = readUEtrace(_in, "SH: redundant_pic_cnt");
        }
        if (sh.sliceType == SliceType.B) {
            sh.directSpatialMvPredFlag = readBool(_in, "SH: direct_spatial_mv_pred_flag");
        }
        if (sh.sliceType == SliceType.P || sh.sliceType == SliceType.SP || sh.sliceType == SliceType.B) {
            sh.numRefIdxActiveOverrideFlag = readBool(_in, "SH: num_ref_idx_active_override_flag");
            if (sh.numRefIdxActiveOverrideFlag) {
                sh.numRefIdxActiveMinus1[0] = readUEtrace(_in, "SH: num_ref_idx_l0_active_minus1");
                if (sh.sliceType == SliceType.B) {
                    sh.numRefIdxActiveMinus1[1] = readUEtrace(_in, "SH: num_ref_idx_l1_active_minus1");
                }
            }
        }
        readRefPicListReordering(sh, _in);
        if ((pps.weightedPredFlag && (sh.sliceType == SliceType.P || sh.sliceType == SliceType.SP))
                || (pps.weightedBipredIdc == 1 && sh.sliceType == SliceType.B))
            readPredWeightTable(sps, pps, sh, _in);
        if (nalUnit.nal_ref_idc != 0)
            readDecoderPicMarking(nalUnit, sh, _in);
        if (pps.entropyCodingModeFlag && sh.sliceType.isInter()) {
            sh.cabacInitIdc = readUEtrace(_in, "SH: cabac_init_idc");
        }
        sh.sliceQpDelta = readSE(_in, "SH: slice_qp_delta");
        if (sh.sliceType == SliceType.SP || sh.sliceType == SliceType.SI) {
            if (sh.sliceType == SliceType.SP) {
                sh.spForSwitchFlag = readBool(_in, "SH: sp_for_switch_flag");
            }
            sh.sliceQsDelta = readSE(_in, "SH: slice_qs_delta");
        }
        if (pps.deblockingFilterControlPresentFlag) {
            sh.disableDeblockingFilterIdc = readUEtrace(_in, "SH: disable_deblocking_filter_idc");
            if (sh.disableDeblockingFilterIdc != 1) {
                sh.sliceAlphaC0OffsetDiv2 = readSE(_in, "SH: slice_alpha_c0_offset_div2");
                sh.sliceBetaOffsetDiv2 = readSE(_in, "SH: slice_beta_offset_div2");
            }
        }
        if (pps.numSliceGroupsMinus1 > 0 && pps.sliceGroupMapType >= 3 && pps.sliceGroupMapType <= 5) {
            int len = getPicHeightInMbs(sps) * (sps.picWidthInMbsMinus1 + 1)
                    / (pps.sliceGroupChangeRateMinus1 + 1);
            if ((getPicHeightInMbs(sps) * (sps.picWidthInMbsMinus1 + 1))
                    % (pps.sliceGroupChangeRateMinus1 + 1) > 0)
                len += 1;

            len = CeilLog2(len + 1);
            sh.sliceGroupChangeCycle = readU(_in, len, "SH: slice_group_change_cycle");
        }

        return sh;
    }

    private static int CeilLog2(int uiVal) {
        int uiTmp = uiVal - 1;
        int uiRet = 0;

        while (uiTmp != 0) {
            uiTmp >>= 1;
            uiRet++;
        }
        return uiRet;
    }

    // static int i = 0;

    private static void readDecoderPicMarking(NALUnit nalUnit, SliceHeader sh, BitReader _in) {
        if (nalUnit.type == NALUnitType.IDR_SLICE) {
            boolean noOutputOfPriorPicsFlag = readBool(_in, "SH: no_output_of_prior_pics_flag");
            boolean longTermReferenceFlag = readBool(_in, "SH: long_term_reference_flag");
            sh.refPicMarkingIDR = new RefPicMarkingIDR(noOutputOfPriorPicsFlag, longTermReferenceFlag);
        } else {
            boolean adaptiveRefPicMarkingModeFlag = readBool(_in, "SH: adaptive_ref_pic_marking_mode_flag");
            if (adaptiveRefPicMarkingModeFlag) {
                ArrayList<Instruction> mmops = new ArrayList<Instruction>();
                int memoryManagementControlOperation;
                do {
                    memoryManagementControlOperation = readUEtrace(_in, "SH: memory_management_control_operation");

                    Instruction instr = null;

                    switch (memoryManagementControlOperation) {
                        case 1:
                            instr = new RefPicMarking.Instruction(InstrType.REMOVE_SHORT, readUEtrace(_in,
                                    "SH: difference_of_pic_nums_minus1") + 1, 0);
                            break;
                        case 2:
                            instr = new RefPicMarking.Instruction(InstrType.REMOVE_LONG,
                                    readUEtrace(_in, "SH: long_term_pic_num"), 0);
                            break;
                        case 3:
                            instr = new RefPicMarking.Instruction(InstrType.CONVERT_INTO_LONG, readUEtrace(_in,
                                    "SH: difference_of_pic_nums_minus1") + 1, readUEtrace(_in, "SH: long_term_frame_idx"));
                            break;
                        case 4:
                            instr = new RefPicMarking.Instruction(InstrType.TRUNK_LONG, readUEtrace(_in,
                                    "SH: max_long_term_frame_idx_plus1") - 1, 0);
                            break;
                        case 5:
                            instr = new RefPicMarking.Instruction(InstrType.CLEAR, 0, 0);
                            break;
                        case 6:
                            instr = new RefPicMarking.Instruction(InstrType.MARK_LONG,
                                    readUEtrace(_in, "SH: long_term_frame_idx"), 0);
                            break;
                    }
                    if (instr != null)
                        mmops.add(instr);
                } while (memoryManagementControlOperation != 0);
                sh.refPicMarkingNonIDR = new RefPicMarking(mmops.toArray(new Instruction[]{}));
            }
        }
    }

    private static void readPredWeightTable(SeqParameterSet sps, PictureParameterSet pps, SliceHeader sh, BitReader _in) {
        sh.predWeightTable = new PredictionWeightTable();
        int[] numRefsMinus1 = sh.numRefIdxActiveOverrideFlag ? sh.numRefIdxActiveMinus1
                : pps.numRefIdxActiveMinus1;
        int[] nr = new int[]{numRefsMinus1[0] + 1, numRefsMinus1[1] + 1};

        sh.predWeightTable.lumaLog2WeightDenom = readUEtrace(_in, "SH: luma_log2_weight_denom");
        if (sps.chromaFormatIdc != MONO) {
            sh.predWeightTable.chromaLog2WeightDenom = readUEtrace(_in, "SH: chroma_log2_weight_denom");
        }
        int defaultLW = 1 << sh.predWeightTable.lumaLog2WeightDenom;
        int defaultCW = 1 << sh.predWeightTable.chromaLog2WeightDenom;

        for (int list = 0; list < 2; list++) {
            sh.predWeightTable.lumaWeight[list] = new int[nr[list]];
            sh.predWeightTable.lumaOffset[list] = new int[nr[list]];
            sh.predWeightTable.chromaWeight[list] = new int[2][nr[list]];
            sh.predWeightTable.chromaOffset[list] = new int[2][nr[list]];
            for (int i = 0; i < nr[list]; i++) {
                sh.predWeightTable.lumaWeight[list][i] = defaultLW;
                sh.predWeightTable.lumaOffset[list][i] = 0;
                sh.predWeightTable.chromaWeight[list][0][i] = defaultCW;
                sh.predWeightTable.chromaOffset[list][0][i] = 0;
                sh.predWeightTable.chromaWeight[list][1][i] = defaultCW;
                sh.predWeightTable.chromaOffset[list][1][i] = 0;
            }
        }

        readWeightOffset(sps, pps, sh, _in, nr, 0);
        if (sh.sliceType == SliceType.B) {
            readWeightOffset(sps, pps, sh, _in, nr, 1);
        }
    }

    private static void readWeightOffset(SeqParameterSet sps, PictureParameterSet pps, SliceHeader sh, BitReader _in,
                                         int[] numRefs, int list) {

        for (int i = 0; i < numRefs[list]; i++) {
            boolean lumaWeightL0Flag = readBool(_in, "SH: luma_weight_l0_flag");
            if (lumaWeightL0Flag) {
                sh.predWeightTable.lumaWeight[list][i] = readSE(_in, "SH: weight");
                sh.predWeightTable.lumaOffset[list][i] = readSE(_in, "SH: offset");
            }
            if (sps.chromaFormatIdc != MONO) {
                boolean chromaWeightL0Flag = readBool(_in, "SH: chroma_weight_l0_flag");
                if (chromaWeightL0Flag) {
                    sh.predWeightTable.chromaWeight[list][0][i] = readSE(_in, "SH: weight");
                    sh.predWeightTable.chromaOffset[list][0][i] = readSE(_in, "SH: offset");
                    sh.predWeightTable.chromaWeight[list][1][i] = readSE(_in, "SH: weight");
                    sh.predWeightTable.chromaOffset[list][1][i] = readSE(_in, "SH: offset");
                }
            }
        }
    }

    private static void readRefPicListReordering(SliceHeader sh, BitReader _in) {
        sh.refPicReordering = new int[2][][];
        // System.out.println(i++);
        if (sh.sliceType.isInter()) {
            boolean refPicListReorderingFlagL0 = readBool(_in, "SH: ref_pic_list_reordering_flag_l0");
            if (refPicListReorderingFlagL0) {
                sh.refPicReordering[0] = readReorderingEntries(_in);
            }
        }
        if (sh.sliceType == SliceType.B) {
            boolean refPicListReorderingFlagL1 = readBool(_in, "SH: ref_pic_list_reordering_flag_l1");
            if (refPicListReorderingFlagL1) {
                sh.refPicReordering[1] = readReorderingEntries(_in);
            }
        }
    }

    private static int[][] readReorderingEntries(BitReader _in) {
        IntArrayList ops = IntArrayList.createIntArrayList();
        IntArrayList args = IntArrayList.createIntArrayList();
        do {
            int idc = readUEtrace(_in, "SH: reordering_of_pic_nums_idc");
            if (idc == 3)
                break;
            ops.add(idc);
            args.add(readUEtrace(_in, "SH: abs_diff_pic_num_minus1"));
        } while (true);
        return new int[][]{ops.toArray(), args.toArray()};
    }
}
