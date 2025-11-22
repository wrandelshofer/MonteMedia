/*
 * @(#)ScaleOpFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.api.Test;
import org.monte.media.image.TestImageFactory;
import org.monte.media.image.algo.BilinearInterpolationResampleAlgoFloat;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SlowScaleOpTest {
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
        var op = new SlowScaleOp(src.getWidth(), src.getHeight(), dstWidth, dstHeight, blurRadiusFactor,
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
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/SlowScaleOp-output.png"));
        assertTrue(success);

    }
}