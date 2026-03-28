/*
 * @(#)H264RawYuvEncoder.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.codec.video.ImageBufferToArray;
import org.monte.media.h264.impl.jcodec.codecs.h264.decode.SliceHeaderReader;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.NALUnit;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.NALUnitType;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.PictureParameterSet;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.h264.impl.jcodec.codecs.h264.io.write.SliceHeaderWriter;
import org.monte.media.h264.impl.jcodec.common.io.BitReader;
import org.monte.media.h264.impl.jcodec.common.io.BitWriter;
import org.monte.media.h264.impl.jcodec.common.model.ColorSpace;
import org.monte.media.h264.impl.jcodec.common.model.Picture;
import org.monte.media.h264.impl.jcodec.impl.AWTUtil;
import org.monte.media.io.ByteArrayImageOutputStream;

import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVC1;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.h264.codec.video.PictureEncoder.ENCODING_PICTURE;

/// This code has been derived from "World's Smallest h.264 Encoder".
///
/// Stream format:
/// ```
/// +-----------------------------------+
/// | sequence parameter set (SPS)      |
/// +-----------------------------------+
/// | picture parameter set (PPS)       |
/// +-----------------------------------+
/// | video frames:                     |
/// |   +-----------------------------+ |
/// |   | slice header                | |
/// |   +-----------------------------+ |
/// |   | macroblocks:                | |
/// |   |   +-----------------------+ | |
/// |   |   | macroblock header     | | |
/// |   |   +-----------------------+ | |
/// |   |   | Y[16][16] array       | | |
/// |   |   +-----------------------+ | |
/// |   |   | Cb[8][8] array        | | |
/// |   |   +-----------------------+ | |
/// |   |   | Cr[8][8] array        | | |
/// |   |   +-----------------------+ | |
/// |   +-----------------------------+ |
/// |   | slice stop bit              | |
/// |   +-----------------------------+ |
/// +-----------------------------------+
/// ```
/// Structs:
///
/// The type `u(n)` indicates an unsigned integer of n bits,
/// and `ue(v)` indicates an unsigned exponential-Golomb-coded value of a variable number of bits.
/// ///
/// ```
/// Sequence Parameter Set (SPS)
///
/// |Parameter Name               |Type |Value|Comments                             |
/// |-----------------------------|:---:|:---:|-------------------------------------|
/// |nal_marker                   |u(32)|   1 |Must be set to 1.                    |
/// |forbidden_zero_bit           | u(1)|   0 |Must be set to 0.                    |
/// |nal_ref_idc                  | u(2)|   3 |3 means it is “important”.           |
/// |nal_unit_type                | u(5)|   7 |Indicates this is a SPS.             |
/// |                             |     |     |                                     |
/// |profile_idc                  | u(8)|  66 |Baseline profile.                    |
/// |constraint_set0_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |constraint_set1_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |constraint_set2_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |constraint_set3_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |constraint_set4_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |constraint_set5_flag         | u(1)|   0 |We’re not going to honor constraints.|
/// |reserved_zero_2bits          | u(2)|   0 |Better set them to zero.             |
/// |level_idc                    | u(8)|  10 |Level 1, sec A.3.1.                  |
/// |seq_parameter_set_id         |ue(v)|   0 |We’ll just use id 0.                 |
/// |chroma_format_idc            |ue(v)|null |Only present in a higher level_idc.  |
/// |separate_colour_plane_flag   | u(1)|null |Only present in a higher level_idc.  |
/// |log2_max_frame_num_minus4    |ue(v)|   0 |Let’s have as few frame numbers as   |
/// |                             |     |     |possible.                            |
/// |pic_order_cnt_type           |ue(v)|   0 |Keep things simple.                  |
/// |log2_max_pic_order_cnt_lsb_minus4|ue(v)|0|Fewer is better.                     |
/// |num_ref_frames               |ue(v)|   0 |We will only send I-slices.          |
/// |gaps_in_frame_num_value_allowed_flag|u(1)|0|We will have no gaps.              |
/// |pic_width_in_mbs_minus_1     |ue(v)|   7 |128px width = 8 macroblocks wide.    |
/// |pic_height_in_map_units_minus_1|ue(v)| 5 |92px height = 6 macroblocks high.    |
/// |frame_mbs_only_flag          | u(1)|   1 |We will not do field/frame encoding. |
/// |direct_8x8_inference_flag    | u(1)|   0 |Used for B slices. We will not send  |
/// |                             |     |     |B slices.                            |
/// |frame_cropping_flag          | u(1)|   0 |We will not do frame cropping.       |
/// |vui_parameters_present_flag  | u(1)|   0 |We will not send VUI data.           |
/// |rbsp_stop_one_bit            | u(1)|   1 |Stop bit.                            |
/// ```
/// ```
/// Picture Parameter Set (PPS)
///
/// |Parameter Name               |Type |Value|Comments                             |
/// |-----------------------------|:---:|:---:|-------------------------------------|
/// |nal_marker                   |u(32)|   1 |Must be set to 1.                    |
/// |forbidden_zero_bit           | u(1)|   0 |Must be set to 0.                    |
/// |nal_ref_idc                  | u(2)|   3 |3 means it is “important”.           |
/// |nal_unit_type                | u(5)|   8 |Indicates this is a PPS.             |
/// |                             |     |     |                                     |
/// |pic_parameter_set_id         |ue(v)|   0 |                                     |
/// |seq_parameter_set_id         |ue(v)|   0 |                                     |
/// |entropy_coding_mode_flag     | u(1)|   0 |                                     |
/// |bottom_field_pic_order_in_frame_present_flag|u(1)|0|                           |
/// |num_slice_groups_minus1      |ue(v)|   0 |                                     |
/// |.slice_group_map_type        | u(1)|   0 |                                     |
/// |.run_length_minus1           | u(8)|   0 |                                     |
/// |.top_left                    |ue(v)|   0 |                                     |
/// |.bottom_right                |ue(v)|   0 |                                     |
/// |.slice_group_change_direction_flag|u(1)|0|                                     |
/// |.slice_group_change_rate_minus1|u(2)|  0 |                                     |
/// |.pic_size_in_map_units_minus1|ue(v)[]| 0 |                                     |
/// |.slice_group_id              |u(v)[]|null|                                     |
/// |num_ref_idx_l0_default_active_minus1|ue(v)|0|                                  |
/// |num_ref_idx_l2_default_active_minus1|ue(v)|0|                                  |
/// |weighted_pred_flag           |ue(v)|   0 |                                     |
/// |weighted_bipred_idc          |ue(v)|   0 |                                     |
/// |pic_init_qp_minus26          |u(1) |   0 |                                     |
/// |pic_init_qs_minus26          |ue(v)|   7 |                                     |
/// |chroma_qp_index_offset       |ue(v)|   5 |                                     |
/// |deblocking_filter_control_present_flag|u(1)|1|                                 |
/// |constrained_intra_pred_flag  | u(1)|   0 |                                     |
/// |redundant_pic_cnt_present_flag|u(1)|   0 |                                     |
/// |transform_8x8_mode_flag      | u(1)|   0 |                                     |
/// |pic_scaling_matrix_present_flag|u(1)|  0 |                                     |
/// |pic_scaling_list_present_flag[i]|u(1)| 0 |                                     |
/// |second_chroma_qp_index_offset| u(1)|   0 |                                     |
/// ```
/// ```
/// Slice Header
///
/// |Parameter Name               |Type |Value|Comments                             |
/// |-----------------------------|:---:|:---:|-------------------------------------|
/// |nal_marker                   |u(32)|   1 |Must be set to 1.                    |
/// |forbidden_zero_bit           | u(1)|   0 |Must be set to 0.                    |
/// |nal_ref_idc                  | u(2)|   0 |0 means it is not “important”.       |
/// |nal_unit_type                | u(5)|   5 |Indicates this is an Intra Slice.    |
/// |                             |     |     |                                     |
/// |first_mb_in_slice            |ue(v)|   0 |Address of first macroblock in slice.|
/// |slice_type                   |ue(v)|   2 |Indicates an I slice                 |
/// |pic_parameter_set_id         |ue(v)|   0 |Id of picture parameter set in use.  |
/// |colour_plane_id              | u(2)|null |Only present in a higher level_idc.  |
/// |frame_num                    | u(v)|   0 |0 for IDR picture.                   |
/// |field_pic_flag               | u(1)|null |1 indicates slice of a coded field.  |
/// |bottom_field_flag            | u(1)|   0 |1 indicates slice of a bottom field. |
/// |idr_pic_id                   |ue(v)|   0 |Identifies an IDR picture. The value |
/// |                             |     |     |is in range 0 to 65535, inclusive.   |
/// |pic_order_cnt_lsb            | u(v)|   0 |                                     |
/// |delta_pic_order_cnt_bottom   |se(v)|   0 |                                     |
/// |delta_pic_order_cnt[0]       |se(v)|   0 |                                     |
/// |delta_pic_order_cnt[1]       |se(v)|   0 |                                     |
/// |redundant_pic_cnt            |ue(v)|   0 |                                     |
/// |direct_spatial_mv_pred_flag  | u(1)|   0 |                                     |
/// |num_ref_idx_active_override_flag|u(1)| 0 |                                     |
/// |num_ref_idx_l0_active_minus1 |ue(v)|   0 |                                     |
/// |num_ref_idx_l1_active_minus1 |ue(v)|   0 |                                     |
/// |cabac_init_idc               |ue(v)|   0 |                                     |
/// |slice_qp_delta               |se(v)|   0 |                                     |
/// |sp_for_switch_flag           | u(1)|   0 |                                     |
/// |slice_qs_delta               |se(v)|   0 |                                     |
/// |disable_deblocking_filter_idc|ue(v)|   0 |                                     |
/// |slice_alpha_c0_offset_div2   |se(v)|   0 |                                     |
/// |slice_beta_offset_div2       |se(v)|   0 |                                     |
/// |slice_group_change_cycle     | u(v)|   0 |                                     |
/// |ref_pic_list_modification_flag_l0|u(1)|   0 |                                     |
/// |modification_of_pic_nums_idc |ue(v)|   0 |                                     |
/// |abs_diff_pic_num_minus1      |ue(v)|   0 |                                     |
/// |long_term_pic_num            |ue(v)|   0 |                                     |
/// |ref_pic_list_modification_flag_l1|u(1)|   0 |                                     |
/// |modification_of_pic_nums_idc |ue(v)|   0 |                                     |
/// |abs_diff_pic_num_minus1      |ue(v)|   0 |                                     |
/// |long_term_pic_num            |ue(v)|   0 |                                     |
/// ```
/// References:
///
/// <dl>
///     <dt>Ben Mesander. World's Smallest h.264 Encoder.
///     [BSD 2-Clause License.](https://www.cardinalpeak.com/downloads/hello264.c)</dt>
///     <dd>[cardinalpeak.com](https://www.cardinalpeak.com/blog/worlds-smallest-h-264-encoder)</dd>
///     <dt>Alex Izvorski. h264bitstream.
///     [GNU Lesser General Public License.](https://github.com/aizvorski/h264bitstream/blob/master/LICENSE)
///
/// Note: We have not used code from this project. We only used the binaries from that project to verify our output.
///     </dt>
///     <dd>[github.com](https://github.com/aizvorski/h264bitstream/blob/master/h264_stream.h)</dd>
///     <dt>ITU-T Recommendation H.264, "Advanced video coding for generic audiovisual services", May 2003.</dt>
/// </dl>
public class H264RawYuvEncoder extends org.monte.media.av.AbstractCodec {
    int sequenceNumber = 0;
    private PictureParameterSet picParameterSet;
    private SeqParameterSet seqParameterSet;

    public H264RawYuvEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_BUFFERED_IMAGE,
                                DataClassKey, BufferedImage.class), //
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_PICTURE,
                                DataClassKey, Picture.class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                DepthKey, 24,
                                EncodingKey, ENCODING_AVC1,
                                DataClassKey, byte[].class), //
                }//
        );
        name = "Uncompressed H264 Encoder";
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        BufferedImage bufferedImage = new ImageBufferToArray().getBufferedImage(in);
        // int lumaWidth = bufferedImage.getWidth();
        // int lumaHeight = bufferedImage.getHeight();
        int lumaWidth = 128;
        int lumaHeight = 96;
        int chromaWidth = lumaWidth / 2;
        int chromaHeight = lumaHeight / 2;


        Image scaledImage = bufferedImage.getScaledInstance(lumaWidth, lumaHeight, Image.SCALE_SMOOTH);
        bufferedImage = new BufferedImage(lumaWidth, lumaHeight, bufferedImage.getType());
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        Picture pict = AWTUtil.fromBufferedImage(bufferedImage, ColorSpace.YUV420J);
        frame_t frame = new frame_t(new byte[lumaHeight][lumaWidth],
                new byte[chromaHeight][chromaWidth],
                new byte[chromaHeight][chromaWidth]);
        byte[][] data = pict.getData();
        for (int i = 0; i < lumaHeight; i++) {
            for (int j = 0; j < lumaWidth; j++) {
                frame.Y[i][j] = (byte) (data[0][i * lumaWidth + j] + 128);
            }
        }
        for (int i = 0; i < chromaHeight; i++) {
            for (int j = 0; j < chromaWidth; j++) {
                frame.Cr[i][j] = (byte) (data[2][i * chromaWidth + j] + 128);
                frame.Cb[i][j] = (byte) (data[1][i * chromaWidth + j] + 128);
            }
        }

        out.sequenceNumber = sequenceNumber;
        if (sequenceNumber % outputFormat.get(KeyFrameIntervalKey, 60) == 0) {
            this.picParameterSet = createPicParameterSet();
            this.seqParameterSet = createSeqParameterSet(lumaWidth, lumaHeight);
            out.setFlag(KEYFRAME);
        }
        sequenceNumber++;

        var bout = new ByteArrayImageOutputStream(out.data instanceof byte[] bs ? bs : new byte[1024]);
        try {
            if (out.isFlag(KEYFRAME)) {
                bout.write(createSeqParameterSetBytes(seqParameterSet));
                bout.write(createPicParameterSetBytes(picParameterSet));
                createSliceHeader(lumaWidth, lumaHeight, seqParameterSet, picParameterSet, sequenceNumber);
            }
            encode(frame, bout, lumaWidth, lumaHeight, seqParameterSet, picParameterSet);
            out.data = bout.getBuffer();
            out.length = bout.size();
        } catch (IOException e) {
            out.setFlag(DISCARD);
            out.exception = e;
            return CODEC_FAILED;
        }

        return CODEC_OK;
    }

    private byte[] createPicParameterSetBytes(PictureParameterSet ppset) {
        ByteBuffer bb = ByteBuffer.allocate(128);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 1);
        bb.put((byte) ((3 << 5) | (NALUnitType.PPS.getValue())));
        ppset.write(bb);
        return java.util.Arrays.copyOf(bb.array(), bb.position());
    }

    private static PictureParameterSet createPicParameterSet() {
        var ppset = new PictureParameterSet();
        ppset.picParameterSetId = 0;
        ppset.seqParameterSetId = 0;
        ppset.entropyCodingModeFlag = false;
        ppset.picOrderPresentFlag = false;
        ppset.numSliceGroupsMinus1 = 0;
        ppset.sliceGroupMapType = 0;
        ppset.sliceGroupChangeDirectionFlag = false;
        ppset.sliceGroupChangeRateMinus1 = 0;
        ppset.runLengthMinus1 = null;
        ppset.topLeft = null;
        ppset.bottomRight = null;
        ppset.sliceGroupId = null;
        ppset.numRefIdxActiveMinus1 = new int[]{0, 0};
        ppset.weightedPredFlag = false;
        ppset.weightedBipredIdc = 0;
        ppset.picInitQpMinus26 = 0;
        ppset.picInitQsMinus26 = 0;
        ppset.chromaQpIndexOffset = 0;
        ppset.deblockingFilterControlPresentFlag = false;
        ppset.constrainedIntraPredFlag = false;
        ppset.redundantPicCntPresentFlag = false;
        ppset.extended = null;
        return ppset;
    }

    private byte[] createSeqParameterSetBytes(SeqParameterSet spset) {
        ByteBuffer bb = ByteBuffer.allocate(128);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 1);
        bb.put((byte) ((3 << 5) | (NALUnitType.SPS.getValue())));
        spset.write(bb);
        return java.util.Arrays.copyOf(bb.array(), bb.position());
    }

    private static SeqParameterSet createSeqParameterSet(int lumaWidth, int lumaHeight) {
        SeqParameterSet spset = new SeqParameterSet();
        spset.profileIdc = 66;
        spset.constraintSet0Flag = false;
        spset.constraintSet1Flag = false;
        spset.constraintSet2Flag = false;
        spset.constraintSet3Flag = false;
        spset.constraintSet4Flag = false;
        spset.constraintSet5Flag = false;
        spset.levelIdc = 10;
        spset.seqParameterSetId = 0;
        spset.chromaFormatIdc = ColorSpace.YUV420J;
        spset.separateColourPlaneFlag = false;
        spset.bitDepthLumaMinus8 = 0;
        spset.bitDepthChromaMinus8 = 0;
        spset.qpprimeYZeroTransformBypassFlag = false;
        spset.scalingMatrix = null;
        spset.log2MaxFrameNumMinus4 = 0;
        spset.picOrderCntType = 0;
        spset.log2MaxPicOrderCntLsbMinus4 = 0;
        spset.deltaPicOrderAlwaysZeroFlag = false;
        spset.offsetForNonRefPic = 0;
        spset.offsetForTopToBottomField = 0;
        spset.offsetForRefFrame = new int[0];
        spset.numRefFrames = 0;
        spset.gapsInFrameNumValueAllowedFlag = false;
        spset.picWidthInMbsMinus1 = ((lumaWidth + 15) >> 4) - 1;
        spset.picHeightInMapUnitsMinus1 = ((lumaHeight + 15) >> 4) - 1;
        spset.frameMbsOnlyFlag = true;
        spset.mbAdaptiveFrameFieldFlag = false;
        spset.direct8x8InferenceFlag = false;
        spset.frameCroppingFlag = false;
        spset.frameCropLeftOffset = 0;
        spset.frameCropRightOffset = 0;
        spset.frameCropTopOffset = 0;
        spset.frameCropBottomOffset = 0;
        spset.vuiParams = null;
        spset.fieldPicFlag = false;
        spset.numRefFramesInPicOrderCntCycle = 0;
        return spset;
    }

    private byte[] createSliceHeader(int lumaWidth, int lumaHeight, SeqParameterSet spset, PictureParameterSet ppset, int sequenceNumber) {
        SliceHeader sliceHeader = new SliceHeader();
        sliceHeader.firstMbInSlice = 0;
        sliceHeader.sliceType = SliceType.I;
        sliceHeader.sliceTypeRestr = true;
        sliceHeader.picParameterSetId = 0;
        sliceHeader.frameNum = 0;
        sliceHeader.fieldPicFlag = false;
        sliceHeader.bottomFieldFlag = false;
        sliceHeader.idrPicId = sequenceNumber % 65536;
        sliceHeader.picOrderCntLsb = sequenceNumber % 16;
        sliceHeader.refPicMarkingNonIDR = null;
        sliceHeader.refPicMarkingIDR = null;
        sliceHeader.refPicReordering = null;
        sliceHeader.predWeightTable = null;
        sliceHeader.deltaPicOrderCntBottom = 0;
        sliceHeader.deltaPicOrderCnt = new int[]{0, 0};
        sliceHeader.redundantPicCnt = 0;
        sliceHeader.directSpatialMvPredFlag = false;
        sliceHeader.numRefIdxActiveOverrideFlag = false;
        sliceHeader.numRefIdxActiveMinus1 = new int[]{0, 0};
        sliceHeader.cabacInitIdc = 0;
        sliceHeader.sliceQpDelta = 0;
        sliceHeader.spForSwitchFlag = false;
        sliceHeader.sliceQsDelta = 0;
        sliceHeader.disableDeblockingFilterIdc = 0;
        sliceHeader.sliceAlphaC0OffsetDiv2 = 0;
        sliceHeader.sliceBetaOffsetDiv2 = 0;
        sliceHeader.sliceGroupChangeCycle = 0;
        sliceHeader.sps = spset;
        sliceHeader.pps = ppset;

        ByteBuffer bb0 = ByteBuffer.wrap(slice_header);
        bb0.position(5);
        BitReader br0 = BitReader.createBitReader(bb0);
        var sh = SliceHeaderReader.readPart1(br0);
        sh.sps = spset;
        sh.pps = ppset;
        sh = SliceHeaderReader.readPart2(sh, new NALUnit(NALUnitType.IDR_SLICE, 0), sh.sps, sh.pps, br0);

        System.out.println("\nsliceHeaderSoll " + sh);

        System.out.println("\nsliceHeader SOLL:" + Arrays.toString(slice_header));
        ByteBuffer bb = ByteBuffer.allocate(100);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put((byte) 1);
        bb.put((byte) ((NALUnitType.IDR_SLICE.getValue())));
        BitWriter bw = new BitWriter(bb);
        SliceHeaderWriter.write(sliceHeader, true, 0, bw);
        bw.write1Bit(1);//trailing 1 bit
        bw.flush();
        byte[] new_slice_header = Arrays.copyOf(bw.getBuffer().array(), bw.getBuffer().position());
        System.out.println("\nsliceHeader IST :" + Arrays.toString(new_slice_header));
        slice_header = new_slice_header;
        return slice_header;
    }

    /* SQCIF */
    private static final int LUMA_WIDTH = 128;
    private static final int LUMA_HEIGHT = 96;
    private static final int CHROMA_WIDTH = LUMA_WIDTH / 2;
    private static final int CHROMA_HEIGHT = LUMA_HEIGHT / 2;

    /* YUV planar data, as written by ffmpeg */
    record frame_t(
            byte[][] Y,
            byte[][] Cb,
            byte[][] Cr
    ) {
    }

    /* H.264 bitstreams */
    private byte[] sps = {0x00, 0x00, 0x00, 0x01, 0x67, 0x42, 0x00, 0x0a, (byte) 0xf8, 0x41, (byte) 0xa2};
    private byte[] pps = {0x00, 0x00, 0x00, 0x01, 0x68, (byte) 0xce, 0x38, (byte) 0x80};
    private byte[] slice_header = {0x00, 0x00, 0x00, 0x01, 0x05, (byte) 0x88, (byte) 0x84, 0x21, (byte) 0xa0};
    private final byte[] macroblock_header = {0x0d, 0x00};

    /* Write a macroblock's worth of YUV data in I_PCM mode */
    private void macroblock(int i, int j, frame_t frame, ImageOutputStream out) throws IOException {
        int x, y;

        if (!((i == 0) && (j == 0))) {
            out.write(macroblock_header);
        }

        for (x = i * 16; x < (i + 1) * 16; x++)
            for (y = j * 16; y < (j + 1) * 16; y++)
                out.write(frame.Y[x][y]);
        for (x = i * 8; x < (i + 1) * 8; x++)
            for (y = j * 8; y < (j + 1) * 8; y++)
                out.write(frame.Cb[x][y]);
        for (x = i * 8; x < (i + 1) * 8; x++)
            for (y = j * 8; y < (j + 1) * 8; y++)
                out.write(frame.Cr[x][y]);
    }

    /* Write out PPS, SPS, and loop over input, writing out I-slices. */
    private void encode(frame_t frame, ImageOutputStream out, int lumaWidth, int lumaHeight, SeqParameterSet seqParameterSet, PictureParameterSet pictureParameterSet) throws IOException {
        int i, j;

        //out.write(sps);
        //out.write(pps);

        out.write(createSliceHeader(lumaWidth, lumaHeight, seqParameterSet, pictureParameterSet, sequenceNumber));

        for (i = 0; i < lumaHeight / 16; i++)
            for (j = 0; j < lumaWidth / 16; j++)
                macroblock(i, j, frame, out);

        out.write(0x80); /* slice stop bit */
    }

}

