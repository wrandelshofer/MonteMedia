package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.Picture;

import static java.lang.System.arraycopy;

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
public class Yuv422pToYuv420p implements Transform {

    @Override
    public void transform(Picture src, Picture dst) {
        int lumaSize = src.getWidth() * src.getHeight();
        arraycopy(src.getPlaneData(0), 0, dst.getPlaneData(0), 0, lumaSize);
        copyAvg(src.getPlaneData(1), dst.getPlaneData(1), src.getPlaneWidth(1), src.getPlaneHeight(1));
        copyAvg(src.getPlaneData(2), dst.getPlaneData(2), src.getPlaneWidth(2), src.getPlaneHeight(2));
    }

    private void copyAvg(byte[] src, byte[] dst, int width, int height) {
        int offSrc = 0, offDst = 0;
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++, offDst++, offSrc++) {
                dst[offDst] = (byte) ((src[offSrc] + src[offSrc + width] + 1) >> 1);
            }
            offSrc += width;
        }
    }
}
