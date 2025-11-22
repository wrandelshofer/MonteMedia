/*
 * @(#)AvcDecoderConfigurationRecord.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff;

import org.monte.media.util.ByteArray;

import java.util.Set;

/// Header data for an encoded H.264 frame in a [org.monte.media.av.Buffer].
///
/// The [#data] contains the data for video* coding layer (VCL). The VCL data is embedded
/// in data for the network* abstraction layer (NAL). The NAL describes how the stream could be split into packets
/// - Network Abstraction Layer units (NALUnit).
///
/// There are two "classes" of NAL units types defined in ITU-T Specification's* Annex A - VCL and non-VCL NAL units.
/// The first one holds the encoded video, while the other does not contain video data at all.
///
/// VCL:
///     <dl>
///         <dt>Coded slice of a non-IDR picture (non-IDR)</dt>
///         <dd>Contains a part or a complete non-keyframe (that is: P-frame or a B-frame)</dd>
///         <dt>Coded slice of an IDR picture (IDR)</dt>
///         <dd>Contains a part or a complete keyframe (also known as I-frame).
///         The name IDR (that stands for instantaneous decoding refresh) originates from the fact that the decoder can
///         "forget" the previous frames when the new keyframe appears, since it contains the complete information about
///         the frame.</dd>
///     </dl>
/// non-VCL:
///     <dl>
///         <dt>Sequence parameter set (SPS)</dt>
///         <dd>Contains metadata that is applicable to one or more coded video sequences.
///         In that NALu you will find information allowing you to calculate the video resolution or H.264 profile.</dd>
///         <dt>Picture parameter set (PPS)</dt>
///         <dd>Contains metadata applicable to one or more coded pictures.</dd>
///         <dt>Access unit delimiter (AUD)</dt>
///         <dd>Just a separator between access units</dd>
///         <dt>Supplemental enhancement information (SEI)</dt>
///         <dd>Contains some additional metadata that "assist in processes related to decoding, display or other
///         purposes". At the same time, information stored in SEI is not required to restore the picture during the
///         decoding process, so the decoders are not obliged to process SEI. In fact, SEI is defined as Annex D to
///         the ITU specification</dd>
///     </dl>
/// References:
/// <dl>
///     <dt>H.264 layers - VCL vs NAL</dt><dd>[membrane.stream](https://membrane.stream/learn/h264/2)</dd>
///     <dt>NALu types</dt><dd>[membrane.stream](https://membrane.stream/learn/h264/3)</dd>
/// </dl>
///
/// @param sequenceParameterSetNALUnit List of SPS NALUnits.
/// @param pictureParameterSetNALUnit  List of PPS NALUnits.
public record AvcDecoderConfigurationRecord(
        int avcProfileIndication,
        int profileCompatibility,
        int avcLevelIndication,
        int nalLengthSize,
        Set<ByteArray> sequenceParameterSetNALUnit, Set<ByteArray> pictureParameterSetNALUnit) {
}
