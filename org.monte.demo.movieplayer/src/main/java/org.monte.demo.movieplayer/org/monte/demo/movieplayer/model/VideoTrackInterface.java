/*
 * @(#)VideoTrackInterface.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

/**
 * An interface for {@link javafx.scene.media.VideoTrack}.
 */
public interface VideoTrackInterface extends TrackInterface {
    int getHeight();

    int getWidth();
}
