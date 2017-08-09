/* @(#)SplineInterpolator.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.interpolator;

import java.awt.geom.Point2D;

/**
 * A spline interpolator for use in conjunction with an Animator object.
 * <p>
 * This class interpolates fractional values using Bezier splines.  The anchor
 * points for the spline are assumed to be (0, 0) and (1, 1).  Control points
 * should all be in the range [0, 1].
 * <p>
 * For more information on how splines are used to interpolate, refer to the
 * SMIL specification at http://w3c.org.
 * <p>
 * <a href="http://www.w3.org/TR/smil/smil-animation.html#animationNS-InterpolationKeysplines"
 * >http://www.w3.org/TR/smil/smil-animation.html#animationNS-InterpolationKeysplines</a>
 *
 * @author Werner Randelshofer
 * @version $Id: SplineInterpolator.java 364 2016-11-09 19:54:25Z werner $
 */
public class SplineInterpolator extends AbstractSplineInterpolator {

    /** Note: (x0,y0) and (x1,y1) are implicitly (0, 0) and (1,1) respectively. */
    private float x1, y1, x2, y2;

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2).  The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * <p>
     * The interpolator runs for one second.
     * </p>
     *
     * @param x1 The x coordinate for the first bezier control point.
     * @param y1 The y coordinate for the first bezier control point.
     * @param x2 The x coordinate for the second bezier control point.
     * @param y2 The x coordinate for the second bezier control point.
     *
     * @throws IllegalArgumentException This exception is thrown when values
     * beyond the allowed [0,1] range are passed in
     */
    public SplineInterpolator(float x1, float y1, float x2, float y2) {
        this(x1, y1, x2, y2, 1000);

    }

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2).  The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * <p>
     * The interpolator runs for the specified time span.
     * </p>
     * @param x1 The x coordinate for the first bezier control point.
     * @param y1 The y coordinate for the first bezier control point.
     * @param x2 The x coordinate for the second bezier control point.
     * @param y2 The x coordinate for the second bezier control point.
     * @param timespan The time span in milliseconds.
     *
     *
     * @throws IllegalArgumentException This exception is thrown when values
     * beyond the allowed [0,1] range are passed in
     */
    public SplineInterpolator(float x1, float y1, float x2, float y2, long timespan) {
        this(x1, y1, x2, y2, false, timespan);
    }

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2).  The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * <p>
     * The interpolator runs for the specified time span.
     * </p>
     * @param x1 The x coordinate for the first bezier control point.
     * @param y1 The y coordinate for the first bezier control point.
     * @param x2 The x coordinate for the second bezier control point.
     * @param y2 The x coordinate for the second bezier control point.
     * @param reverse Run interpolator in the reverse direction.
     * @param timespan The time span in milliseconds.
     *
     *
     * @throws IllegalArgumentException This exception is thrown when values
     * beyond the allowed [0,1] range are passed in
     */
    public SplineInterpolator(float x1, float y1, float x2, float y2, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);

        if (x1 < 0 || x1 > 1.0f
                || y1 < 0 || y1 > 1.0f
                || x2 < 0 || x2 > 1.0f
                || y2 < 0 || y2 > 1.0f) {
            throw new IllegalArgumentException("Control points must be in "
                    + "the range [0, 1]:");
        }

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        updateFractions(100);
    }


    /**
     * Evaluates the spline function at curve parameter time t.
     */
    @Override
    public Point2D.Float getXY(float t, Point2D.Float p) {
        if (p == null) {
            p = new Point2D.Float();
        }
        float invT = (1 - t);
        float b1 = 3 * t * (invT * invT);
        float b2 = 3 * (t * t) * invT;
        float b3 = t * t * t;
        p.setLocation((b1 * x1) + (b2 * x2) + b3, (b1 * y1) + (b2 * y2) + b3);
        return p;
    }
    /**
     * Evaluates the spline function at curve parameter time t.
     */
    @Override
    public float getY(float t) {
        float invT = (1 - t);
        float b1 = 3 * t * (invT * invT);
        float b2 = 3 * (t * t) * invT;
        float b3 = t * t * t;
        return (b1 * y1) + (b2 * y2) + b3;
    }
}
