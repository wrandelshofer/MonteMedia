/* @(#)ColorCyclePlayer.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

/**
 * A {@link Player} which supports a second layer of animation by cycling colors
 * in the color palette of the current image in the video track.
 * 
 * Color cycling is provided in a separate layer on top of the video track. It
 * can be performed independently of video playback.
 * 
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-08-04 Created.
 */
public interface ColorCyclePlayer extends Player {

    /**
     * Returns true if color cycling is started.
     *
     * @return TODO
     */
    public boolean isColorCyclingStarted();

    /**
     * Starts/Stops color cycling.
     *
     * @param b TODO
     */
    public void setColorCyclingStarted(boolean b);

    /**
     * Returns true if color cycling is available.
     *
     * @return TODO
     */
    public boolean isColorCyclingAvailable();

    /**
     * Sets whether colors are blended during color cycling.
     *
     * @param newValue TODO
     */
    public void setBlendedColorCycling(boolean newValue);

    /**
     * Returns true if colors are blended during color cycling.
     *
     * @return TODO
     */
    public boolean isBlendedColorCycling();
}
