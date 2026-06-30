/*
 * @(#)ICC_ProfileBuilderTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.junit.jupiter.api.Test;
import org.monte.media.color.tonecurve.GammaToneCurve;
import org.monte.media.color.tonecurve.ParametricToneCurve;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ICC_ProfileBuilderTest {
    @Test
    public void shouldBuildProfile() throws IOException {
        var w = new ICC_ProfileBuilder();

        float[] white = {0.9504547f, 1.0f, 1.0890503f};
        w.setWhiteXYZ(white);
        float[] black = {0, 0, 0};
        w.setBlackXYZ(black);
        float[] red = {0.6097412f, 0.31111145f, 0.019470215f};
        w.setRedXYZ(red);
        float[] green = {0.20527649f, 0.6256714f, 0.06086731f};
        w.setGreenXYZ(green);
        float[] blue = {0.14918518f, 0.06321716f, 0.7445679f};
        w.setBlueXYZ(blue);

        w.setWhiteXYZ(0.95f, 1f, 1.089f);
        w.setRedXYZ(0.61f, 0.311f, 0.019f);
        w.setGreenXYZ(0.205f, 0.626f, 0.061f);
        w.setBlueXYZ(0.149f, 0.063f, 0.745f);
        var toneCurve = new GammaToneCurve(2.1992188f);
        new ParametricToneCurve(toneCurve);
        w.setGreenToneCurve(toneCurve);
        w.setBlueToneCurve(toneCurve);
        w.setRedToneCurve(toneCurve);

        var profile = w.build();
        var r = new ICC_ProfileReader(profile);

        if (profile instanceof ICC_ProfileRGB) {
            ICC_ProfileRGB profileRGB = (ICC_ProfileRGB) profile;
            float eps = 1e-2f;
            assertArrayEquals(white, profileRGB.getMediaWhitePoint(), eps, "white");
            assertEquals(2, profileRGB.getMajorVersion(), "major version");
            assertEquals(16, profileRGB.getMinorVersion(), "minor version");
            assertArrayEquals(red, (float[]) r.getTag(ICC_Profile.icSigRedColorantTag), eps, "red");
            assertArrayEquals(green, (float[]) r.getTag(ICC_Profile.icSigGreenColorantTag), eps, "green");
            assertArrayEquals(blue, (float[]) r.getTag(ICC_Profile.icSigBlueColorantTag), eps, "blue");
            assertEquals(toneCurve.gamma(), ((ParametricToneCurve) r.getTag(ICC_Profile.icSigRedTRCTag)).gamma(), eps, "red TRC");
            assertEquals(toneCurve.gamma(), ((ParametricToneCurve) r.getTag(ICC_Profile.icSigRedTRCTag)).gamma(), eps, "green TRC");
            assertEquals(toneCurve.gamma(), ((ParametricToneCurve) r.getTag(ICC_Profile.icSigRedTRCTag)).gamma(), eps, "blue TRC");
        }

        assertEquals("RGB ", r.getColorSpaceOfData(), "color space of data");
        assertEquals("XYZ ", r.getConnectionSpace(), "connection space");
        assertArrayEquals(new float[]{0.9642176f, 1.0000153f, 0.824918f}, r.getIlluminantD50(), 1e-2f, "Illuminant D50");

        var cs = new ICC_ColorSpace(profile);
        System.out.println(cs);
        var expectedRed = new float[]{0.85815215f, 0.026413366f, 0.029678797f};
        var actualRed = cs.fromRGB(new float[]{1.0f, 0.0f, 0.0f});
        assertArrayEquals(expectedRed, actualRed, "red");
    }

}