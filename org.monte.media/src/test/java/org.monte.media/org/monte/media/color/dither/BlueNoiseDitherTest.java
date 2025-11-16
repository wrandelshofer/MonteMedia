/*
 * @(#)BlueNoiseDitherTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.DoubleSummaryStatistics;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueNoiseDitherTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    public void shouldBeNormalized(int channel) {
        var d = new BlueNoiseDither(channel, 1.0f).getData();
        DoubleSummaryStatistics acc = new DoubleSummaryStatistics();
        for (int y = 0; y < d.length; y++) {
            for (int x = 0; x < d[y].length; x++) {
                acc.accept(d[y][x]);
            }
        }
        System.out.println(acc);
        assertTrue(abs(acc.getSum()) < 0.001, "Sum must be close to zero, sum=" + acc.getSum());
        assertTrue(abs(acc.getAverage()) < 0.001, "Average must be close to zero, avg=" + acc.getAverage());
        assertTrue(abs(acc.getMin() + 1) < 0.001, "Min must be close to -1, min=" + acc.getMin());
        assertTrue(abs(acc.getMax() - 1) < 0.001, "Max must be close to +1,max=" + acc.getMax());
    }

}