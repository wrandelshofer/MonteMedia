/*
 * @(#)MediaPlayerAdapter.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.fxplayer;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.movieplayer.GenericMediaPlayer;

public class MediaPlayerAdapter implements GenericMediaPlayer {
    private final MediaPlayer player;

    public MediaPlayerAdapter(MediaPlayer player) {
        this.player = player;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return player.currentTimeProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        return player.totalDurationProperty();
    }

    @Override
    public ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty() {
        return player.statusProperty();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void dispose() {
        player.dispose();
    }

    @Override
    public void seek(Duration seekTime) {
        player.seek(seekTime);
    }
}
