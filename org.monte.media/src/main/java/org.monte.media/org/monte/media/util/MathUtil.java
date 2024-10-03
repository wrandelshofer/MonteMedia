/*
 * @(#)MathUtil.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

/**
 * Math utilities.
 */
public class MathUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private MathUtil() {

    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static double clamp(double v, double min, double max) {
        return v < min ? min : v > max ? max : v;
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static float clamp(float v, float min, float max) {
        return v < min ? min : v > max ? max : v;
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static int clamp(int v, int min, int max) {
        return v < min ? min : v > max ? max : v;
    }
}
