/*
 * @(#)BayerDitherTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BayerDitherTest {

    @ParameterizedTest
    @ValueSource(ints = {4, 8})
    public void testSize(int size) {
        float spread = 1;
        checkMinMaxSum(size, spread);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 8})
    public void testSpread(int spread) {
        checkMinMaxSum(4, spread);
    }


    private static void checkMinMaxSum(int size, float spread) {
        BayerDither dither = new BayerDither(size, spread);
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        double sum = 0;
        int w = dither.getWidth();
        int h = dither.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = dither.get(x, y);
                min = Math.min(min, v);
                max = Math.max(max, v);
                sum += v;
            }
        }
        assertEquals(-spread, min, 1e-3, "min");
        assertEquals(spread, max, 1e-3, "max");
        assertEquals(0.0, sum, 1e-3, "sum");
    }
}
