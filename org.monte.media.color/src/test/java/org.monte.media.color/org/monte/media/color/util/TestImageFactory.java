/*
 * @(#)TestImageFactory.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.util;

import org.junit.jupiter.api.Test;
import org.monte.media.image.FloatImages;

import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.function.ToDoubleBiFunction;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestImageFactory {
    /// Creates an image with a high-frequency pattern.
    ///
    /// @return test image
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

    /// Creates an image with the 6 faces of an sRGB cube.
    ///
    /// @return test image
    public static BufferedImage createRgbCubeFaces() {
        return createRgbCubeFaces(ColorSpace.getInstance(ColorSpace.CS_sRGB), 8);
    }

    /// Creates an image in the specified color space using a [DirectColorModel] of
    /// [DataBuffer#TYPE_BYTE],[DataBuffer#TYPE_SHORT],[DataBuffer#TYPE_INT] with the
    /// specified number of bits per component.
    ///
    /// @param cs               the color space
    /// @param width            image width
    /// @param height           image height
    /// @param bitsPerComponent number of bits per component (specify 8 to get a 24-bit RGB image)
    public static BufferedImage createImage(ColorSpace cs, int width, int height, int bitsPerComponent) {
        int mask = (1 << bitsPerComponent) - 1;
        int transferType = (bitsPerComponent * 4 <= 8) ? DataBuffer.TYPE_BYTE : (bitsPerComponent * 4 <= 16) ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_INT;
        ColorModel cm = switch (cs.getType()) {
            case ColorSpace.TYPE_RGB ->
                    new DirectColorModel(cs, Math.min(32, bitsPerComponent * 4), mask << bitsPerComponent * 2, mask << bitsPerComponent,
                            mask, 0, false,
                            transferType);
            default -> new ComponentColorModel(cs, false, false, Transparency.OPAQUE, transferType);
        };
        var img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        return img;
    }

    public static BufferedImage createRgbCubeFaces(ColorSpace cs, int bitDepth) {
        int size = 1 << bitDepth;
        int height = size * 2;
        int width = size * 3;
        var img = createImage(cs, width, height, bitDepth);
        return createRgbCubeFaces(img, bitDepth);
    }

    public static BufferedImage createRgbCubeFaces(BufferedImage img, int bitDepth) {
        int size = 1 << bitDepth;
        int height = size * 2;
        int width = size * 3;
        DirectColorModel cm = (DirectColorModel) img.getColorModel();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                setRGB(img, x, y, x, y, size - 1, cm);
                setRGB(img, x + size, y, x, size - 1, y, cm);
                setRGB(img, x + size * 2, y, size - 1, x, y, cm);
                setRGB(img, x, y + size, x, y, 0, cm);
                setRGB(img, x + size, y + size, x, 0, y, cm);
                setRGB(img, x + size * 2, y + size, 0, x, y, cm);
            }
        }
        return img;
    }

    private static void setRGB(BufferedImage img, int x, int y, int r, int g, int b, DirectColorModel cm) {
        WritableRaster raster = img.getRaster();
        SampleModel sampleModel = raster.getSampleModel();
        sampleModel.setSample(x, y, 0, r, raster.getDataBuffer());
        sampleModel.setSample(x, y, 1, g, raster.getDataBuffer());
        sampleModel.setSample(x, y, 2, b, raster.getDataBuffer());
        // sampleModel.setSample(x, y, 3, 255, raster.getDataBuffer());
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
        BufferedImage dst = TestImageFactory.createRgbCubeFaces();
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/testimage-rgb-faces.png"));
        assertTrue(success);
    }

    public static ICC_Profile createRec2020Profile() {
        byte[] data = {0, 0, 2, 44, 97, 112, 112, 108, 4, 0, 0, 0, 109, 110, 116, 114,
                82, 71, 66, 32, 88, 89, 90, 32, 7, -25, 0, 6, 0, 9, 0, 9,
                0, 54, 0, 38, 97, 99, 115, 112, 65, 80, 80, 76, 0, 0, 0, 0,
                65, 80, 80, 76, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, -10, -42, 0, 1, 0, 0, 0, 0, -45, 45,
                97, 112, 112, 108, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 10, 100, 101, 115, 99, 0, 0, 0, -4, 0, 0, 0, 68,
                99, 112, 114, 116, 0, 0, 1, 64, 0, 0, 0, 80, 119, 116, 112, 116,
                0, 0, 1, -112, 0, 0, 0, 20, 114, 88, 89, 90, 0, 0, 1, -92,
                0, 0, 0, 20, 103, 88, 89, 90, 0, 0, 1, -72, 0, 0, 0, 20,
                98, 88, 89, 90, 0, 0, 1, -52, 0, 0, 0, 20, 114, 84, 82, 67,
                0, 0, 1, -32, 0, 0, 0, 32, 99, 104, 97, 100, 0, 0, 2, 0,
                0, 0, 0, 44, 98, 84, 82, 67, 0, 0, 1, -32, 0, 0, 0, 32,
                103, 84, 82, 67, 0, 0, 1, -32, 0, 0, 0, 32, 109, 108, 117, 99,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 12, 101, 110, 85, 83,
                0, 0, 0, 40, 0, 0, 0, 28, 0, 82, 0, 101, 0, 99, 0, 46,
                0, 32, 0, 73, 0, 84, 0, 85, 0, 45, 0, 82, 0, 32, 0, 66,
                0, 84, 0, 46, 0, 50, 0, 48, 0, 50, 0, 48, 0, 45, 0, 49,
                109, 108, 117, 99, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 12,
                101, 110, 85, 83, 0, 0, 0, 52, 0, 0, 0, 28, 0, 67, 0, 111,
                0, 112, 0, 121, 0, 114, 0, 105, 0, 103, 0, 104, 0, 116, 0, 32,
                0, 65, 0, 112, 0, 112, 0, 108, 0, 101, 0, 32, 0, 73, 0, 110,
                0, 99, 0, 46, 0, 44, 0, 32, 0, 50, 0, 48, 0, 50, 0, 51,
                88, 89, 90, 32, 0, 0, 0, 0, 0, 0, -10, -42, 0, 1, 0, 0,
                0, 0, -45, 45, 88, 89, 90, 32, 0, 0, 0, 0, 0, 0, -84, 105,
                0, 0, 71, 111, -1, -1, -1, -127, 88, 89, 90, 32, 0, 0, 0, 0,
                0, 0, 42, 105, 0, 0, -84, -29, 0, 0, 7, -83, 88, 89, 90, 32,
                0, 0, 0, 0, 0, 0, 32, 3, 0, 0, 11, -83, 0, 0, -53, -2,
                112, 97, 114, 97, 0, 0, 0, 0, 0, 3, 0, 0, 0, 2, 56, -28,
                0, 0, -24, -32, 0, 0, 23, 32, 0, 0, 56, -28, 0, 0, 20, -68,
                115, 102, 51, 50, 0, 0, 0, 0, 0, 1, 12, 66, 0, 0, 5, -34,
                -1, -1, -13, 38, 0, 0, 7, -109, 0, 0, -3, -112, -1, -1, -5, -94,
                -1, -1, -3, -93, 0, 0, 3, -36, 0, 0, -64, 110};
        return ICC_Profile.getInstance(data);
    }
}
