/*
 * @(#)ConvolveOp.java
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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Kernel;
import java.util.stream.IntStream;

import static java.lang.Math.clamp;


/// A convolution filter for images that have a data buffer of type [DataBuffer#TYPE_FLOAT].
///
/// References:
///
/// Jerry Huxtable, Jerry's Java Image Processing Pages. MIT License.
/// : [jhlabs.com](http://www.jhlabs.com/ip/filters/download.html)
///
public class ConvolveOp implements BufferedImageOp {
    private final Kernel kernel;
    private final EdgeAction edgeAction;


    /// Creates a new instance with [MirrorEdgeAction].
    ///
    /// @param kernel the kernel
    public ConvolveOp(Kernel kernel) {
        this(kernel, new MirrorEdgeAction());
    }

    /// Creates a new instance.
    ///
    /// @param kernel     the kernel
    /// @param edgeAction the edge action
    public ConvolveOp(Kernel kernel, EdgeAction edgeAction) {
        this.kernel = kernel;
        this.edgeAction = edgeAction;
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
    ///                                                                                                                                                                                                                                                                                                                                                                                                                                            or if `dst` does not have a buffer of type [DataBufferFloat].
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
        src = FloatImages.convertImage(src, dst.getColorModel(), null);

        var in = (DataBufferFloat) src.getRaster().getDataBuffer();
        var out = (DataBufferFloat) dst.getRaster().getDataBuffer();

        int width = src.getWidth();
        int height = src.getHeight();
        for (int bank = 0, n = in.getNumBanks(); bank < n; bank++) {
            convolve(kernel, in.getData(bank), out.getData(bank), width, height);
        }
        return dst;
    }

    private void convolve(Kernel kernel, float[] inPixels, float[] outPixels, int width, int height) {
        if (kernel.getHeight() == 1)
            convolveH(kernel, inPixels, outPixels, width, height);
        else if (kernel.getWidth() == 1)
            convolveV(kernel, inPixels, outPixels, width, height);
        else
            convolveHV(kernel, inPixels, outPixels, width, height);
    }

    private void convolveHV(Kernel kernel, float[] inPixels, float[] outPixels, int width, int height) {
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int cols = kernel.getWidth();
        int rows2 = rows / 2;
        int cols2 = cols / 2;

        //for (int y = 0; y < height; y++) {
        IntStream.range(0, height).parallel().forEach(y -> {
            int yindex = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0;
                for (int row = -rows2; row <= rows2; row++) {
                    int iy = edgeAction.map(y + row, height);
                    int ioffset = iy * width;
                    int moffset = cols * (row + rows2) + cols2;
                    for (int col = -cols2; col <= cols2; col++) {
                        float f = matrix[moffset + col];
                        int ix = edgeAction.map(x + col, width);
                        r = Math.fma(f, inPixels[ioffset + ix], r);
                    }
                }
                outPixels[yindex + x] = clamp(r, 0f, 1f);
            }
        });
        //}

    }

    private void convolveH(Kernel kernel, float[] inPixels, float[] outPixels, int width, int height) {
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        //for (int y = 0; y < height; y++) {
        IntStream.range(0, height).parallel().forEach(y -> {
            int yindex = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    int ix = edgeAction.map(x + col, width);
                    r = Math.fma(f, inPixels[yindex + ix], r);
                }
                outPixels[yindex + x] = clamp(r, 0f, 1f);
            }
        });
        //}
    }

    private void convolveV(Kernel kernel, float[] inPixels, float[] outPixels, int width, int height) {
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int rows2 = rows / 2;

        //for (int y = 0; y < height; y++) {
        IntStream.range(0, height).parallel().forEach(y -> {
            int yindex = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0;
                for (int row = -rows2; row <= rows2; row++) {
                    int iy = edgeAction.map(y + row, height);
                    int ioffset = iy * width;
                    float f = matrix[row + rows2];
                    r = Math.fma(f, inPixels[ioffset + x], r);
                }
                outPixels[yindex + x] = clamp(r, 0f, 1f);
            }
        });
        //}
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
}
