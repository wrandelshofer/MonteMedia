/*
 * @(#)ParametricToneMapperTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParametricToneMapperTest {
    @Test
    public void shouldMapBijectiveForSRGB() {
        assertBijective(new ParametricToneCurve(2.4f, 0.9478607f, 0.052139282f, 0.07739258f, 0.04045f));
    }

    @Test
    public void shouldMapBijectiveForRec709() {
        assertBijective(new ParametricToneCurve(2.22222f, 0.9099121f, 0.09008789f, 0.22222f, 0.08099365f));
    }

    private void assertBijective(ParametricToneCurve curve) {
        var m = new ParametricToneMapper(curve);
        for (int i = 0; i < 1000; i++) {
            float x = i / 1000f;
            float y = m.curvedToLinear(0, x);
            float xx = m.linearToCurved(0, y);
            assertEquals(x, xx, 1e-4f, "i=" + i);
        }
    }

    @Test
    public void shouldBeTheSameAsSrgb() {
        var pc = new ParametricToneCurve(2.4f, 1 / 1.055f, 0.055f / 1.055f, 1 / 12.92f, 0.04045f);
        var gc = new GammaToneCurve(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f);
        var pm = new ParametricToneMapper(pc);
        var gm = new GammaToneMapper(gc);
        for (int i = 0; i < 1000; i++) {
            float x = i / 1000f;
            float y = x;
            float py = pm.curvedToLinear(0, x);
            float gy = gm.curvedToLinear(0, x);
            float px = pm.linearToCurved(0, y);
            float gx = gm.linearToCurved(0, y);
            assertEquals(py, gy, 1e-4f, "i=" + i);
            assertEquals(px, gx, 1e-4f, "i=" + i);
        }

    }
}