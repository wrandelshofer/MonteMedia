/*
 * @(#)RationalTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RationalTest {

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

    @TestFactory
    public List<DynamicTest> dynamicTestMultiplication() {
        return Arrays.asList(
                dynamicTest("0", () -> testMultiplication(Rational.valueOf(1074236, 11025), Rational.valueOf(1000),
                        Rational.valueOf(1074236 * 1000, 11025))),
                dynamicTest("1", () -> testMultiplication(Rational.valueOf(58592367, 601), Rational.valueOf(1000),
                        Rational.valueOf(58592367 * 1000L, 601)))
        );
    }

    void testMultiplication(Rational a, Rational b, Rational expected) {
        Rational actual = a.multiply(b);
        System.out.println(a.doubleValue() + " * " + b.doubleValue() + " = " + actual.doubleValue());
        assertEquals(expected, actual);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestDivision() {
        return Arrays.asList(
                dynamicTest("0", () -> testDivision(Rational.valueOf(42874287, 440), Rational.valueOf(1000),
                        Rational.valueOf(42874287, 440 * 1000))),
                dynamicTest("1", () -> testDivision(Rational.valueOf(58592367, 601), Rational.valueOf(1000),
                        Rational.valueOf(58592367, 601 * 1000))),
                dynamicTest("2", () -> testDivision(Rational.valueOf(14219000, 147), Rational.valueOf(1000),
                        Rational.valueOf(14219, 147)))
        );
    }

    void testDivision(Rational a, Rational b, Rational expected) {
        Rational actual = a.divide(b);
        System.out.println(a + " / " + b + " = " + actual);
        System.out.println(a.doubleValue() + " / " + b.doubleValue() + " = " + actual.doubleValue());
        assertEquals(expected, actual);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestDivisionWithLong() {
        return Arrays.asList(
                dynamicTest("0", () -> testDivisionWithLong(Rational.valueOf(42874287, 440), 1000,
                        Rational.valueOf(42874287, 440 * 1000))),
                dynamicTest("1", () -> testDivisionWithLong(Rational.valueOf(58592367, 601), 1000,
                        Rational.valueOf(58592367, 601 * 1000))),
                dynamicTest("2", () -> testDivisionWithLong(Rational.valueOf(14219000, 147), 1000,
                        Rational.valueOf(14219, 147)))
        );
    }

    void testDivisionWithLong(Rational a, long b, Rational expected) {
        Rational actual = a.divide(b);
        System.out.println(a + " / " + b + " = " + actual);
        System.out.println(a.doubleValue() + " / " + b + " = " + actual.doubleValue());
        assertEquals(expected, actual);
    }


    @TestFactory
    public List<DynamicTest> dynamicTestToDouble() {
        return Arrays.asList(
                dynamicTest("0", () -> testToDouble(Rational.valueOf(42874287, 440 * 1000))),
                dynamicTest("1", () -> testToDouble(Rational.valueOf(601000, 58592367)))
        );
    }

    void testToDouble(Rational a) {
        double expected = a.getNumerator() / (double) a.getDenominator();
        double actual = a.doubleValue();
        assertEquals(expected, actual);
    }


    @ParameterizedTest
    @ValueSource(doubles = {97436.37188208617})
    public void shouldConvertFromDouble(double d) {
        var r = Rational.valueOf(d);
        var actual = r.doubleValue();
        assertEquals(d, actual);

    }
}
