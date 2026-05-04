/*
 * @(#)BayerDitherTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlueNoiseDither128Test {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    public void testSize(int channel) {
        float spread = 1;
        checkMinMaxSum(channel, spread);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 8})
    public void testSpread(int spread) {
        checkMinMaxSum(0, spread);
    }


    private static void checkMinMaxSum(int channel, float spread) {
        BlueNoiseDither128 dither = new BlueNoiseDither128(channel, spread);
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
        assertEquals(0.0, sum, 1e-2, "sum");
    }
}
