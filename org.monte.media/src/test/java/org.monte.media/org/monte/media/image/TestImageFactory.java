/*
 * @(#)TestImageFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferFloat;
import java.io.File;
import java.io.IOException;
import java.util.function.ToDoubleBiFunction;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestImageFactory {
    /**
     * Creates an image with a high-frequency pattern.
     *
     * @return test image
     */
    public static BufferedImage createHighFrequencyTestImage() {
        int size = 256;
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        var img = FloatImages.reuseDestImage(null, size, size, cs);
        double factor = 3.5 * Math.PI / size;
        var f = (ToDoubleBiFunction<Double, Double>) (x, y) -> {
            double xn = x * factor;
            double yn = y * factor;
            double r = sqrt(xn * xn + yn * yn);
            return cos(r * r);
        };

        var buf = (DataBufferFloat) img.getRaster().getDataBuffer();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float v = Math.clamp((float) f.applyAsDouble((double) x, (double) y), 0f, 1f);
                for (int b = 0; b < buf.getNumBanks(); b++) {
                    buf.getData(b)[y * size + x] = v;
                }
            }
        }
        return img;
    }

    /**
     * Creates an image with the 6 faces of an sRGB cube.
     *
     * @return test image
     */
    public static BufferedImage createSRgbFaces() {
        int size = 256;
        var img = new BufferedImage(size * 3, size * 2, BufferedImage.TYPE_INT_RGB);
        var r = img.getRaster();
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                img.setRGB(x, y, (x << 16) | (y << 8) | 255);
                img.setRGB(x + size, y, (x << 16) | (255 << 8) | (y));
                img.setRGB(x + size * 2, y, (255 << 16) | (x << 8) | (y));
                img.setRGB(x, y + size, (x << 16) | (y << 8));
                img.setRGB(x + size, y + size, (x << 16) | (y));
                img.setRGB(x + size * 2, y + size, (x << 8) | (y));
            }
        }
        return img;
    }

    @Test
    public void shouldCreateHighFrequencyTestPattern() throws IOException {
        BufferedImage dst = TestImageFactory.createHighFrequencyTestImage();
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/testimage-high-frequency.png"));
        assertTrue(success);
    }

    @Test
    public void shouldCreateSRgbFacesTestPattern() throws IOException {
        BufferedImage dst = TestImageFactory.createSRgbFaces();
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/testimage-rgb-faces.png"));
        assertTrue(success);
    }
}
