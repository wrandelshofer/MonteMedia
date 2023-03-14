/*
 * @(#)Interpolator.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

/**
 * A linear interpolator for use in conjunction with an Animator object.
 *
 * @author Werner Randelshofer
 */
public abstract class Interpolator {
    private float startValue;
    private float endValue;
    private long startTime;
    private long timespan;
    private boolean isFinished;

    /**
     * Creates a new interpolator which interpolates from 0 to 1 within one
     * second.
     */
    public Interpolator() {
        this(0f, 1f);
    }

    /**
     * Creates a new interpolator which interpolates from 0 to 1 within the
     * specified timespan.
     */
    public Interpolator(long timespan) {
        this(0f, 1f, timespan);
    }

    /**
     * Creates a new interpolator which interpolates into the specified
     * direction within one second.
     *
     * @param reverse Set this to true, if you want to interpolate from 1 to 0
     *                instead of from 0 to 1.
     */
    public Interpolator(boolean reverse) {
        this((reverse) ? 1f : 0f, (reverse) ? 0f : 1f);
    }

    /**
     * Creates a new interpolator which interpolates from the specified
     * start value to the specified end value within one second.
     *
     * @param startValue A value between 0 and 1.
     * @param endValue   A value between 0 and 1.
     */
    public Interpolator(float startValue, float endValue) {
        this(startValue, endValue, 1000);
    }

    /**
     * Creates a new interpolator which interpolates from the specified
     * start value to the specified end value within the specified timespan.
     *
     * @param startValue A value between 0 and 1.
     * @param endValue   A value between 0 and 1.
     * @param timespan   A timespan in milliseconds.
     */
    public Interpolator(float startValue, float endValue, long timespan) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.timespan = timespan;
    }

    /**
     * Updates the interpolator.
     *
     * @param fraction An interpolated fraction between 0 and 1.
     */
    protected abstract void update(float fraction);

    /**
     * Computes a fraction from the specified linear fraction.
     * In the simplest case, this method returns the linear fraction.
     * The returned value must be between 0 and 1.
     *
     * @param linearFraction The linear fraction between 0 and 1.
     * @return A computed fraction between 0 and 1.
     */
    protected float getFraction(float linearFraction) {
        return linearFraction;
    }

    /**
     * Returns true, if this interpolator replaces
     * interpolations by that interpolator.
     */
    public boolean replaces(Interpolator that) {
        return false;
    }

    /**
     * Initializes the interpolation.
     * <p>
     * Once this method has been called, method #finish must be called
     * before the interpolator can be destroyed.
     *
     * @param currentTimeMillis
     */
    public void initialize(long currentTimeMillis) {
        startTime = currentTimeMillis;
        update(getFraction(startValue));
    }

    /**
     * Returns true, if the timespan of the Interpolator has elapsed since
     * initialize was called.
     *
     * @param currentTimeMillis The current time.
     * @return Returns true, if the time since initialize was called is greater
     * or equal the timespan of the interpolator.
     */
    public boolean isElapsed(long currentTimeMillis) {
        return timespan <= currentTimeMillis - startTime;
    }

    /**
     * Interpolates with the current time millis.
     *
     * @param currentTimeMillis
     */
    public void interpolate(long currentTimeMillis) {
        long elapsed = Math.min(timespan, currentTimeMillis - startTime);
        float weight = elapsed / (float) timespan;
        update(getFraction(startValue * (1 - weight) + endValue * weight));
    }

    /**
     * Finishes the interpolation and calls this.notifyAll() allowing other
     * threads to synchronize on isFinished() of the interpolator.
     *
     * @param currentTimeMillis
     */
    public void finish(long currentTimeMillis) {
        if (!isFinished) {
            update(getFraction(endValue));
            isFinished = true;

            synchronized (this) {
                notifyAll();
            }
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isSequential(Interpolator that) {
        return false;
    }

    public void setTimespan(long t) {
        this.timespan = t;
    }
}
