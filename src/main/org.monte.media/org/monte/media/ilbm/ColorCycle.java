/* @(#)ColorCycle.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.ilbm;

/**
 * Base class for color cycling in an IFF ILBM image.
 *
 * @author Werner Randelshofer
 * @version 1.1 2010-08-03 Added support for blended cycles.
 * <br>1.0 2010-01-22 Created.
 */
public abstract class ColorCycle implements Cloneable {

    /** Cycle rate. */
    protected int rate;
    /** Time scale of the cycle rate. Dividing the rate by the time scale yields
     * the rate per second.
     */
    protected int timeScale;
    /** Whether the color cycle is active. */
    protected boolean isActive;

    /** Whether colors are blended into each other when shifted. */
    protected boolean isBlended;

    public ColorCycle(int rate, int timeScale, boolean isActive) {
        this.rate = rate;
        this.timeScale = timeScale;
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }
    public int getRate() {
        return rate;
    }

    public int getTimeScale() {
        return timeScale;
    }

    /** Returns true if colors are blended when shifted. */
    public boolean isBlended() {
        return isBlended;
    }
    /** Set to true to blend colors when they are shifted. */
    public void setBlended(boolean newValue) {
        isBlended=newValue;
    }


    public abstract void doCycle(int[] rgbs, long time);

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            InternalError error = new InternalError();
            error.initCause(ex);
            throw error;
        }
    }
}
