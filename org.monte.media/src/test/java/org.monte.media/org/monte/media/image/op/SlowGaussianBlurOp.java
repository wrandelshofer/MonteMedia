/*
 * @(#)BoxGaussianBlurOp.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.monte.media.image.FloatImages;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferFloat;

/// Applies a Gaussian filter.
///
/// References:
///
/// Ivan Kutskir. Fastest Gaussian Blur (in linear time). Algorithm 1.
/// : [blog.ivank.net](https://blog.ivank.net/fastest-gaussian-blur.html)
public class SlowGaussianBlurOp implements BufferedImageOp {
    private final float sigma;

    public SlowGaussianBlurOp(float sigma) {
        this.sigma = sigma;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dest) {
            throw new IllegalArgumentException("src image cannot be the " +
                    "same as the dst image");
        }
        if (dest == null) dest = createCompatibleDestImage(src, src.getColorModel());
        else if (!(dest.getRaster().getDataBuffer() instanceof DataBufferFloat)) {
            throw new IllegalArgumentException("dst must have data buffer float");
        }
        src = FloatImages.convertImage(src, dest.getColorModel(), null);

        var in = (DataBufferFloat) src.getRaster().getDataBuffer();
        var out = (DataBufferFloat) dest.getRaster().getDataBuffer();

        int width = src.getWidth();
        int height = src.getHeight();
        for (int bank = 0, n = in.getNumBanks(); bank < n; bank++) {
            gaussBlur_1(in.getData(bank), out.getData(bank), width, height, sigma);
        }
        return dest;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        int width = src.getWidth();
        int height = src.getHeight();
        return FloatImages.reuseDestImage(null, width, height, destCM);
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            return new Point2D.Double(srcPt.getX(), srcPt.getY());
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    /// Applies a Gaussian filter.
    ///
    /// @param scl source channel
    /// @param tcl target channel
    /// @param w   width
    /// @param h   height
    /// @param r   radius
    private void gaussBlur_1(float[] scl, float[] tcl, int w, int h, float r) {
        var rs = (int) Math.ceil(r * 2.57);     // significant radius
        for (var i = 0; i < h; i++)
            for (var j = 0; j < w; j++) {
                double val = 0, wsum = 0;
                for (var iy = i - rs; iy < i + rs + 1; iy++)
                    for (var ix = j - rs; ix < j + rs + 1; ix++) {
                        int x = Math.min(w - 1, Math.max(0, ix));
                        int y = Math.min(h - 1, Math.max(0, iy));
                        var dsq = (ix - j) * (ix - j) + (iy - i) * (iy - i);
                        var wght = Math.exp(-dsq / (2 * r * r)) / (Math.PI * 2 * r * r);
                        val += scl[y * w + x] * wght;
                        wsum += wght;
                    }
                tcl[i * w + j] = (float) (val / wsum);
            }
    }
}
