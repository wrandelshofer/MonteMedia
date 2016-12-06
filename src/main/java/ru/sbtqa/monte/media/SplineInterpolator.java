/* @(#)SplineInterpolator.java
 * Copyright © 2007 Werner Randelshofer
 * Hausmatt 10, CH-6405 Goldau, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 *
 * This class uses code from http://timingframework.dev.java.net:
 * The derived code is Copyright (c) 2006, Sun Microsystems, Inc
 *     All rights reserved.
 *
 *     Redistribution and use in source and binary forms, with or without
 *     modification, are permitted provided that the following conditions
 *     are met:
 *
 *       * Redistributions of source code must retain the above copyright
 *         notice, this list of conditions and the following disclaimer.
 *       * Redistributions in binary form must reproduce the above
 *         copyright notice, this list of conditions and the following
 *         disclaimer in the documentation and/or other materials provided
 *         with the distribution.
 *       * Neither the name of the TimingFramework project nor the names of its
 *         contributors may be used to endorse or promote products derived
 *         from this software without specific prior written permission.
 *
 *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *     "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *     LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *     A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *     OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *     SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *     LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *     OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package ru.sbtqa.monte.media;

import java.awt.geom.Point2D;

/**
 * A spline interpolator for use in conjunction with an Animator object.
 * 
 * This class interpolates fractional values using Bezier splines. The anchor
 * points for the spline are assumed to be (0, 0) and (1, 1). Control points
 * should all be in the range [0, 1].
 * 
 * For more information on how splines are used to interpolate, refer to the
 * SMIL specification at http://w3c.org.
 * 
 * <a href="http://www.w3.org/TR/smil/smil-animation.html#animationNS-InterpolationKeysplines"
 * >http://www.w3.org/TR/smil/smil-animation.html#animationNS-InterpolationKeysplines</a>
 *
 * @author Werner Randelshofer
 * @version $Id: SplineInterpolator.java 364 2016-11-09 19:54:25Z werner $
 */
public class SplineInterpolator extends AbstractSplineInterpolator {

    /**
     * Note: (x0,y0) and (x1,y1) are implicitly (0, 0) and (1,1) respectively.
     */
    private float x1, y1, x2, y2;

    /**
     * Creates a new instance of SplineInterpolator with the control points
     * defined by (x1, y1) and (x2, y2). The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * 
     * The interpolator runs for one second.
     * 
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
     * defined by (x1, y1) and (x2, y2). The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * 
     * The interpolator runs for the specified time span.
     * 
     *
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
     * defined by (x1, y1) and (x2, y2). The anchor points are implicitly
     * defined as (0, 0) and (1, 1).
     * 
     * The interpolator runs for the specified time span.
     * 
     *
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
     *
     * @param t TODO
     * @param p TODO
     * @return TODO
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
     *
     * @param t TODO
     * @return TODO
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
