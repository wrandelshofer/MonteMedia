/*
 * @(#)IntMathTest.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IntMathTest {
    public void testGcd(int a, int b, int expected) {
        int actual = IntMath.gcd(a, b);
        assertEquals(expected, actual);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsGcd() {
        return Arrays.asList(
                dynamicTest("0", () -> testGcd(0, 0, 0)),
                dynamicTest("1", () -> testGcd(17, 34, 17)),
                dynamicTest("2", () -> testGcd(50, 49, 1))

        );
    }

}