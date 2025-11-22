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
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class FloatImages {

    /// Returns the provided source image if it has a float raster and the
    /// specified color model, or converts it to a float raster.
    ///
    /// @param src    source image
    /// @param destCM the destination color model
    /// @return the converted or reused source image
    public static BufferedImage reuseSourceImage(BufferedImage src, ColorModel destCM) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = reuseDestImage(src, w, h, destCM);
        if (dest == src) {
            return src;
        }

        WritableRaster srcRast = src.getRaster();
        if (srcRast.getTransferType() == DataBuffer.TYPE_BYTE
                && src.getColorModel().getColorSpace() == destCM.getColorSpace()
                && src.getSampleModel() instanceof PixelInterleavedSampleModel pism) {


            WritableRaster dstRast = dest.getRaster();
            byte[] srcData = ((DataBufferByte) srcRast.getDataBuffer()).getData();
            float[] destPix = new float[3];
            int[] bandOffsets = pism.getBandOffsets();
            float[] destData0 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(bandOffsets[0]);
            float[] destData1 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(bandOffsets[1]);
            float[] destData2 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(bandOffsets[2]);
            for (int y = 0; y < h; y++) {
                int yindex = y * w;
                for (int x = 0; x < w; x++) {
                    destPix[0] = (srcData[(yindex + x) * 3] & 0xff) * (1f / 255f);
                    destPix[1] = (srcData[(yindex + x) * 3 + 1] & 0xff) * (1f / 255f);
                    destPix[2] = (srcData[(yindex + x) * 3 + 2] & 0xff) * (1f / 255f);
                    destData0[(yindex + x)] = destPix[0];
                    destData1[(yindex + x)] = destPix[1];
                    destData2[(yindex + x)] = destPix[2];
                }
            }
        } else {
            // This is fast, but does not work well in a parallel stream.
            // With Graphics2D only one thread can call drawImage at a time.
            var g = dest.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
        }

        return dest;
    }

    /// Returns the provided destination image if it has a float raster and the specified color space, or converts it to a float raster.
    ///
    /// @param dst destination image, can be null
    /// @param cs  the destination color space
    /// @return the converted or reused source image
    public static BufferedImage reuseDestImage(BufferedImage dst, int width, int height, ColorSpace cs) {
        ColorModel destCM = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT);
        return reuseDestImage(dst, width, height, destCM);
    }

    /// Returns the provided destination image if it has a float raster and the specified color model, or converts it to a float raster.
    ///
    /// @param dst    destination image, can be null
    /// @param destCM the destination color model
    /// @return the converted or reused source image
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
