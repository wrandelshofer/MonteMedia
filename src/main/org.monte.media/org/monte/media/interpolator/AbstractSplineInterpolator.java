/* @(#)AbstractSplineInterpolator.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.interpolator;

import java.awt.geom.Point2D.Float;
import java.util.Comparator;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.util.Arrays;
import org.monte.media.av.Interpolator;
import static java.lang.Math.*;

/**
 * {@code AbstractSplineInterpolator}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-01-25 Created.
 */
public abstract class AbstractSplineInterpolator extends Interpolator {

    /** Note: (x0,y0) and (x1,y1) are implicitly (0, 0) and (1,1) respectively. */
    private LengthItem[] fractions;

    private static class LengthItem {

        public float x, y, t;

        public LengthItem(float x, float y, float t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }

        public LengthItem(Point2D.Float p, float t) {
            this.x = p.x;
            this.y = p.y;
            this.t = t;
        }

        @Override
        public String toString() {
            return "LengthItem{" + "x=" + x + ", y=" + y + ", t=" + t + '}';
        }
        
        
    }

    private static class FractionComparator implements Comparator<LengthItem> {

        @Override
        public int compare(LengthItem o1, LengthItem o2) {
            if (o1.x > o2.x) {
                return 1;
            } else if (o1.x < o2.x) {
                return -1;
            }
            return 0;
        }
    }
    private static FractionComparator fractionComparator = new FractionComparator();

 public AbstractSplineInterpolator() {
        this(0f, 1f);
    }
    /**
     * Creates a new interpolator which interpolates from 0 to 1 within the
     * specified timespan.
     */
    public AbstractSplineInterpolator(long timespan) {
        this(0f, 1f, timespan);
    }
    /**
     * Creates a new interpolator which interpolates into the specified
     * direction within one second.
     *
     * @param reverse Set this to true, if you want to interpolate from 1 to 0
     * instead of from 0 to 1. 
     */
    public AbstractSplineInterpolator(boolean reverse) {
        this((reverse) ? 1f : 0f, (reverse) ? 0f : 1f);
    }
    /**
     * Creates a new interpolator which interpolates from the specified
     * start value to the specified end value within one second.
     *
     * @param startValue A value between 0 and 1. 
     * @param endValue A value between 0 and 1. 
     */
    public AbstractSplineInterpolator(float startValue, float endValue) {
        this(startValue, endValue, 1000);
    }
    /**
     * Creates a new interpolator which interpolates from the specified
     * start value to the specified end value within the specified timespan.
     *
     * @param startValue A value between 0 and 1. 
     * @param endValue A value between 0 and 1. 
     * @param timespan A timespan in milliseconds.
     */
    public AbstractSplineInterpolator(float startValue, float endValue, long timespan) {
        super(startValue,endValue,timespan);
    }    

    /** This method must be called by the subclass in the constructor.
     * 
     * @param N 
     */
    protected void updateFractions(int N) {
        fractions = new LengthItem[N];
        Point2D.Float p = new Point2D.Float();
        for (int i = 0; i < N; i++) {
            float t = (float) i / (N - 1);
            fractions[i] = new LengthItem(getXY(t, p), t);
        }
    }

    /**
     * Evaluates the spline function at time t, and clamps the result value between 0
     * and 1.
     */
    @Override
    public final float getFraction(float t) {
        LengthItem p1 = new LengthItem(t, 0f, t);
        LengthItem p2 = new LengthItem(t, 0f, t);
        int index = Arrays.binarySearch(fractions, p1, fractionComparator);
        if (index >= 0) {// we have found the exact value
            return fractions[index].y;
        }

        // we found the next bigger value
        index = -1 - index;
        if (index == fractions.length) {
            return fractions[fractions.length - 1].y;
        }
        if (index == 0) {
            return fractions[0].y;
        }

        p1 = fractions[max(0, index - 1)];
        p2 = fractions[min(fractions.length - 1, index)];
        float weight = (p2.x - t) / (p2.x - p1.x);
        float s = p1.t * weight + p2.t * (1 - weight);
        return getY(s);
    }

    /**
     * Evaluates the spline function at curve parameter time t.
     */
    protected abstract Point2D.Float getXY(float t, Point2D.Float p);

    /**
     * Evaluates the spline function at curve parameter time t.
     */
    protected abstract float getY(float t);

    /** This method is empty. 
     * Subclasses don't have to call super.update(fraction). */
    @Override
    protected void update(float fraction) {
    }
}
