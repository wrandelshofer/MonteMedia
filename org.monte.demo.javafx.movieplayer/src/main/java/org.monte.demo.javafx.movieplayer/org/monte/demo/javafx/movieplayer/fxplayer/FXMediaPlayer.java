/*
 * @(#)FXMediaPlayer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.MediaInterface;
import org.monte.demo.javafx.movieplayer.model.MediaPlayerInterface;

/**
 * Adapter for JavaFX {@link MediaPlayer}.
 */
public class FXMediaPlayer implements MediaPlayerInterface {
    private final MediaPlayer player;
    private final MediaInterface media;

    public FXMediaPlayer(MediaPlayer player) {
        this.player = player;
        this.media = new FXMedia(player.getMedia());
    }

    @Override
    public DoubleProperty audioSpectrumIntervalProperty() {
        return player.audioSpectrumIntervalProperty();
    }

    @Override
    public ObjectProperty<AudioSpectrumListener> audioSpectrumListenerProperty() {
        return player.audioSpectrumListenerProperty();
    }

    @Override
    public IntegerProperty audioSpectrumNumBandsProperty() {
        return player.audioSpectrumNumBandsProperty();
    }

    @Override
    public IntegerProperty audioSpectrumThresholdProperty() {
        return player.audioSpectrumThresholdProperty();
    }

    @Override
    public BooleanProperty autoPlayProperty() {
        return player.autoPlayProperty();
    }

    @Override
    public DoubleProperty balanceProperty() {
        return player.balanceProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty() {
        return player.bufferProgressTimeProperty();
    }

    @Override
    public ReadOnlyIntegerProperty currentCountProperty() {
        return player.currentCountProperty();
    }

    @Override
    public ReadOnlyDoubleProperty currentRateProperty() {
        return player.currentRateProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return player.currentTimeProperty();
    }

    @Override
    public IntegerProperty cycleCountProperty() {
        return player.cycleCountProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> cycleDurationProperty() {
        return player.cycleDurationProperty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReadOnlyObjectProperty<Throwable> errorProperty() {
        return (ReadOnlyObjectProperty<Throwable>) (ReadOnlyObjectProperty<?>) player.errorProperty();
    }

    @Override
    public BooleanProperty muteProperty() {
        return player.muteProperty();
    }

    @Override
    public ObjectProperty<Runnable> onEndOfMediaProperty() {
        return player.onEndOfMediaProperty();
    }

    @Override
    public ObjectProperty<Runnable> onErrorProperty() {
        return player.onErrorProperty();
    }

    @Override
    public ObjectProperty<Runnable> onHaltedProperty() {
        return player.onHaltedProperty();
    }

    @Override
    public ObjectProperty<EventHandler<MediaMarkerEvent>> onMarkerProperty() {
        return player.onMarkerProperty();
    }

    @Override
    public ObjectProperty<Runnable> onPausedProperty() {
        return player.onPausedProperty();
    }

    @Override
    public ObjectProperty<Runnable> onPlayingProperty() {
        return player.onPlayingProperty();
    }

    @Override
    public Runnable getOnReady() {
        return player.getOnReady();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        return player.totalDurationProperty();
    }

    @Override
    public DoubleProperty volumeProperty() {
        return player.volumeProperty();
    }

    @Override
    public AudioEqualizer getAudioEqualizer() {
        return player.getAudioEqualizer();
    }

    @Override
    public MediaInterface getMedia() {
        return media;
    }

    @Override
    public ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty() {
        return player.statusProperty();
    }

    @Override
    public ObjectProperty<Duration> stopTimeProperty() {
        return player.stopTimeProperty();
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

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public Duration getFrameAfter(Duration timestamp) {
        return timestamp.add(Duration.seconds(10));
    }

    @Override
    public Duration getFrameBefore(Duration timestamp) {
        return timestamp.add(Duration.seconds(10));
    }

    @Override
    public ObjectProperty<Runnable> onReadyProperty() {
        return player.onReadyProperty();
    }

    @Override
    public ObjectProperty<Runnable> onRepeatProperty() {
        return player.onRepeatProperty();
    }

    @Override
    public ObjectProperty<Runnable> onStalledProperty() {
        return player.onStalledProperty();
    }

    @Override
    public ObjectProperty<Runnable> onStoppedProperty() {
        return player.onStoppedProperty();
    }

    @Override
    public DoubleProperty rateProperty() {
        return player.rateProperty();
    }

    @Override
    public ObjectProperty<Duration> startTimeProperty() {
        return player.startTimeProperty();
    }
}
