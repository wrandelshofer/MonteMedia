/*
 * @(#)ANIMAudioClip.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.eightsvx;

/**
 * The {@code ANIMAudioClip} interface is a simple abstraction for
 * playing a sound clip. Multiple {@code ANIMAudioClip} items can be
 * playing at the same time, and the resulting sound is mixed
 * together to produce a composite.
 */
public interface AudioClip {
    /**
     * Starts playing this audio clip. Each time this method is called,
     * the clip is restarted from the beginning.
     */
    void play();

    /**
     * Starts playing this audio clip in a loop.
     */
    void loop();

    /**
     * Stops playing this audio clip.
     */
    void stop();
}
