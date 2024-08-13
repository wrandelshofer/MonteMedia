/*
 * @(#)GenericMediaPlayer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public interface GenericMediaPlayer {
    ReadOnlyObjectProperty<Duration> currentTimeProperty();

    default Duration getCurrentTime() {
        return currentTimeProperty().get();
    }

    ReadOnlyObjectProperty<Duration> totalDurationProperty();

    default Duration getTotalDuration() {
        return totalDurationProperty().get();
    }

    ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty();

    default MediaPlayer.Status getStatus() {
        return statusProperty().get();
    }

    void pause();

    void play();

    void dispose();

    void seek(Duration seekTime);
}
