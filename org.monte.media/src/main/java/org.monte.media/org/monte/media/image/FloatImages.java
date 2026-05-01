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

/// FIXME Incorporate the code of this class into the [Images] class.
public class FloatImages {

    /// Returns the provided source image if it has a float raster and the
    /// specified color model, or converts it to a float raster.
    ///
    /// @param src    source image
    /// @param destCM the destination color model
    /// @param dest   reuse this image instead of creating a new image
    /// @return the converted or reused source image
    public static BufferedImage convertImage(BufferedImage src, ColorModel destCM, BufferedImage dest) {
        if (src.getColorModel() == destCM) {
            return src;
        }

        int w = src.getWidth();
        int h = src.getHeight();
        dest = reuseDestImage(dest, w, h, destCM);

        WritableRaster srcRast = src.getRaster();
        if (srcRast.getTransferType() == DataBuffer.TYPE_BYTE
                && src.getColorModel().getColorSpace() == destCM.getColorSpace()
                && src.getSampleModel() instanceof PixelInterleavedSampleModel srcSampleModel) {
            convertImagePixelInterleavedSameColorSpace(dest, srcSampleModel, srcRast, h, w);
        } else if (srcRast.getTransferType() == DataBuffer.TYPE_BYTE
                && src.getColorModel().getColorSpace().isCS_sRGB()
                && src.getSampleModel() instanceof PixelInterleavedSampleModel pism) {
            convertImagePixelInterleavedFromSRGB(destCM, dest, pism, srcRast, h, w);
        } else if (srcRast.getTransferType() == DataBuffer.TYPE_BYTE
                && src.getSampleModel() instanceof PixelInterleavedSampleModel pism) {
            convertImagePixelInterleavedDifferentColorSpace(src, destCM, dest, pism, srcRast, h, w);
        } else {
            // This is fast, but does not work well in a parallel stream.
            // With Graphics2D only one thread can call drawImage at a time.
            var g = dest.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
        }

        return dest;
    }

    private static void convertImagePixelInterleavedDifferentColorSpace(BufferedImage src, ColorModel destCM, BufferedImage dest, PixelInterleavedSampleModel pism, WritableRaster srcRast, int h, int w) {
        WritableRaster dstRast = dest.getRaster();
        var srcCS = src.getColorModel().getColorSpace();
        var destCS = destCM.getColorSpace();
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
                destPix = srcCS.toCIEXYZ(destPix);
                destPix = destCS.fromCIEXYZ(destPix);
                destData0[(yindex + x)] = destPix[0];
                destData1[(yindex + x)] = destPix[1];
                destData2[(yindex + x)] = destPix[2];
            }
        }
    }

    private static void convertImagePixelInterleavedFromSRGB(ColorModel destCM, BufferedImage dest, PixelInterleavedSampleModel pism, WritableRaster srcRast, int h, int w) {
        WritableRaster dstRast = dest.getRaster();
        var destCS = destCM.getColorSpace();
        //if (destCS == ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) destCS = LinearSrgbColorSpace.getInstance();
        byte[] srcData = ((DataBufferByte) srcRast.getDataBuffer()).getData();

        PixelInterleavedSampleModel srcSampleModel = (PixelInterleavedSampleModel) srcRast.getSampleModel();
        int[] srcBandOffsets = srcSampleModel.getBandOffsets();
        int[] dstBankIndices = ((BandedSampleModel) dstRast.getSampleModel()).getBankIndices();
        int numBands = srcSampleModel.getNumBands();
        float[] destData0 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[0]);
        float[] destData1 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[1]);
        float[] destData2 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[2]);
        float[] destData3 = numBands == 4 && dstBankIndices.length == 4 ? ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[3]) : null;
        for (int y = 0; y < h; y++) {
            //IntStream.range(0, h).parallel().forEach(y -> {
            float[] destPix = new float[3];
            int yindex = y * w;
            for (int x = 0; x < w; x++) {
                destPix[0] = (srcData[(yindex + x) * numBands + srcBandOffsets[0]] & 0xff) * (1f / 255f);
                destPix[1] = (srcData[(yindex + x) * numBands + srcBandOffsets[1]] & 0xff) * (1f / 255f);
                destPix[2] = (srcData[(yindex + x) * numBands + srcBandOffsets[2]] & 0xff) * (1f / 255f);
                destPix = destCS.fromRGB(destPix);
                destData0[(yindex + x)] = destPix[0];
                destData1[(yindex + x)] = destPix[1];
                destData2[(yindex + x)] = destPix[2];
                if (destData3 != null) {
                    destData3[(yindex + x)] = (srcData[(yindex + x) * numBands + srcBandOffsets[3]] & 0xff) * (1f / 256f);
                }
            }
            //});
        }
    }

    private static void convertImagePixelInterleavedSameColorSpace(BufferedImage dest, PixelInterleavedSampleModel srcSampleModel, WritableRaster srcRast, int h, int w) {
        WritableRaster dstRast = dest.getRaster();
        byte[] srcData = ((DataBufferByte) srcRast.getDataBuffer()).getData();
        int[] srcBandOffsets = srcSampleModel.getBandOffsets();
        int[] dstBankIndices = ((BandedSampleModel) dstRast.getSampleModel()).getBankIndices();
        float[] destData0 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[0]);
        float[] destData1 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[1]);
        float[] destData2 = ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[2]);
        int numBands = srcSampleModel.getNumBands();
        float[] destData3 = numBands == 4 && dstBankIndices.length == 4 ? ((DataBufferFloat) dstRast.getDataBuffer()).getData(dstBankIndices[3]) : null;
        for (int y = 0; y < h; y++) {
            int yindex = y * w;
            for (int x = 0; x < w; x++) {
                destData0[(yindex + x)] = (srcData[(yindex + x) * numBands + srcBandOffsets[0]] & 0xff) * (1f / 256f);
                destData1[(yindex + x)] = (srcData[(yindex + x) * numBands + srcBandOffsets[1]] & 0xff) * (1f / 256f);
                destData2[(yindex + x)] = (srcData[(yindex + x) * numBands + srcBandOffsets[2]] & 0xff) * (1f / 256f);
                if (destData3 != null) {
                    destData3[(yindex + x)] = (srcData[(yindex + x) * numBands + srcBandOffsets[3]] & 0xff) * (1f / 256f);
                }
            }
        }
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

    /// Returns the provided destination image if it has a float raster and the specified color model,
    /// or converts it to a float raster.
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
        BufferedImage bufferedImage = new BufferedImage(cm, Raster.createWritableRaster(
                sampleModel, new DataBufferFloat(width * height, cm.getNumComponents()),
                new Point(0, 0)), cm.isAlphaPremultiplied(), null);
        return bufferedImage;
    }
}
