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
public class RgbToYuv422p implements Transform {

    @Override
    public void transform(Picture img, Picture dst) {

        byte[] y = img.getData()[0];
        byte[] out1 = new byte[3];
        byte[] out2 = new byte[3];
        byte[][] dstData = dst.getData();

        int off = 0, offSrc = 0;
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth() >> 1; j++) {
                int offY = off << 1;

                RgbToYuv420p.rgb2yuv(y[offSrc++], y[offSrc++], y[offSrc++], out1);
                dstData[0][offY] = out1[0];

                RgbToYuv420p.rgb2yuv(y[offSrc++], y[offSrc++], y[offSrc++], out2);
                dstData[0][offY + 1] = out2[0];

                dstData[1][off] = (byte) ((out1[1] + out2[1] + 1) >> 1);
                dstData[2][off] = (byte) ((out1[2] + out2[2] + 1) >> 1);
                ++off;
            }
        }
    }
}