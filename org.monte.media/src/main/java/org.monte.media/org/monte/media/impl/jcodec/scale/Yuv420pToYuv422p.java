package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.Picture;

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
public class Yuv420pToYuv422p implements Transform {

    public Yuv420pToYuv422p() {
    }

    @Override
    public void transform(Picture src, Picture dst) {
        copy(src.getPlaneData(0), dst.getPlaneData(0), src.getWidth(), dst.getWidth(), dst.getHeight());

        _copy(src.getPlaneData(1), dst.getPlaneData(1), 0, 0, 1, 2, src.getWidth() >> 1, dst.getWidth() >> 1,
                src.getHeight() >> 1, dst.getHeight());
        _copy(src.getPlaneData(1), dst.getPlaneData(1), 0, 1, 1, 2, src.getWidth() >> 1, dst.getWidth() >> 1,
                src.getHeight() >> 1, dst.getHeight());
        _copy(src.getPlaneData(2), dst.getPlaneData(2), 0, 0, 1, 2, src.getWidth() >> 1, dst.getWidth() >> 1,
                src.getHeight() >> 1, dst.getHeight());
        _copy(src.getPlaneData(2), dst.getPlaneData(2), 0, 1, 1, 2, src.getWidth() >> 1, dst.getWidth() >> 1,
                src.getHeight() >> 1, dst.getHeight());
    }

    private static final void _copy(byte[] src, byte[] dest, int offX, int offY, int stepX, int stepY, int strideSrc,
                                    int strideDest, int heightSrc, int heightDst) {
        int offD = offX + offY * strideDest, srcOff = 0;
        for (int i = 0; i < heightSrc; i++) {
            for (int j = 0; j < strideSrc; j++) {
                dest[offD] = src[srcOff++];
                offD += stepX;
            }
            int lastOff = offD - stepX;
            for (int j = strideSrc * stepX; j < strideDest; j += stepX) {
                dest[offD] = dest[lastOff];
                offD += stepX;
            }
            offD += (stepY - 1) * strideDest;
        }
        int lastLine = offD - stepY * strideDest;
        for (int i = heightSrc * stepY; i < heightDst; i += stepY) {
            for (int j = 0; j < strideDest; j += stepX) {
                dest[offD] = dest[lastLine + j];
                offD += stepX;
            }
            offD += (stepY - 1) * strideDest;
        }
    }

    private static void copy(byte[] src, byte[] dest, int srcWidth, int dstWidth, int dstHeight) {
        int height = src.length / srcWidth;
        int dstOff = 0, srcOff = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < srcWidth; j++) {
                dest[dstOff++] = src[srcOff++];
            }
            for (int j = srcWidth; j < dstWidth; j++)
                dest[dstOff++] = dest[srcWidth - 1];
        }
        int lastLine = (height - 1) * dstWidth;
        for (int i = height; i < dstHeight; i++) {
            for (int j = 0; j < dstWidth; j++) {
                dest[dstOff++] = dest[lastLine + j];
            }
        }
    }
}