/*
 * @(#)RationalTest.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class RationalTest {

    void testFloor(Rational a, int b, Rational expected) {
        assertEquals(expected, a.floor(b));
    }

    @TestFactory
    public List<DynamicTest> dynamicTestFloor() {
        return Arrays.asList(
                dynamicTest("0", () -> testFloor(Rational.valueOf(10, 60), 60, Rational.valueOf(1, 6))),
                dynamicTest("1", () -> testFloor(Rational.valueOf(10, 70), 60, Rational.valueOf(2, 15)))
        );
    }
}
