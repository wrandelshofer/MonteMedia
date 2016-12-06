/* @(#)MathUtils.java
 * Copyright © 2013 Werner Randelshofer, Switzerland.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with Werner Randelshofer. 
 * For details see accompanying license terms.
 */
package ru.sbtqa.monte.media.math;

import static java.lang.Math.*;

/**
 * {@code MathUtils}.
 *
 * @author Werner Randelshofer
 * @version $Id: MathUtils.java 364 2016-11-09 19:54:25Z werner $
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
