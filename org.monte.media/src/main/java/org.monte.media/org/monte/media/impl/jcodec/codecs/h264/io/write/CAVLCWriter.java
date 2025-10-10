package org.monte.media.impl.jcodec.codecs.h264.io.write;

import org.monte.media.impl.jcodec.api.NotImplementedException;
import org.monte.media.impl.jcodec.common.io.BitWriter;
import org.monte.media.impl.jcodec.common.tools.MathUtil;

import static org.monte.media.impl.jcodec.common.tools.Debug.trace;

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
 * A class responsible for outputting exp-Golomb values into binary stream
 *
 * @author The JCodec project
 */
public class CAVLCWriter {

    private CAVLCWriter() {
    }

    public static void writeUtrace(BitWriter out, int value, int n, String message) {
        out.writeNBit(value, n);
        trace(message, value);
    }

    public static void writeUE(BitWriter out, int value) {
        int bits = 0;
        int cumul = 0;
        for (int i = 0; i < 15; i++) {
            if (value < cumul + (1 << i)) {
                bits = i;
                break;
            }
            cumul += (1 << i);
        }
        out.writeNBit(0, bits);
        out.write1Bit(1);
        out.writeNBit(value - cumul, bits);
    }

    public static void writeSE(BitWriter out, int value) {
        writeUE(out, MathUtil.golomb(value));
    }

    public static void writeUEtrace(BitWriter out, int value, String message) {
        writeUE(out, value);
        trace(message, value);
    }

    public static void writeSEtrace(BitWriter out, int value, String message) {
        writeUE(out, MathUtil.golomb(value));
        trace(message, value);
    }

    public static void writeTE(BitWriter out, int value, int max) {
        if (max > 1)
            writeUE(out, value);
        else
            out.write1Bit(~value & 0x1);
    }

    public static void writeBool(BitWriter out, boolean value, String message) {
        out.write1Bit(value ? 1 : 0);
        trace(message, value ? 1 : 0);
    }

    public static void writeU(BitWriter out, int i, int n) {
        out.writeNBit(i, n);
    }

    public static void writeNBit(BitWriter out, long value, int n, String message) {
        for (int i = 0; i < n; i++) {
            out.write1Bit((int) (value >> (n - i - 1)) & 0x1);
        }
        trace(message, value);
    }

    public static void writeTrailingBits(BitWriter out) {
        out.write1Bit(1);
        out.flush();
    }

    public static void writeSliceTrailingBits() {
        throw new NotImplementedException("todo");
    }
}