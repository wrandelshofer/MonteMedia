/*
 * @(#)GammaToneMapperTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GammaToneMapperTest {
    @ParameterizedTest
    @CsvSource({
            // name gamma a b c d
            "srgb,2.4f, 1.055f, 0.055f, 12.92f, 0.04045f",
            "a98,2.19921875,1,0,1,0"
    })
    public void shouldMapBijectiveForSRGB(String name, float gamma, float a, float b, float c, float d) {
        assertBijective(new GammaToneCurve(gamma, a, b, c, d));
    }


    private void assertBijective(GammaToneCurve curve) {
        var m = new GammaToneMapper(curve);
        for (int i = 0; i < 1000; i++) {
            float x = i / 1000f;
            float y = m.toLinear(0, x);
            float xx = m.fromLinear(0, y);
            assertEquals(x, xx, 1e-4f, "i=" + i);

        }
    }
}