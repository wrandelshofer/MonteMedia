/*
 * @(#)ColorSpaceImageOp.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.op;

import org.monte.media.color.NamedColorSpace;
import org.monte.media.color.SrgbColorSpace;
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
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.util.stream.IntStream;

/// Converts an image from a `src` color space to a `dst` color space.
/// Does not rescale colors if the dst color space is larger than src.
/// Clips out of gamut colors.
public class ColorSpaceConvertOp implements BufferedImageOp {

    private ColorSpace dstColorSpace;

    public ColorSpaceConvertOp() {
        this(null);
    }

    public ColorSpaceConvertOp(ColorSpace dstColorSpace) {
        this.dstColorSpace = dstColorSpace;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null && dstColorSpace == null) {
            return src;
        }


        //dst = srcToXyzToDestUsingFloatImages(src, dstColorSpace, dst);
        if ((dstColorSpace instanceof NamedColorSpace dcs || dstColorSpace.isCS_sRGB())
                && (src.getColorModel().getColorSpace() instanceof NamedColorSpace scs || src.getColorModel().getColorSpace().isCS_sRGB())) {
            dst = srcToXyzToDestWithNamedColorSpace(src, dstColorSpace, dst);
        } else {
            dst = srcToXyzToDest(src, dstColorSpace, dst);
        }

        return dst;
    }

    /// This is also the slowest conversion function
    private static BufferedImage srcToXyzToDestUsingFloatImages(BufferedImage src, ColorSpace cs, BufferedImage dst) {
        var cm = new ComponentColorModel(cs, src.getColorModel().hasAlpha(),
                src.getColorModel().isAlphaPremultiplied(), src.getColorModel().getTransparency(), DataBuffer.TYPE_FLOAT);
        return FloatImages.convertImage(src, cm, dst);
    }

    /// This is the slowest conversion function
    private BufferedImage srcToXyzToDest(BufferedImage src, ColorSpace dstColorSpace, BufferedImage dst) {
        dst = reuseDestImage(src, dstColorSpace, dst);
        var srcRas = src.getRaster();
        var dstRas = dst.getRaster();
        var srcCM = src.getColorModel();
        var dstCM = dst.getColorModel();
        var srcCS = srcCM.getColorSpace();
        var dstCS = dstCM.getColorSpace();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        IntStream.range(0, src.getHeight()).parallel().forEach(y -> {
            float[] dstColor = null;
            float[] dstColor4 = new float[4];
            Object srcPixel = null;
            Object dstPixel = null;
            float[] color4 = new float[4];
            float[] color = new float[4];
            float alpha = 0;
            //for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                srcPixel = srcRas.getDataElements(x, y, srcPixel);
                color = srcCM.getNormalizedComponents(srcPixel, color4, 0);
                if (needSrcAlpha) {
                    alpha = color[srcNumComp];
                }
                color = srcCS.toCIEXYZ(color);
                dstColor = dstCS.fromCIEXYZ(color);
                System.arraycopy(dstColor, 0, dstColor4, 0, 3);
                if (needSrcAlpha) {
                    dstColor4[3] = alpha;
                } else if (dstHasAlpha) {
                    dstColor4[3] = 1.0f;
                }
                dstPixel = dstCM.getDataElements(dstColor4, 0, dstPixel);
                dstRas.setDataElements(x, y, dstPixel);
            }
            //}
        });
        return dst;
    }

    private BufferedImage srcToXyzToDestWithNamedColorSpace(BufferedImage src, ColorSpace dstColorSpace, BufferedImage dst) {
        dst = reuseDestImage(src, dstColorSpace, dst);

        var srcRas = src.getRaster();
        var dstRas = dst.getRaster();
        var srcCM = src.getColorModel();
        var dstCM = dst.getColorModel();
        NamedColorSpace srcCS = srcCM.getColorSpace().isCS_sRGB() ? SrgbColorSpace.getInstance() : (NamedColorSpace) srcCM.getColorSpace();
        NamedColorSpace dstCS = dstCM.getColorSpace().isCS_sRGB() ? SrgbColorSpace.getInstance() : (NamedColorSpace) dstCM.getColorSpace();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        IntStream.range(0, src.getHeight()).parallel().forEach(y -> {
            float[] dstColor = null;
            float[] dstColor4 = new float[4];
            Object srcPixel = null;
            Object dstPixel = null;
            float[] color4 = new float[4];
            float[] color = new float[4];
            float[] xyz = new float[4];
            float alpha = 0;
            //for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                srcPixel = srcRas.getDataElements(x, y, srcPixel);
                color = srcCM.getNormalizedComponents(srcPixel, color4, 0);
                if (needSrcAlpha) {
                    alpha = color[srcNumComp];
                }
                xyz = srcCS.toCIEXYZ(color, xyz);
                dstColor4 = dstCS.fromCIEXYZ(xyz, dstColor4);
                if (needSrcAlpha) {
                    dstColor4[3] = alpha;
                } else if (dstHasAlpha) {
                    dstColor4[3] = 1.0f;
                }
                dstPixel = dstCM.getDataElements(dstColor4, 0, dstPixel);
                dstRas.setDataElements(x, y, dstPixel);
            }
            //}
        });
        return dst;
    }

    private static void filterUsingFallbackMethod(BufferedImage src, BufferedImage dst) {
        var g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
    }


    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle2D.Double(0, 0, src.getWidth(), src.getHeight());
    }

    private BufferedImage reuseDestImage(BufferedImage src, ColorSpace cs, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        var srcCM = src.getColorModel();
        if (dest != null && dest.getWidth() == width && dest.getHeight() == height
                && dest.getSampleModel() instanceof BandedSampleModel bsm
                && bsm.getNumBands() == src.getColorModel().getNumComponents()
                && dest.getColorModel().getColorSpace() == cs) {
            return dest;
        }


        // The color model must be alpha premultiplied if it has alpha!
        ComponentColorModel destCM = new ComponentColorModel(cs, srcCM.hasAlpha(), srcCM.hasAlpha(),
                srcCM.getTransparency(), DataBuffer.TYPE_FLOAT);

        SampleModel destSampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT,
                width, height, destCM.getNumComponents());
        dest = new BufferedImage(destCM, Raster.createWritableRaster(
                destSampleModel, new DataBufferFloat(width * height, destCM.getNumComponents()),
                new Point(0, 0)), destCM.isAlphaPremultiplied(), null);
        return dest;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), destCM.isAlphaPremultiplied(), null);
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }
}
