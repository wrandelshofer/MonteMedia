package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Utils2;
import org.monte.media.impl.jcodec.common.io.BitReader;
import org.monte.media.impl.jcodec.common.tools.Debug;

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
 * @author The JCodec project
 */
public class CAVLCReader {

    private CAVLCReader() {

    }

    public static int readNBit(BitReader bits, int n, String message) {
        int val = bits.readNBit(n);

        trace(message, val);

        return val;
    }

    public static int readUE(BitReader bits) {
        int cnt = 0;
        while (bits.read1Bit() == 0 && cnt < 32)
            cnt++;

        int res = 0;
        if (cnt > 0) {
            long val = bits.readNBit(cnt);

            res = (int) ((1 << cnt) - 1 + val);
        }

        return res;
    }

    public static int readUEtrace(BitReader bits, String message) {
        int res = readUE(bits);

        trace(message, res);

        return res;
    }

    public static int readSE(BitReader bits, String message) {
        int val = readUE(bits);

        val = H264Utils2.golomb2Signed(val);

        trace(message, val);

        return val;
    }

    public static boolean readBool(BitReader bits, String message) {

        boolean res = bits.read1Bit() == 0 ? false : true;

        trace(message, res ? 1 : 0);

        return res;
    }

    public static int readU(BitReader bits, int i, String string) {
        return (int) readNBit(bits, i, string);
    }

    public static int readTE(BitReader bits, int max) {
        if (max > 1)
            return readUE(bits);
        return ~bits.read1Bit() & 0x1;
    }

    public static int readME(BitReader bits, String string) {
        return readUEtrace(bits, string);
    }

    public static int readZeroBitCount(BitReader bits, String message) {
        int count = 0;
        while (bits.read1Bit() == 0 && count < 32)
            count++;

        if (Debug.debug)
            trace(message, String.valueOf(count));

        return count;
    }

    public static boolean moreRBSPData(BitReader bits) {
        return !(bits.remaining() < 32 && bits.checkNBit(1) == 1 && (bits.checkNBit(24) << 9) == 0);
    }
}