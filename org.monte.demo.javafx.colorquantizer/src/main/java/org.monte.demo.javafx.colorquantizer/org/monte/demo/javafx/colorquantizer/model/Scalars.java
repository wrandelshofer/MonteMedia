/*
 * @(#)Scalars.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.model;

public class Scalars {
    public static final double REAL_THRESHOLD = 1e-8;

    /**
     * Don't let anyone instantiate this class.
     */
    private Scalars() {
    }

    public static boolean almostEqual(double a, double b) {
        return almostEqual(a, b, REAL_THRESHOLD);
    }

    public static boolean almostEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean almostZero(double a) {
        return almostZero(a, REAL_THRESHOLD);
    }

    public static boolean almostZero(double a, double epsilon) {
        return Math.abs(a) < epsilon;
    }
}
