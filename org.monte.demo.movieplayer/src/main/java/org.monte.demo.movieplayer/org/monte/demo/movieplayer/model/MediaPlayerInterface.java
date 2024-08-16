/*
 * @(#)MediaPlayerInterface.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

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

/**
 * Interface for a player that can play {@link MediaInterface}.
 */
public interface MediaPlayerInterface {
    DoubleProperty audioSpectrumIntervalProperty();

    ObjectProperty<AudioSpectrumListener> audioSpectrumListenerProperty();

    IntegerProperty audioSpectrumNumBandsProperty();

    IntegerProperty audioSpectrumThresholdProperty();

    BooleanProperty autoPlayProperty();

    DoubleProperty balanceProperty();

    ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty();

    ReadOnlyIntegerProperty currentCountProperty();

    ReadOnlyDoubleProperty currentRateProperty();

    ReadOnlyObjectProperty<Duration> currentTimeProperty();

    IntegerProperty cycleCountProperty();

    ReadOnlyObjectProperty<Duration> cycleDurationProperty();

    ReadOnlyObjectProperty<Throwable> errorProperty();

    BooleanProperty muteProperty();

    ObjectProperty<Runnable> onEndOfMediaProperty();

    ObjectProperty<Runnable> onErrorProperty();

    ObjectProperty<Runnable> onHaltedProperty();

    ObjectProperty<EventHandler<MediaMarkerEvent>> onMarkerProperty();

    ObjectProperty<Runnable> onPausedProperty();

    ObjectProperty<Runnable> onPlayingProperty();

    ObjectProperty<Runnable> onReadyProperty();

    ObjectProperty<Runnable> onRepeatProperty();

    ObjectProperty<Runnable> onStalledProperty();

    ObjectProperty<Runnable> onStoppedProperty();

    DoubleProperty rateProperty();

    ObjectProperty<Duration> startTimeProperty();

    ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty();

    ObjectProperty<Duration> stopTimeProperty();

    ReadOnlyObjectProperty<Duration> totalDurationProperty();

    DoubleProperty volumeProperty();

    AudioEqualizer getAudioEqualizer();

    MediaInterface getMedia();

    default double getAudioSpectrumInterval() {
        return audioSpectrumIntervalProperty().get();
    }

    default AudioSpectrumListener getAudioSpectrumListener() {
        return audioSpectrumListenerProperty().get();
    }

    default Duration getBufferProgressTime() {
        return bufferProgressTimeProperty().get();
    }

    default Duration getCurrentTime() {
        return currentTimeProperty().get();
    }

    default Duration getCycleDuration() {
        return cycleDurationProperty().get();
    }

    default Throwable getError() {
        return errorProperty().get();
    }

    default Runnable getOnEndOfMedia() {
        return onEndOfMediaProperty().get();
    }

    default Runnable getOnError() {
        return onErrorProperty().get();
    }

    default Runnable getOnHalted() {
        return onHaltedProperty().get();
    }

    default EventHandler<MediaMarkerEvent> getOnMarker() {
        return onMarkerProperty().get();
    }

    default Runnable getOnPaused() {
        return onPausedProperty().get();
    }

    default Runnable getOnPlaying() {
        return onPlayingProperty().get();
    }

    default Runnable getOnReady() {
        return onReadyProperty().get();
    }

    default Runnable getOnRepeat() {
        return onRepeatProperty().get();
    }

    default Runnable getOnStalled() {
        return onStalledProperty().get();
    }

    default Runnable getOnStopped() {
        return onStoppedProperty().get();
    }

    default Duration getStartTime() {
        return startTimeProperty().get();
    }

    default MediaPlayer.Status getStatus() {
        return statusProperty().get();
    }

    default Duration getStopTime() {
        return stopTimeProperty().get();
    }

    default Duration getTotalDuration() {
        return totalDurationProperty().get();
    }

    default int getAudioSpectrumNumBands() {
        return audioSpectrumNumBandsProperty().get();
    }

    default int getAudioSpectrumThreshold() {
        return audioSpectrumThresholdProperty().get();
    }

    default Boolean istAutoPlay() {
        return autoPlayProperty().get();
    }

    default Double getBalance() {
        return balanceProperty().get();
    }

    default int getCurrentCount() {
        return currentCountProperty().get();
    }

    default double getCurrentRate() {
        return currentRateProperty().get();
    }

    default int getCycleCount() {
        return cycleCountProperty().get();
    }

    default boolean isMute() {
        return muteProperty().get();
    }

    default double getRate() {
        return rateProperty().get();
    }

    default double getVolume() {
        return volumeProperty().get();
    }

    default void setAudioSpectrumInterval(double newValue) {
        audioSpectrumIntervalProperty().set(newValue);
    }

    default void setAudioSpectrumListener(AudioSpectrumListener newValue) {
        audioSpectrumListenerProperty().set(newValue);
    }

    default void setOnEndOfMedia(Runnable newValue) {
        onEndOfMediaProperty().set(newValue);
    }

    default void setOnError(Runnable newValue) {
        onErrorProperty().set(newValue);
    }

    default void setOnHalted(Runnable newValue) {
        onHaltedProperty().set(newValue);
    }

    default void setOnMarker(EventHandler<MediaMarkerEvent> newValue) {
        onMarkerProperty().set(newValue);
    }

    default void setOnPaused(Runnable newValue) {
        onPausedProperty().set(newValue);
    }

    default void setOnPlaying(Runnable newValue) {
        onPlayingProperty().set(newValue);
    }

    default void setOnReady(Runnable newValue) {
        onReadyProperty().set(newValue);
    }

    default void setOnRepeat(Runnable newValue) {
        onRepeatProperty().set(newValue);
    }

    default void setOnStalled(Runnable newValue) {
        onStalledProperty().set(newValue);
    }

    default void setOnStopped(Runnable newValue) {
        onStoppedProperty().set(newValue);
    }

    default void setStartTime(Duration newValue) {
        startTimeProperty().set(newValue);
    }

    default void setStopTime(Duration newValue) {
        stopTimeProperty().set(newValue);
    }

    default void setAudioSpectrumNumBands(int newValue) {
        audioSpectrumNumBandsProperty().set(newValue);
    }

    default void setAudioSpectrumThreshold(int newValue) {
        audioSpectrumThresholdProperty().set(newValue);
    }

    default void setAutoPlay(Boolean newValue) {
        autoPlayProperty().set(newValue);
    }

    default void setBalance(Double newValue) {
        balanceProperty().set(newValue);
    }

    default void setCycleCount(int newValue) {
        cycleCountProperty().set(newValue);
    }

    default void setMute(boolean newValue) {
        muteProperty().set(newValue);
    }

    default void setRate(double newValue) {
        rateProperty().set(newValue);
    }

    default void setVolume(double newValue) {
        volumeProperty().set(newValue);
    }


    void dispose();

    void pause();

    void play();

    void seek(Duration seekTime);

    void stop();


}
