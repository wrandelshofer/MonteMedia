package org.monte.media.impl.jcodec.codecs.h264.io.model;

import org.monte.media.impl.jcodec.common.io.BitReader;
import org.monte.media.impl.jcodec.common.io.BitWriter;

import java.nio.ByteBuffer;

import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readBool;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readU;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readUEtrace;
import static org.monte.media.impl.jcodec.codecs.h264.io.write.CAVLCWriter.writeTrailingBits;

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
 * Sequence Parameter Set Extension entity of H264 bitstream
 * <p>
 * Capable to serialize / deserialize itself with CAVLC bit stream
 *
 * @author The JCodec project
 */
public class SeqParameterSetExt {

    public int seq_parameter_set_id;
    public int aux_format_idc;
    public int bit_depth_aux_minus8;
    public boolean alpha_incr_flag;
    public boolean additional_extension_flag;
    public int alpha_opaque_value;
    public int alpha_transparent_value;

    public static SeqParameterSetExt read(ByteBuffer is) {
        BitReader _in = BitReader.createBitReader(is);

        SeqParameterSetExt spse = new SeqParameterSetExt();
        spse.seq_parameter_set_id = readUEtrace(_in, "SPSE: seq_parameter_set_id");
        spse.aux_format_idc = readUEtrace(_in, "SPSE: aux_format_idc");
        if (spse.aux_format_idc != 0) {
            spse.bit_depth_aux_minus8 = readUEtrace(_in, "SPSE: bit_depth_aux_minus8");
            spse.alpha_incr_flag = readBool(_in, "SPSE: alpha_incr_flag");
            spse.alpha_opaque_value = readU(_in, spse.bit_depth_aux_minus8 + 9, "SPSE: alpha_opaque_value");
            spse.alpha_transparent_value = readU(_in, spse.bit_depth_aux_minus8 + 9, "SPSE: alpha_transparent_value");
        }
        spse.additional_extension_flag = readBool(_in, "SPSE: additional_extension_flag");

        return spse;
    }

    public void write(ByteBuffer out) {
        BitWriter writer = new BitWriter(out);
        writeTrailingBits(writer);
    }
}