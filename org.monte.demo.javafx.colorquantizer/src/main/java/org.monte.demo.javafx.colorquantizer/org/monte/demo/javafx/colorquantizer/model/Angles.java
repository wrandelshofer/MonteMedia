/*
 * @(#)Angles.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.model;


import java.awt.geom.Point2D;

import static java.lang.Math.PI;

public class Angles {
    /**
     * Gets the angle of the specified line.
     *
     * @param x1 the x-coordinate of point 1 on the line
     * @param y1 the y-coordinate of point 1 on the line
     * @param x2 the x-coordinate of point 2 on the line
     * @param y2 the y-coordinate of point 2 on the line
     * @return the angle in radians
     */
    public static double angle(double x1, double y1, double x2, double y2) {
        double dy = y2 - y1;
        double dx = x2 - x1;
        return atan2(dy, dx);
    }

    /**
     * Computes atan2 if dy and dx are large enough.
     * <p>
     * Math.atan2 can go into an infinite loop if dy and dx are almost zero.
     *
     * @param dy the dy
     * @param dx the dx
     * @return atan2 of dy, dx or 0.
     */
    public static double atan2(double dy, double dx) {
        return Scalars.almostZero(dy) && Scalars.almostZero(dx) ? 0.0 : Math.atan2(dy, dx);
    }

    /**
     * Signed difference of two angles.
     *
     * @param from angle 0
     * @param to   angle 1
     * @return -PI &lt;= diff &lt;= PI.
     */
    public static double angleSubtract(double from, double to) {
        double diff = from - to;
        if (diff < -2 * PI) {
            diff += 2 * PI;
        }
        return diff;
    }

    /**
     * Signed shortest distance between two angles.
     *
     * @param from angle 0
     * @param to   angle 1
     * @return -PI &lt;= diff &lt;= PI.
     */
    public static double anglesSignedSpan(double from, double to) {
        double diff = to - from;
        if (diff > PI) {
            diff = diff - PI;
        } else if (diff < -PI) {
            diff = diff + 2 * PI;
        }
        return diff;
    }

    /**
     * Unsigned shortest distance between two angles.
     *
     * @param from angle 0
     * @param to   angle 1
     * @return 0 &lt;= diff &lt;= PI.
     */
    public static double anglesUnsignedSpan(double from, double to) {
        return from > to ? from - to : to - from;
    }


    /**
     * Gets the perpendicular vector to the given vector.
     *
     * @param x the x value of the vector
     * @param y the x value of the vector
     * @return the perpendicular vector
     */
    public static Point2D.Double perp(double x, double y) {
        return new Point2D.Double(y, -x);
    }

    public static double atan2Ellipse(double cx, double cy, double rx, double ry, double x, double y) {
        return atan2(y, x);
    }

    /**
     * Returns the trigonometric sine of an angle in degrees.
     * <p>
     * References:
     * <dl>
     *     <dt>Values of Trigonometric ratios for 0, 30, 45, 60 and 90 degrees</dt>
     *     <dd><a href="https://mathinstructor.net/2012/08/values-of-trigonometric-ratios-for-0-30-45-60-and-90-degrees/">mathinstructor.net</a></dd>
     * </dl>
     *
     * @param aDeg an angle in degrees
     * @return the sine of the argument
     */
    public static double sinDegrees(double aDeg) {
        int aDegInt = (int) aDeg;
        if (aDeg == aDegInt) {
            switch (aDegInt % 360) {
                case 0:
                    return 0.0;// = sqrt(0/4)
                case 30:
                case 150:
                case -210:
                case -330:
                    return 0.5;// = sqrt(1/4)
                case -30:
                case -150:
                case 210:
                case 330:
                    return -0.5;// = sqrt(1/4)
                case 45:
                case 135:
                case -315:
                case -225:
                    return Math.sqrt(0.5);// = sqrt(2/4)
                case -45:
                case -135:
                case 315:
                case 225:
                    return -Math.sqrt(0.5);// = sqrt(2/4)
                case 60:
                case 120:
                case -300:
                case -240:
                    return Math.sqrt(0.75);// = sqrt(3/4)
                case -60:
                case -120:
                case 300:
                case 240:
                    return -Math.sqrt(0.75);// = sqrt(3/4)
                case 90:
                case -270:
                    return 1;// = sqrt(4/4)
                case -90:
                case 270:
                    return -1;// = sqrt(4/4)

            }
        }
        return Math.sin(Math.toRadians(aDeg));
    }

    /**
     * Returns the trigonometric cosine of an angle in degrees.
     * <p>
     * References:
     * <dl>
     *     <dt>Values of Trigonometric ratios for 0, 30, 45, 60 and 90 degrees</dt>
     *     <dd><a href="https://mathinstructor.net/2012/08/values-of-trigonometric-ratios-for-0-30-45-60-and-90-degrees/">mathinstructor.net</a></dd>
     * </dl>
     *
     * @param aDeg an angle in degrees
     * @return the cosine of the argument
     */
    public static double cosDegrees(double aDeg) {
        return sinDegrees(aDeg + 90);
    }
}
