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
 * @author The JCodec project
 */
public class HRDParameters {

    // cpb_cnt_minus1
    public int cpbCntMinus1;
    // bit_rate_scale
    public int bitRateScale;
    // cpb_size_scale
    public int cpbSizeScale;
    // bit_rate_value_minus1
    public int[] bitRateValueMinus1;
    // cpb_size_value_minus1
    public int[] cpbSizeValueMinus1;
    // cbr_flag
    public boolean[] cbrFlag;
    // initial_cpb_removal_delay_length_minus1
    public int initialCpbRemovalDelayLengthMinus1;
    // cpb_removal_delay_length_minus1
    public int cpbRemovalDelayLengthMinus1;
    // dpb_output_delay_length_minus1
    public int dpbOutputDelayLengthMinus1;
    // time_offset_length
    public int timeOffsetLength;

}
