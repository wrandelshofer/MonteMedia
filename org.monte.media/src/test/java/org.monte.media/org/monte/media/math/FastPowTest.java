/*
 * @(#)FastPowTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastPowTest {

    @ParameterizedTest
    @ValueSource(doubles = {2.4})
    public void shouldApproximateMathPowInRange0To1(double p) {
        var error = 0.02;//2%
        for (int i = 0; i < 256; i++) {
            double v = i / 255.0;
            var expected = Math.pow(v, p);
            var actual = fastPow(v, p);
            assertEquals(expected, actual, error, "i=" + i + ", v=" + v + ",p=" + p);
        }
    }

    /// Pow approximation with exponentiation by squaring.
    ///
    /// This has 1.7% average error, no matter how large the exponent gets.
    ///
    /// Martinus. Fast approximation to Math.pow()
    /// : [reddit.com](https://www.reddit.com/r/gamedev/comments/n7na0/fast_approximation_to_mathpow/)
    /// : [pastebin.com](https://pastebin.com/ZW95gEyr)
    public static double fastPow(final double a, final double b) {
        // exponentiation by squaring
        double r = 1.0;
        int exp = (int) b;
        double base = a;
        while (exp != 0) {
            if ((exp & 1) != 0) {
                r *= base;
            }
            base *= base;
            exp >>= 1;
        }

        // use the IEEE 754 trick for the fraction of the exponent
        final double b_faction = b - (int) b;
        final long tmp = Double.doubleToLongBits(a);
        final long tmp2 = (long) (b_faction * (tmp - 4606921280493453312L)) + 4606921280493453312L;
        return r * Double.longBitsToDouble(tmp2);
    }
}
