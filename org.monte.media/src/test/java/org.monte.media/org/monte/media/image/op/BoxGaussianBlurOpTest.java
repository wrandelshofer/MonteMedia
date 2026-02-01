/*
 * @(#)GaussianBlurOpTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.image.TestImageFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BoxGaussianBlurOpTest {

    @ParameterizedTest
    @ValueSource(floats = {4, 10})
    public void shouldConvolveImageUsingGaussian(float sigma) throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();

        var op = new BoxGaussianBlurOp(sigma);
        var dest = op.filter(src, null);
        BufferedImage destSRgb = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = destSRgb.createGraphics();
        g.drawImage(dest, 0, 0, null);
        g.dispose();

        boolean success = ImageIO.write(destSRgb, "PNG", new File("target/BoxGaussianBlurOp-" + sigma + ".png"));
    }
}