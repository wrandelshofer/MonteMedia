/*
 * @(#)Rec709ColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.image.TestImageFactory;

import java.awt.image.DataBufferInt;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rec709ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new Rec709ColorSpace();
    }

    @Test
    public void shouldGenerateRec709CubeFacesWithValuesInRange() throws IOException {
        var src = TestImageFactory.createRec709Faces();
        //boolean success = ImageIO.write(src, "PNG", new File("target/rec-709.png"));

        // The pixel values must be in range 16 to 235
        var pixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int red = (p & 0xff0000) >>> 16;
            int green = (p & 0xff00) >>> 8;
            int blue = (p & 0xff);
            assertTrue(16 <= red && red <= 235, "red is out of range, rgb=" + red + "," + green + "," + blue);
            assertTrue(16 <= green && green <= 235, "green is out of range, rgb=" + red + "," + green + "," + blue);
            assertTrue(16 <= blue && blue <= 235, "blue is out of range, rgb=" + red + "," + green + "," + blue);
        }

    }
}