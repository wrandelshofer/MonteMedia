/*
 * @(#)ScaleOpFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

import org.junit.jupiter.api.Test;
import org.monte.media.image.algo.BilinearInterpolationResampleAlgoFloat;
import org.monte.media.image.op.GaussianKernelFactory;
import org.monte.media.image.op.ScaleOp;
import org.monte.media.image.op.UnsharpMaskOp;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleOpFloatTest {
    int dstWidth = 200;
    int dstHeight = 100;
    private final float blurRadiusFactor = 0.75f;
    private final float sharpenRadius = 1.35f;
    private final float sharpenAmount = 0.5f;
    private final float sharpenThreshold = 2.5f / 256f;

    @Test
    public void shouldDownscaleImageUsingScaleOp() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        var op = new ScaleOp(src.getWidth(), src.getHeight(), dstWidth, dstHeight, blurRadiusFactor,
                new GaussianKernelFactory(),
                //new LanczosKernelFactory(),
                new BilinearInterpolationResampleAlgoFloat());
        //new NearestNeighbourResampleAlgoFloat());
        var dst = op.filter(src, null);

        var op2 = new UnsharpMaskOp(sharpenRadius, sharpenAmount, sharpenThreshold);
        dst = op2.filter(dst, null);

        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/downscale-output.png"));
        assertTrue(success);

    }

    @Test
    public void shouldDownscaleImageUsingAwt() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        BufferedImage srcRgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = srcRgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        double sx = dstWidth / (double) src.getWidth();
        double sy = dstHeight / (double) src.getHeight();
        var op = new AffineTransformOp(AffineTransform.getScaleInstance(sx, sy), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        var dst = op.filter(srcRgb, null);

        boolean success = ImageIO.write(dst, "PNG", new File("target/downscale-output-awt.png"));
        assertTrue(success);

    }
}