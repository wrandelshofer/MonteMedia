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
            gaussBlur_5(in.getData(bank), out.getData(bank), width, height, sigmaX, sigmaY);
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
    /// @param in  source channel, content will be destroyed
    /// @param out target channel
    /// @param w   width
    /// @param h   height
    /// @param rX  radius for x-axis
    /// @param rY  radius for y-axis
    private void gaussBlur_4(float[] in, float[] out, int w, int h, float rX, float rY) {
        var bxsX = boxesForGauss(rX, 3);
        var bxsY = boxesForGauss(rY, 3);
        boxBlur_4(in, out, w, h, (bxsX[0] - 1) / 2, (bxsY[0] - 1) / 2);
        boxBlur_4(out, in, w, h, (bxsX[1] - 1) / 2, (bxsY[1] - 1) / 2);
        boxBlur_4(in, out, w, h, (bxsX[2] - 1) / 2, (bxsY[2] - 1) / 2);
    }

    /// Approximates a Gaussian filter by applying 3 Box filters.
    ///
    /// @param in  source channel, content will be destroyed
    /// @param out target channel
    /// @param w   width
    /// @param h   height
    /// @param rX  radius for x-axis
    /// @param rY  radius for y-axis
    private void gaussBlur_5(float[] in, float[] out, int w, int h, float rX, float rY) {
        var bxsX = boxesForGauss(rX, 3);
        var bxsY = boxesForGauss(rY, 3);
        boxBlurHorizontal(in, out, w, h, (bxsX[0] - 1) / 2);
        boxBlurHorizontal(out, in, w, h, (bxsX[1] - 1) / 2);
        boxBlurHorizontal(in, out, w, h, (bxsX[2] - 1) / 2);
        transposeImage(out, in, w, h);
        boxBlurHorizontal(in, out, h, w, (bxsY[0] - 1) / 2);
        boxBlurHorizontal(out, in, h, w, (bxsY[1] - 1) / 2);
        boxBlurHorizontal(in, out, h, w, (bxsY[2] - 1) / 2);
        transposeImage(out, in, h, w);
        System.arraycopy(in, 0, out, 0, in.length);
    }

    /**
     * Transpose the image. The transpose is done in blocks to reduce the number of cache misses.
     *
     * @param in  the input array
     * @param out the output array
     * @param w   the width of the input image
     * @param h   the height of hte input image
     */
    private static void transposeImage(float[] in, float[] out, int w, int h) {
        int block = 256 / 4;
        for (int x = 0; x < w; x += block) {
            for (int y = 0; y < h; y += block) {
                int blockx = Math.min(w, x + block) - x;
                int blocky = Math.min(h, y + block) - y;
                for (int xx = 0; xx < blockx; xx++) {
                    for (int yy = 0; yy < blocky; yy++) {
                        out[yy + xx * h] = in[yy * w + xx];
                    }
                }
            }
        }
    }

    /// Applies a box filter.
    ///
    /// @param in  source channel, content will be destroyed
    /// @param out target channel
    /// @param w   width (scanline stride must be the same as the width)
    /// @param h   height
    /// @param rX  radius for x-axis
    /// @param rY  radius for y-axis
    private void boxBlur_4(float[] in, float[] out, int w, int h, int rX, int rY) {
        System.arraycopy(in, 0, out, 0, in.length);
        boxBlurHorizontal(out, in, w, h, rX);
        boxBlurVertical(in, out, w, h, rY);
    }

    /// Applies a horizontal box filter.
    ///
    /// We compute `bh[i,j], bh[i,j+1], bh[i,j+2], ...`.
    /// But the neighboring values `bh[i,j]` and `bh[i,j+1]` are almost the same.
    /// The only difference is in one left-most value and one right-most value.
    /// So `bh[i,j+1] = bh[i,j] + f[i,j+r+1] − f[i,j−r]`.
    ///
    /// We will compute the one-dimensional blur by creating the accumulator.
    /// First, we put the value of left-most cell into it.
    /// Then we will compute next values just by editing the previous value in constant time.
    ///  This 1D blur has the complexity O(n) (independent on r).
    /// But it is performed twice to get box blur, which is performed 3 times to get gaussian blur.
    /// So the complexity of this gaussian blur is 6 * O(n).
    ///
    /// @param in  source channel
    /// @param out target channel
    /// @param w   width (scanline stride must be the same as the width)
    /// @param h   height
    /// @param r   radius for x-axis
    private void boxBlurHorizontal(float[] in, float[] out, int w, int h, int r) {
        // Compute the inverse of the box area, so that we can use a multiplication instead of a division
        float iarr = 1f / (r + r + 1);

        // For each row i
        for (var i = 0; i < h; i++) {
            int ti = i * w;// index in target channel
            int li = ti; // index of left-most value in source channel
            int ri = ti + r; // index of right-most value in source channel
            float fv = in[ti], lv = in[ti + w - 1];

            float val = (r + 1) * fv;
            for (var j = 0; j < r; j++) {
                val += in[ti + j];
            }
            for (var j = 0; j <= r; j++) {
                val += in[ri++] - fv;
                out[ti++] = val * iarr;
            }
            for (var j = r + 1; j < w - r; j++) {
                val += in[ri++] - in[li++];
                out[ti++] = val * iarr;
            }
            for (var j = w - r; j < w; j++) {
                val += lv - in[li++];
                out[ti++] = val * iarr;
            }
        }
    }

    /// Applies a vertical box filter.
    ///
    /// @param in  source channel
    /// @param out target channel
    /// @param w   width (scanline stride must be the same as the width)
    /// @param h   height
    /// @param r   radius for y-axis
    private void boxBlurVertical(float[] in, float[] out, int w, int h, int r) {
        float iarr = 1f / (r + r + 1);

        // For each column i
        for (var i = 0; i < w; i++) {
            int ti = i, li = ti, ri = ti + r * w;
            float fv = in[ti], lv = in[ti + w * (h - 1)];

            float val = (r + 1) * fv;
            for (var j = 0; j < r; j++) {
                val += in[ti + j * w];
            }
            for (var j = 0; j <= r; j++) {
                val += in[ri] - fv;
                out[ti] = val * iarr;
                ri += w;
                ti += w;
            }
            for (var j = r + 1; j < h - r; j++) {
                val += in[ri] - in[li];
                out[ti] = val * iarr;
                li += w;
                ri += w;
                ti += w;
            }
            for (var j = h - r; j < h; j++) {
                val += lv - in[li];
                out[ti] = val * iarr;
                li += w;
                ti += w;
            }
        }
    }
}
