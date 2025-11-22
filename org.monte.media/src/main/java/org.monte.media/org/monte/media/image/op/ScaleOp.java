/*
 * @(#)ScaleOp.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.monte.media.image.FloatImages;
import org.monte.media.image.algo.NearestNeighbourResampleAlgoFloat;
import org.monte.media.image.algo.ResampleAlgoFloat;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferFloat;


/// Scales an [BufferedImage] image by processing [#TYPE_FLOAT] data samples.
///
/// Converts the image to [#TYPE_FLOAT] if necessary.
///
/// Applies an approximated Gaussian Blur to the image before downscaling it.
///
/// The color space of the image affects the output:
///
///   - sRGB will darken around dark areas
///   - linear RGB will
///
/// sRGB will darken edges, linear RGB will
public class ScaleOp implements BufferedImageOp {
    private final int srcHeight;
    private final int srcWidth;
    private final int dstHeight;
    private final int dstWidth;
    private final BoxGaussianOp gaussian;
    private final ResampleAlgoFloat resampler;

    /// Constructs a [ScaleOp] given the desired
    /// width and height of the source and destination image.
    ///
    /// @param srcWidth  the width
    /// @param srcHeight the height
    /// @param dstWidth  the width
    /// @param dstHeight the height
    public ScaleOp(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        this(srcWidth, srcHeight, dstWidth, dstHeight, 0.5f, new GaussianKernelFactory(), new NearestNeighbourResampleAlgoFloat());
    }

    /// Constructs a [ScaleOp] given the desired
    /// width and height of the source and destination image.
    ///
    /// @param srcWidth           the width
    /// @param srcHeight          the height
    /// @param dstWidth           the width
    /// @param dstHeight          the height
    /// @param kernelRadiusFactor The factor with which the scale factor is multiplied
    ///                                                                                                                                                                                       to compute the radius of the blur kernel.
    public ScaleOp(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                   float kernelRadiusFactor, SeparableKernelFactory kernelFactory, ResampleAlgoFloat resampler) {
        this.srcWidth = srcWidth;
        this.srcHeight = srcHeight;
        this.dstWidth = dstWidth;
        this.dstHeight = dstHeight;

        if (kernelRadiusFactor > 0) {
            float widthFactor = srcWidth / (float) dstWidth;
            float heightFactor = srcHeight / (float) dstHeight;
            float radiusX = kernelRadiusFactor * widthFactor;
            float radiusY = kernelRadiusFactor * heightFactor;
            float[] dataH = kernelFactory.createKernel(radiusX);
            float[] dataV = kernelFactory.createKernel(radiusY);
            gaussian = new BoxGaussianOp(radiusX, radiusY);
        } else {
            gaussian = null;
        }
        this.resampler = resampler;
    }

    /// Transforms the source [BufferedImage] and stores the result
    /// in the destination [BufferedImage].
    ///
    /// If the color models for the two images do not match, a color conversion
    /// into the destination color model is performed.
    ///
    /// @param src The `BufferedImage` to be filtered
    /// @param dst The `BufferedImage` in which to store the results$
    /// @return the filtered image
    /// @throws IllegalArgumentException if `src` and`dst` are the same
    ///                                                                                                                                                                                                       or if `dst` does not have a buffer of type [DataBufferFloat].
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the " +
                    "same as the dst image");
        }
        if (dst == null || !(dst.getRaster().getDataBuffer() instanceof DataBufferFloat)) {
            dst = createCompatibleDestImage(src, src.getColorModel());
        }
        src = FloatImages.reuseSourceImage(src, dst.getColorModel());
        BufferedImage blurred;
        if (gaussian != null) {
            blurred = gaussian.createCompatibleDestImage(src, dst.getColorModel());
            blurred = gaussian.filter(src, blurred);
        } else {
            blurred = src;
        }
        var in = (DataBufferFloat) blurred.getRaster().getDataBuffer();
        var out = (DataBufferFloat) dst.getRaster().getDataBuffer();

        for (int bank = 0, n = in.getNumBanks(); bank < n; bank++) {
            resampler.resample(in.getData(bank), srcWidth, srcHeight, 0, srcWidth, out.getData(bank), dstWidth, dstHeight, 0, dstWidth);
        }
        return dst;
    }


    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle2D.Double(0, 0, dstWidth, dstHeight);
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return FloatImages.reuseDestImage(null, dstWidth, dstHeight, destCM);
    }


    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        dstPt.setLocation(srcPt.getX() * dstWidth / (double) srcWidth,
                srcPt.getY() * dstHeight / (double) srcHeight
        );
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return new RenderingHints(null);
    }


}
