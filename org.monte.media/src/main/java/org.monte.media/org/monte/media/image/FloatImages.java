/*
 * @(#)FloatImages.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

public class FloatImages {

    /**
     * Returns the provided source image if it has a float raster and the specified color model, or converts it to a float raster.
     *
     * @param src    source image
     * @param destCM the destination color model
     * @return the converted or reused source image
     */
    public static BufferedImage reuseSourceImage(BufferedImage src, ColorModel destCM) {
        BufferedImage newSrc = reuseDestImage(src, src.getWidth(), src.getHeight(), destCM);
        if (newSrc == src) {
            return src;
        }
        var g = newSrc.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return newSrc;
    }

    /**
     * Returns the provided destination image if it has a float raster and the specified color space, or converts it to a float raster.
     *
     * @param dst destination image, can be null
     * @param cs  the destination color space
     * @return the converted or reused source image
     */
    public static BufferedImage reuseDestImage(BufferedImage dst, int width, int height, ColorSpace cs) {
        ColorModel destCM = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT);
        return reuseDestImage(dst, width, height, destCM);
    }

    /**
     * Returns the provided destination image if it has a float raster and the specified color model, or converts it to a float raster.
     *
     * @param dst    destination image, can be null
     * @param destCM the destination color model
     * @return the converted or reused source image
     */
    public static BufferedImage reuseDestImage(BufferedImage dst, int width, int height, ColorModel destCM) {
        if (dst != null
                && dst.getWidth() == width
                && dst.getHeight() == height
                && dst.getSampleModel() instanceof BandedSampleModel bsm
                && bsm.getDataType() == DataBuffer.TYPE_FLOAT
                && dst.getColorModel() == destCM) {
            return dst;
        }
        ComponentColorModel cm;
        if (!(destCM instanceof ComponentColorModel ccm) || destCM.getTransferType() != DataBuffer.TYPE_FLOAT) {
            ColorSpace cs = destCM.getColorSpace();
            cm = new ComponentColorModel(cs, destCM.hasAlpha(), destCM.isAlphaPremultiplied(), ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT);
        } else {
            cm = ccm;
        }
        SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT,
                width, height, cm.getNumComponents());
        return new BufferedImage(cm, Raster.createWritableRaster(
                sampleModel, new DataBufferFloat(width * height, cm.getNumComponents()),
                new Point(0, 0)), cm.isAlphaPremultiplied(), null);
    }
}
