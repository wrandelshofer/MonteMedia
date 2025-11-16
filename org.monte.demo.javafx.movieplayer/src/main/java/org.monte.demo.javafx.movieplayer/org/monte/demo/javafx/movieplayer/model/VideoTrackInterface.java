/*
 * @(#)VideoTrackInterface.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.model;

/**
 * An interface for {@link javafx.scene.media.VideoTrack}.
 */
public interface VideoTrackInterface extends TrackInterface {
    int getHeight();

    int getWidth();
}
