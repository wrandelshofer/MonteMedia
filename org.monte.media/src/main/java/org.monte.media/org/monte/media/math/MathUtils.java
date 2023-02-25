/* @(#)MathUtils.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.math;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * {@code MathUtils}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MathUtils {

    public static float clamp(float value, float minValue, float maxValue) {
        return max(minValue, min(value, maxValue));

    }

    public static double clamp(double value, double minValue, double maxValue) {
        return max(minValue, min(value, maxValue));

    }

    public static int clamp(int value, int minValue, int maxValue) {
        return max(minValue, min(value, maxValue));

    }

    public static long clamp(long value, long minValue, long maxValue) {
        return max(minValue, min(value, maxValue));

    }
}
