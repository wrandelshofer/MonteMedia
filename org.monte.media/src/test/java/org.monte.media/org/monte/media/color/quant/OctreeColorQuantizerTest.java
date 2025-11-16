/*
 * @(#)OctreeColorQuantizerTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.quant;

import org.junit.jupiter.api.Test;
import org.monte.media.image.TestImageFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OctreeColorQuantizerTest {
    int K = 256;

    @Test
    public void shouldGeneratePalette() throws IOException {
        BufferedImage src = TestImageFactory.createSRgbFaces();

        var q = new OctreeColorQuantizer(K);
        q.addImage(src);
        var icm = q.computeColorPalette();

        int height = src.getHeight();
        int width = src.getWidth();
        BufferedImage dst = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_INDEXED, icm);
        int[] rgbArray = new int[width * height];
        src.getRGB(0, 0, width, height, rgbArray, 0, width);
        byte[] dstArray = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        for (int i = 0, n = Math.min(rgbArray.length, dstArray.length); i < n; i++) {
            dstArray[i] = (byte) q.quant(rgbArray[i]);
        }

        assertEquals(K, icm.getMapSize(), "must generate K=" + K + " colors");

        boolean success = ImageIO.write(dst, "PNG", new File("target/quant-octree-output.png"));
        assertTrue(success);
    }
}