/*
 * @(#)ICC_ProfileReaderTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.color.io.ColorManagedImageReader;

import java.awt.color.ICC_ColorSpace;
import java.io.File;

public class ICC_ProfileReaderTest {
    @ParameterizedTest
    @ValueSource(strings = {"rgb-faces-sRGB.png", "rgb-faces-DisplayP3.png", "rgb-faces-Rec709.png", "rgb-faces-Rec2020.png"})
    public void testRead(String filename) throws Exception {
        File file = new File("/Volumes/Projects/Java/MonteMedia/ExampleImages/" + filename);
        IO.println("Reading " + filename);
        var image = new ColorManagedImageReader().read(file);
        var cs = image.getColorModel().getColorSpace();
        if (cs instanceof ICC_ColorSpace iccs) {
            IO.println(ICC_ProfileReader.toString(iccs.getProfile()));
        }
        IO.println(ICC_ColorSpaceReader.toString((ICC_ColorSpace) cs));
    }
}