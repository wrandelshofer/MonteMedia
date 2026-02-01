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

/// Approximates a Gaussian blur filter by applying three Box filters.
///
/// Let's denote the half of size of square as `br` ("box radius").
/// The constant value of weight is `1/(2⋅br)^2`
/// (so that the sum over the whole weight is 1).
///
/// We can define box blur as:
///
/// ```
/// b[i,j]= ∑ from y=i−br to i+br ( ∑ from x=j−br to j+br ( f[y,x]/(2⋅br)^2 ))
/// ```
///
/// References:
///
/// Ivan Kutskir. Fastest Gaussian Blur (in linear time). Algorithm 4.
/// : [blog.ivank.net](https://blog.ivank.net/fastest-gaussian-blur.html)
public class BoxGaussianBlurOp implements BufferedImageOp {
    private final float sigmaX;
    private final float sigmaY;

    public BoxGaussianBlurOp(float sigma) {
        this(sigma, sigma);
    }

    public BoxGaussianBlurOp(float sigmaX, float sigmaY) {
        this.sigmaX = sigmaX;
        this.sigmaY = sigmaY;
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
        var newSrc = FloatImages.convertImage(src, dest.getColorModel(), null);

        var in = (DataBufferFloat) newSrc.getRaster().getDataBuffer();
        var out = (DataBufferFloat) dest.getRaster().getDataBuffer();

        int width = src.getWidth();
        int height = src.getHeight();
        for (int bank = 0, n = in.getNumBanks(); bank < n; bank++) {
            gaussBlur_4(in.getData(bank), out.getData(bank), width, height, sigmaX, sigmaY);
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

    /// Converts the standard deviation of gaussian blur `sigma`
    /// into `n` box blurs with different sizes.
    ///
    /// @param sigma standard deviation
    /// @param n     number of boxes
    /// @return array of box blur sizes
    private int[] boxesForGauss(float sigma, int n) {
        var wIdeal = Math.sqrt((12 * sigma * sigma / n) + 1);  // Ideal averaging filter width
        var wl = Math.floor(wIdeal);
        if (wl % 2 == 0) wl--;
        var wu = wl + 2;

        var mIdeal = (12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
        var m = (float) (mIdeal);
        // var sigmaActual = Math.sqrt( (m*wl*wl + (n-m)*wu*wu - n)/12 );

        var sizes = new int[n];
        for (var i = 0; i < n; i++) sizes[i] = (int) (i < m ? wl : wu);
        return sizes;
    }

    /// Approximates a Gaussian filter by applying 3 Box filters.
    ///
    /// @param scl source channel, content will be destroyed
    /// @param tcl target channel
    /// @param w   width
    /// @param h   height
    /// @param rX  radius for x-axis
    /// @param rY  radius for y-axis
    private void gaussBlur_4(float[] scl, float[] tcl, int w, int h, float rX, float rY) {
        var bxsX = boxesForGauss(rX, 3);
        var bxsY = boxesForGauss(rY, 3);
        boxBlur_4(scl, tcl, w, h, (bxsX[0] - 1) / 2, (bxsY[0] - 1) / 2);
        boxBlur_4(tcl, scl, w, h, (bxsX[1] - 1) / 2, (bxsY[1] - 1) / 2);
        boxBlur_4(scl, tcl, w, h, (bxsX[2] - 1) / 2, (bxsY[2] - 1) / 2);
    }

    private void boxBlur_4(float[] scl, float[] tcl, int w, int h, int rX, int rY) {
        System.arraycopy(scl, 0, tcl, 0, scl.length);
        boxBlurH_4(tcl, scl, w, h, rX);
        boxBlurT_4(scl, tcl, w, h, rY);
    }

    private void boxBlurH_4(float[] scl, float[] tcl, int w, int h, int r) {
        float iarr = 1f / (r + r + 1);
        for (var i = 0; i < h; i++) {
            int ti = i * w, li = ti, ri = ti + r;
            float fv = scl[ti], lv = scl[ti + w - 1], val = (r + 1) * fv;
            for (var j = 0; j < r; j++) {
                val += scl[ti + j];
            }
            for (var j = 0; j <= r; j++) {
                val += scl[ri++] - fv;
                tcl[ti++] = val * iarr;
            }
            for (var j = r + 1; j < w - r; j++) {
                val += scl[ri++] - scl[li++];
                tcl[ti++] = val * iarr;
            }
            for (var j = w - r; j < w; j++) {
                val += lv - scl[li++];
                tcl[ti++] = val * iarr;
            }
        }
    }

    private void boxBlurT_4(float[] scl, float[] tcl, int w, int h, int r) {
        float iarr = 1f / (r + r + 1);
        for (var i = 0; i < w; i++) {
            int ti = i, li = ti, ri = ti + r * w;
            float fv = scl[ti], lv = scl[ti + w * (h - 1)], val = (r + 1) * fv;
            for (var j = 0; j < r; j++) {
                val += scl[ti + j * w];
            }
            for (var j = 0; j <= r; j++) {
                val += scl[ri] - fv;
                tcl[ti] = val * iarr;
                ri += w;
                ti += w;
            }
            for (var j = r + 1; j < h - r; j++) {
                val += scl[ri] - scl[li];
                tcl[ti] = val * iarr;
                li += w;
                ri += w;
                ti += w;
            }
            for (var j = h - r; j < h; j++) {
                val += lv - scl[li];
                tcl[ti] = val * iarr;
                li += w;
                ti += w;
            }
        }
    }
}
