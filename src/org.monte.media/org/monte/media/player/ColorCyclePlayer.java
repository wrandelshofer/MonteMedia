/* @(#)ColorCyclePlayer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

/**
 * A {@link Player} which supports a second layer of animation by cycling colors
 * in the color palette of the current image in the video track.
 * <p>
 * Color cycling is provided in a separate layer on top of the video track.
 * It can be performed independently of video playback.
 * </p>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface ColorCyclePlayer extends Player {

    /** Returns true if color cycling is started. */
    public boolean isColorCyclingStarted();

    /** Starts/Stops color cycling. */
    public void setColorCyclingStarted(boolean b);

    /** Returns true if color cycling is available. */
    public boolean isColorCyclingAvailable();

    /** Sets whether colors are blended during color cycling. */
    public void setBlendedColorCycling(boolean newValue);

    /** Returns true if colors are blended during color cycling. */
    public boolean isBlendedColorCycling();
}
