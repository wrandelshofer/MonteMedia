/*
 * @(#)ColorSpaceImageOp.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.op;

import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
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
        if (dst == null) {
            ComponentColorModel cm = new ComponentColorModel(dstColorSpace, src.getColorModel().hasAlpha(), src.isAlphaPremultiplied(), src.getColorModel().getTransparency(), DataBuffer.TYPE_FLOAT);
            dst = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
        }

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
        if (dstHasAlpha) {
            int size = Math.max((dstNumComp + 1), 3);
        } else {
            int size = Math.max(dstNumComp, 3);
        }
        IntStream.range(0, src.getHeight()).parallel().forEach(y -> {
            float[] dstColor = null;
            Object srcPixel = null;
            Object dstPixel = null;
            float[] color = null;
            float alpha = 0;
            //for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                srcPixel = srcRas.getDataElements(x, y, srcPixel);
                color = srcCM.getNormalizedComponents(srcPixel, color, 0);
                if (needSrcAlpha) {
                    alpha = color[srcNumComp];
                }
                color = srcCS.toCIEXYZ(color);
                dstColor = dstCS.fromCIEXYZ(color);
                if (needSrcAlpha) {
                    dstColor[dstNumComp] = alpha;
                } else if (dstHasAlpha) {
                    dstColor[dstNumComp] = 1.0f;
                }
                dstPixel = dstCM.getDataElements(dstColor, 0, dstPixel);
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
