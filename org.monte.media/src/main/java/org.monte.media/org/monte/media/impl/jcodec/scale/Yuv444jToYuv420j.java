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
 * @author Stanislav Vitvitskyy
 */
public class Yuv444jToYuv420j implements Transform {

    @Override
    public void transform(Picture src, Picture dst) {
        int size = src.getWidth() * src.getHeight();
        System.arraycopy(src.getPlaneData(0), 0, dst.getPlaneData(0), 0, size);

        for (int plane = 1; plane < 3; plane++) {
            byte[] srcPl = src.getPlaneData(plane);
            byte[] dstPl = dst.getPlaneData(plane);
            int srcStride = src.getPlaneWidth(plane);
            for (int y = 0, srcOff = 0, dstOff = 0; y < src.getHeight(); y += 2, srcOff += srcStride) {
                for (int x = 0; x < src.getWidth(); x += 2, srcOff += 2, dstOff++) {
                    dstPl[dstOff] = (byte) ((srcPl[srcOff] + srcPl[srcOff + 1] + srcPl[srcOff + srcStride]
                            + srcPl[srcOff + srcStride + 1] + 2) >> 2);
                }
            }
        }
    }
}
