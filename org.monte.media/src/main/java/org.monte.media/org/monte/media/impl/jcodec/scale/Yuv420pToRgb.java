package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.common.tools.MathUtil;

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
public class Yuv420pToRgb implements Transform {

    public Yuv420pToRgb() {
    }

    @Override
    public final void transform(Picture src, Picture dst) {
        byte[] yh = src.getPlaneData(0);
        byte[] uh = src.getPlaneData(1);
        byte[] vh = src.getPlaneData(2);
        byte[] yl = null;
        byte[] ul = null;
        byte[] vl = null;
        byte[][] low = src.getLowBits();

        if (low != null) {
            yl = low[0];
            ul = low[1];
            vl = low[2];
        }
        byte[] data = dst.getPlaneData(0);
        byte[] lowBits = dst.getLowBits() == null ? null : dst.getLowBits()[0];
        boolean hbd = src.isHiBD() && dst.isHiBD();
        int lowBitsNumSrc = src.getLowBitsNum();
        int lowBitsNumDst = dst.getLowBitsNum();

        int offLuma = 0, offChroma = 0;
        int stride = dst.getWidth();
        for (int i = 0; i < (dst.getHeight() >> 1); i++) {
            for (int k = 0; k < (dst.getWidth() >> 1); k++) {
                int j = k << 1;
                if (hbd) {
                    YUV420pToRGBH2H(yh[offLuma + j], yl[offLuma + j], uh[offChroma], ul[offChroma], vh[offChroma],
                            vl[offChroma], lowBitsNumSrc, data, lowBits, lowBitsNumDst, (offLuma + j) * 3);
                    YUV420pToRGBH2H(yh[offLuma + j + 1], yl[offLuma + j + 1], uh[offChroma], ul[offChroma],
                            vh[offChroma], vl[offChroma], lowBitsNumSrc, data, lowBits, lowBitsNumDst,
                            (offLuma + j + 1) * 3);
                    YUV420pToRGBH2H(yh[offLuma + j + stride], yl[offLuma + j + stride], uh[offChroma], ul[offChroma],
                            vh[offChroma], vl[offChroma], lowBitsNumSrc, data, lowBits, lowBitsNumDst,
                            (offLuma + j + stride) * 3);
                    YUV420pToRGBH2H(yh[offLuma + j + stride + 1], yl[offLuma + j + stride + 1], uh[offChroma],
                            ul[offChroma], vh[offChroma], vl[offChroma], lowBitsNumSrc, data, lowBits, lowBitsNumDst,
                            (offLuma + j + stride + 1) * 3);
                } else {
                    YUV420pToRGBN2N(yh[offLuma + j], uh[offChroma], vh[offChroma], data, (offLuma + j) * 3);
                    YUV420pToRGBN2N(yh[offLuma + j + 1], uh[offChroma], vh[offChroma], data, (offLuma + j + 1) * 3);

                    YUV420pToRGBN2N(yh[offLuma + j + stride], uh[offChroma], vh[offChroma], data,
                            (offLuma + j + stride) * 3);
                    YUV420pToRGBN2N(yh[offLuma + j + stride + 1], uh[offChroma], vh[offChroma], data, (offLuma + j
                            + stride + 1) * 3);
                }

                ++offChroma;
            }
            if ((dst.getWidth() & 0x1) != 0) {
                int j = dst.getWidth() - 1;

                YUV420pToRGBN2N(yh[offLuma + j], uh[offChroma], vh[offChroma], data, (offLuma + j) * 3);
                YUV420pToRGBN2N(yh[offLuma + j + stride], uh[offChroma], vh[offChroma], data, (offLuma + j + stride) * 3);

                ++offChroma;
            }

            offLuma += 2 * stride;
        }
        if ((dst.getHeight() & 0x1) != 0) {
            for (int k = 0; k < (dst.getWidth() >> 1); k++) {
                int j = k << 1;
                YUV420pToRGBN2N(yh[offLuma + j], uh[offChroma], vh[offChroma], data, (offLuma + j) * 3);
                YUV420pToRGBN2N(yh[offLuma + j + 1], uh[offChroma], vh[offChroma], data, (offLuma + j + 1) * 3);

                ++offChroma;
            }
            if ((dst.getWidth() & 0x1) != 0) {
                int j = dst.getWidth() - 1;

                YUV420pToRGBN2N(yh[offLuma + j], uh[offChroma], vh[offChroma], data, (offLuma + j) * 3);

                ++offChroma;
            }
        }
    }

    public static void YUV420pToRGBN2N(byte y, byte u, byte v, byte[] data, int off) {
        int c = y + 112;
        int r = (298 * c + 409 * v + 128) >> 8;
        int g = (298 * c - 100 * u - 208 * v + 128) >> 8;
        int b = (298 * c + 516 * u + 128) >> 8;
        data[off] = (byte) (MathUtil.clip(r, 0, 255) - 128);
        data[off + 1] = (byte) (MathUtil.clip(g, 0, 255) - 128);
        data[off + 2] = (byte) (MathUtil.clip(b, 0, 255) - 128);
    }

    public static void YUV420pToRGBH2H(byte yh, byte yl, byte uh, byte ul, byte vh, byte vl, int nlbi,
                                       byte[] data, byte[] lowBits, int nlbo, int off) {
        int clipMax = ((1 << nlbo) << 8) - 1;
        int round = (1 << nlbo) >> 1;

        int c = ((yh + 128) << nlbi) + yl - 64;
        int d = ((uh + 128) << nlbi) + ul - 512;
        int e = ((vh + 128) << nlbi) + vl - 512;

        int r = MathUtil.clip((298 * c + 409 * e + 128) >> 8, 0, clipMax);
        int g = MathUtil.clip((298 * c - 100 * d - 208 * e + 128) >> 8, 0, clipMax);
        int b = MathUtil.clip((298 * c + 516 * d + 128) >> 8, 0, clipMax);

        int valR = MathUtil.clip((r + round) >> nlbo, 0, 255);
        data[off] = (byte) (valR - 128);
        lowBits[off] = (byte) (r - (valR << nlbo));

        int valG = MathUtil.clip((g + round) >> nlbo, 0, 255);
        data[off + 1] = (byte) (valG - 128);
        lowBits[off + 1] = (byte) (g - (valG << nlbo));

        int valB = MathUtil.clip((b + round) >> nlbo, 0, 255);
        data[off + 2] = (byte) (valB - 128);
        lowBits[off + 2] = (byte) (b - (valB << nlbo));
    }
}