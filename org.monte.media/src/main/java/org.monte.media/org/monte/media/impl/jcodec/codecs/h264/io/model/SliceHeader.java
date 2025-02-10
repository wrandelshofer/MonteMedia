package org.monte.media.impl.jcodec.codecs.h264.io.model;

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
 * Slice header H264 bitstream entity
 * <p>
 * capable to serialize / deserialize with CAVLC bitstream
 *
 * @author The JCodec project
 */
public class SliceHeader {

    public SeqParameterSet sps;
    public PictureParameterSet pps;

    public RefPicMarking refPicMarkingNonIDR;
    public RefPicMarkingIDR refPicMarkingIDR;

    public int[][][] refPicReordering;

    // pred_weight_table
    public PredictionWeightTable predWeightTable;
    // first_mb_in_slice
    public int firstMbInSlice;

    // field_pic_flag
    public boolean fieldPicFlag;

    //  slice_type
    public SliceType sliceType;

    // slice_type_restr
    public boolean sliceTypeRestr;

    // pic_parameter_set_id
    public int picParameterSetId;

    // frame_num
    public int frameNum;

    // bottom_field_flag
    public boolean bottomFieldFlag;

    // idr_pic_id
    public int idrPicId;

    // pic_order_cnt_lsb
    public int picOrderCntLsb;

    // delta_pic_order_cnt_bottom
    public int deltaPicOrderCntBottom;

    // delta_pic_order_cnt
    public int[] deltaPicOrderCnt;

    // redundant_pic_cnt
    public int redundantPicCnt;

    // direct_spatial_mv_pred_flag
    public boolean directSpatialMvPredFlag;

    // num_ref_idx_active_override_flag
    public boolean numRefIdxActiveOverrideFlag;

    // num_ref_idx_active_minus1
    public int[] numRefIdxActiveMinus1;

    // cabac_init_idc
    public int cabacInitIdc;

    // slice_qp_delta
    public int sliceQpDelta;

    // sp_for_switch_flag
    public boolean spForSwitchFlag;

    // slice_qs_delta
    public int sliceQsDelta;

    // disable_deblocking_filter_idc
    public int disableDeblockingFilterIdc;

    // slice_alpha_c0_offset_div2
    public int sliceAlphaC0OffsetDiv2;

    // slice_beta_offset_div2
    public int sliceBetaOffsetDiv2;

    // slice_group_change_cycle
    public int sliceGroupChangeCycle;

    public SliceHeader() {
        this.numRefIdxActiveMinus1 = new int[2];
    }


}
