/*
 * @(#)ColorCyclePlayer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

/// A [Player] which supports a second layer of animation by cycling colors
/// in the color palette of the current image in the video track.
///
/// Color cycling is provided in a separate layer on top of the video track.
/// It can be performed independently of video playback.
///
/// @author Werner Randelshofer
public interface ColorCyclePlayer extends Player {

    /// Returns true if color cycling is started.
    public boolean isColorCyclingStarted();

    /// Starts/Stops color cycling.
    public void setColorCyclingStarted(boolean b);

    /// Returns true if color cycling is available.
    public boolean isColorCyclingAvailable();

    /// Sets whether colors are blended during color cycling.
    public void setBlendedColorCycling(boolean newValue);

    /// Returns true if colors are blended during color cycling.
    public boolean isBlendedColorCycling();
}
