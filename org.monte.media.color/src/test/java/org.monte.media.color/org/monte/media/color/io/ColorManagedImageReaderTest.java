/*
 * @(#)ColorManagedImageReaderTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.io;

import org.junit.jupiter.api.Test;
import org.monte.media.color.util.TestImageFactory;

import java.awt.Transparency;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorManagedImageReaderTest {
    @Test
    public void shouldChangeColorsIfWeUseADifferentColorSpace() throws Exception {
        // GIVEN A source image in sRGB
        BufferedImage imgSRGB = TestImageFactory.createRgbCubeFaces();
        assertEquals(0xffff0000, imgSRGB.getRGB(255, 256), "red pixel");

        // GIVEN A target image in another color space
        ICC_ColorSpace cs = new ICC_ColorSpace(TestImageFactory.createRec2020Profile());
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        int w = imgSRGB.getWidth();
        int h = imgSRGB.getHeight();
        WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
        BufferedImage imgRec2020 = new BufferedImage(cm, raster, false, null);

        // WHEN We draw the source image into the drawing image
        imgRec2020.getGraphics().drawImage(imgSRGB, 0, 0, null);
        // THEN A red pixel should look different
        assertEquals(0xffff0000, imgRec2020.getRGB(255, 256), "red pixel");//NOOO!!

        // WHEN We set sRGB pixels on the target image
        var red = cs.fromRGB(new float[]{1f, 0, 0});
        assertArrayEquals(new float[]{0.7918059f, 0.23089951f, 0.07376211f}, red, "red pixel");
        var array = imgSRGB.getRGB(0, 0, w, h, null, 0, w);
        imgRec2020.setRGB(0, 0, w, h, array, 0, w);
        // THEN A red pixel should look different
        assertEquals(0xffff0000, imgRec2020.getRGB(255, 256), "red pixel");//NOOO!!

        // WHEN We explicitly do a round trip over CIEXYZ
        var csSRGB = imgSRGB.getColorModel().getColorSpace();
        float[] rgbFloat = new float[3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = imgSRGB.getRGB(x, y);
                rgbFloat[0] = ((rgb & 0xff0000) >>> 16) / 255f;
                rgbFloat[1] = ((rgb & 0xff00) >>> 8) / 255f;
                rgbFloat[2] = ((rgb & 0xff)) / 255f;
                rgbFloat = csSRGB.toCIEXYZ(rgbFloat);
                rgbFloat = cs.fromCIEXYZ(rgbFloat);
                int rgbRec2020 = 0xff000000
                        | (Math.clamp((int) (rgbFloat[0] * 255), 0, 255) << 16)
                        | (Math.clamp((int) (rgbFloat[1] * 255), 0, 255) << 8)
                        | (Math.clamp((int) (rgbFloat[2] * 255), 0, 255));
                //  assertEquals(rgb, rgbRec2020, "x pixel " + Integer.toHexString(rgb) + " " + Integer.toHexString(rgbRec2020));
                imgRec2020.setRGB(x, y, rgbRec2020);
            }
        }
        // THEN A red pixel should look different
        assertEquals(0xff_c93a12, imgRec2020.getRGB(255, 256), "red pixel");//YES!
        /*for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgbRec2020 = imgRec2020.getRGB(x, y);
                rgbFloat[0] = ((rgbRec2020 & 0xff0000) >>> 16) / 255f;
                rgbFloat[1] = ((rgbRec2020 & 0xff00) >>> 8) / 255f;
                rgbFloat[2] = ((rgbRec2020 & 0xff)) / 255f;
                rgbFloat = cs.toCIEXYZ(rgbFloat);
                rgbFloat = csSRGB.fromCIEXYZ(rgbFloat);
                int rgb = 0xff000000
                        | (Math.clamp((int) (rgbFloat[0] * 255), 0, 255) << 16)
                        | (Math.clamp((int) (rgbFloat[1] * 255), 0, 255) << 8)
                        | (Math.clamp((int) (rgbFloat[2] * 255), 0, 255));
                //  assertEquals(rgb, rgbRec2020, "x pixel " + Integer.toHexString(rgb) + " " + Integer.toHexString(rgbRec2020));
                imgRec2020.setRGB(x, y, rgb);
            }
        }*/
        assertEquals(0xff_ff0000, imgSRGB.getRGB(255, 256), "red pixel");//YES!
    }


}

