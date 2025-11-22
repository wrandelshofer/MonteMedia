/*
 * @(#)UnsharpMaskFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.api.Test;
import org.monte.media.image.TestImageFactory;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnsharpMaskOpTest {
    @Test
    public void shouldUnsharpMaskImageUsingUnsharpMaskOp() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        float s = 2f;
        float amount = 0.5f;
        float threshold = 1f / 255f;
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        UnsharpMaskOp op = new UnsharpMaskOp(s, amount, threshold, 0f, 1f);
        var dst = op.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        dst = op.filter(src, dst);

        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/UnsharpMaskOp-output.png"));
        assertTrue(success);
    }


}