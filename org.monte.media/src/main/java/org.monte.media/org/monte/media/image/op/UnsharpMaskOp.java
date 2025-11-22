/*
 * @(#)UnsharpMaskOp.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.monte.media.image.FloatImages;

import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;


/// Sharpens an image by subtracting a gaussian blur from the image.
public class UnsharpMaskOp implements BufferedImageOp {
    private final ConvolveOp convolveH;
    private final ConvolveOp convolveV;
    private final float radius;
    private final float amount;
    private final float threshold;
    private final float clampMin, clampMax;

    public UnsharpMaskOp() {
        this(1.35f, 0.5f, 2.5f / 256f, 0f, 1f);
    }

    public UnsharpMaskOp(float radius, float amount, float threshold) {
        this(radius, amount, threshold, 0f, 1f);
    }

    public UnsharpMaskOp(float radius, float amount, float threshold, float clampMin, float clampMax) {
        float[] data = new GaussianKernelFactory().createKernel(radius);
        convolveH = new ConvolveOp(new Kernel(data.length, 1, data));
        convolveV = new ConvolveOp(new Kernel(1, data.length, data));
        this.radius = radius;
        this.amount = amount;
        this.threshold = threshold;
        this.clampMax = clampMax;
        this.clampMin = clampMin;
    }

    public float getRadius() {
        return radius;
    }

    public float getAmount() {
        return amount;
    }

    public float getThreshold() {
        return threshold;
    }

    public float getClampMin() {
        return clampMin;
    }

    public float getClampMax() {
        return clampMax;
    }

    /// Transforms the source [BufferedImage] and stores the result
    /// in the destination [BufferedImage].
    ///
    /// If the color models for the two images do not match, a color conversion
    /// into the destination color model is performed.
    ///
    /// @param src The `BufferedImage` to be filtered
    /// @param dst The `BufferedImage` in which to store the results
    /// @return the filtered image
    /// @throws IllegalArgumentException if `src` and`dst` are the same
    ///                                                                                                    or if `dst` does not have a buffer of type [DataBufferFloat].
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the " +
                    "same as the dst image");
        }
        if (dst == null) dst = createCompatibleDestImage(src, src.getColorModel());
        else if (!(dst.getRaster().getDataBuffer() instanceof DataBufferFloat)) {
            throw new IllegalArgumentException("dst must have data buffer float");
        }
        src = FloatImages.reuseSourceImage(src, dst.getColorModel());
        var dstH = convolveH.createCompatibleDestImage(src, dst.getColorModel());
        var dstV = convolveV.createCompatibleDestImage(src, dst.getColorModel());
        dstH = convolveH.filter(src, dstH);
        dstV = convolveV.filter(dstH, dstV);

        var in = (DataBufferFloat) src.getRaster().getDataBuffer();
        var blurred = (DataBufferFloat) dstV.getRaster().getDataBuffer();
        var out = (DataBufferFloat) dst.getRaster().getDataBuffer();

        for (int bank = 0, n = blurred.getNumBanks(); bank < n; bank++) {
            blend(in.getData(bank), blurred.getData(bank), out.getData(bank), amount, threshold);
        }
        return dst;

    }

    private void blend(float[] inPixels, float[] blurredPixels, float[] outPixels, float amount, float threshold) {
        //float a = 4 * amount + 1;
        float a = amount;
        for (int i = 0; i < Math.min(blurredPixels.length, outPixels.length); i++) {
            float diff = inPixels[i] - blurredPixels[i];
            if (Math.abs(diff) >= threshold) {
                outPixels[i] = Math.clamp(diff * a + inPixels[i], clampMin, clampMax);
            } else {
                outPixels[i] = inPixels[i];
            }
        }
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle2D.Double(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        int dstWidth = src.getWidth();
        int dstHeight = src.getHeight();
        ColorSpace cs = destCM.getColorSpace();
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT);
        SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT,
                dstWidth, dstHeight, cs.getNumComponents());
        return new BufferedImage(cm, Raster.createWritableRaster(
                sampleModel, new DataBufferFloat(dstWidth * dstHeight, cm.getNumComponents()),
                new Point(0, 0)), cm.isAlphaPremultiplied(), null);

    }


    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return new RenderingHints(null);
    }

}
