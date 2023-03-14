/*
 * @(#)BezierInterpolator.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.interpolator;

import java.awt.geom.Point2D;

/**
 * A bezier interpolator for use in conjunction with an Animator object.
 * <p>
 * This class interpolates fractional values using a Bezier spline.  The anchor
 * points for the spline are assumed to be (0, 0) and (1, 1).  Control points
 * should all be in the range [0, 1].
 * <p>
 * FIXME - This interpolator does not work!
 *
 * @author Werner Randelshofer
 */
public class BezierInterpolator extends AbstractSplineInterpolator {

    /**
     * Note: (x0,y0) and (x1,y1) are implicitly (0, 0) and (1,1) respectively.
     */
    private double[] controlPoints;

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
     * @throws IllegalArgumentException This exception is thrown when values
     *                                  beyond the allowed [0,1] range are passed in
     */
    public BezierInterpolator(float x1, float y1, float x2, float y2) {
        this(x1, y1, x2, y2, 1000);

    }

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2).  The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * <p>
     * The interpolator runs for the specified time span.
     * </p>
     *
     * @param x1       The x coordinate for the first bezier control point.
     * @param y1       The y coordinate for the first bezier control point.
     * @param x2       The x coordinate for the second bezier control point.
     * @param y2       The x coordinate for the second bezier control point.
     * @param timespan The time span in milliseconds.
     * @throws IllegalArgumentException This exception is thrown when values
     *                                  beyond the allowed [0,1] range are passed in
     */
    public BezierInterpolator(float x1, float y1, float x2, float y2, long timespan) {
        this(x1, y1, x2, y2, false, timespan);
    }

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2).  The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * <p>
     * The interpolator runs for the specified time span.
     * </p>
     *
     * @param x1       The x coordinate for the first bezier control point.
     * @param y1       The y coordinate for the first bezier control point.
     * @param x2       The x coordinate for the second bezier control point.
     * @param y2       The x coordinate for the second bezier control point.
     * @param reverse  Run interpolator in the reverse direction.
     * @param timespan The time span in milliseconds.
     * @throws IllegalArgumentException This exception is thrown when values
     *                                  beyond the allowed [0,1] range are passed in
     */
    public BezierInterpolator(float x1, float y1, float x2, float y2, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);

        if (x1 < 0 || x1 > 1.0f
                || y1 < 0 || y1 > 1.0f
                || x2 < 0 || x2 > 1.0f
                || y2 < 0 || y2 > 1.0f) {
            throw new IllegalArgumentException("Control points must be in "
                    + "the range [0, 1]:");
        }

        controlPoints = new double[4 * 2];
        controlPoints[0] = 0;
        controlPoints[1] = 0;
        controlPoints[2] = x1;
        controlPoints[3] = y1;
        controlPoints[4] = x2;
        controlPoints[5] = y2;
        controlPoints[6] = 1;
        controlPoints[7] = 1;

        updateFractions(100);
    }

    /**
     * Interpolates between the specified control points.
     *
     * @param controlPoints The control points of the bezier path. Must include
     *                      the first and the last control point. The curve must be in the range [0,1].
     */
    public BezierInterpolator(double[][] controlPoints) {
        this(controlPoints, false, 1000);


    }

    /**
     * Interpolates between the specified control points.
     *
     * @param controlPoints The control points of the bezier path. Must include
     *                      the first and the last control point. The curve must be in the range [0,1].
     */
    public BezierInterpolator(double[][] controlPoints, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);
        this.controlPoints = new double[controlPoints.length * 2];
        for (int i = 0; i < controlPoints.length; i++) {
            this.controlPoints[i * 2] = controlPoints[i][0];
            this.controlPoints[i * 2 + 1] = controlPoints[i][1];
        }
        updateFractions(100);
    }

    /**
     * Evaluates the bezier function and returns a 2D point.
     *
     * @return A point, with x- and y-coordinates for time t.
     */
    @Override
    public Point2D.Float getXY(float t, Point2D.Float xy) {
        if (xy == null) xy = new Point2D.Float(0, 0);

        double[] p = controlPoints.clone();

        for (int i = p.length / 2 - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                p[j * 2 + 0] = (1 - t) * p[j * 2 + 0] + t * p[(j + 1) * 2 + 0];
                p[j * 2 + 1] = (1 - t) * p[j * 2 + 1] + t * p[(j + 1) * 2 + 1];
            }
        }

        xy.setLocation(p[0], p[1]);
        return xy;
    }

    /**
     * Evaluates the bezier function and returns a 2D point.
     *
     * @return A point, with x- and y-coordinates for time t.
     */
    @Override
    public float getY(float t) {
        double[] p = controlPoints.clone();

        for (int i = p.length / 2 - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                p[j * 2 + 0] = (1 - t) * p[j * 2 + 0] + t * p[(j + 1) * 2 + 0];
                p[j * 2 + 1] = (1 - t) * p[j * 2 + 1] + t * p[(j + 1) * 2 + 1];
            }
        }
        return (float) p[1];
    }
}
