/*
 * @(#)GammaToneMapperTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.trc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GammaToneMapperTest {
    @Test
    public void shouldMapBijectiveForSRGB() {
        assertBijective(new GammaToneCurve(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f));
    }


    private void assertBijective(GammaToneCurve curve) {
        var m = new GammaToneMapper(curve);
        for (int i = 0; i < 1000; i++) {
            float x = i / 1000f;
            float y = m.toLinear(x);
            float xx = m.fromLinear(y);
            assertEquals(x, xx, 1e-4f, "i=" + i);

        }
    }
}