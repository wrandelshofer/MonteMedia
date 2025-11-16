/*
 * @(#)AWTColorQuantizerTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.quant;

import org.junit.jupiter.api.Test;
import org.monte.media.image.TestImageFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AWTColorQuantizerTest {
    int K = 256;

    @Test
    public void shouldGeneratePalette() throws IOException {
        BufferedImage src = TestImageFactory.createSRgbFaces();

        boolean success = ImageIO.write(src, "GIF", new File("target/quant-output-awt.gif"));
        assertTrue(success);
    }
}