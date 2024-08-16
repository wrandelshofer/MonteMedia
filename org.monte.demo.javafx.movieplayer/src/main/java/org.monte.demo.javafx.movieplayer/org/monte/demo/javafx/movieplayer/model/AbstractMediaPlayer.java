/*
 * @(#)AbstractMediaPlayer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import static javafx.scene.media.MediaPlayer.Status.STOPPED;

public abstract class AbstractMediaPlayer implements MediaPlayerInterface {
    protected final DoubleProperty audioSpectrumInterval = new SimpleDoubleProperty();
    protected final ObjectProperty<AudioSpectrumListener> audioSpectrumListener = new SimpleObjectProperty<>();
    protected final IntegerProperty audioSpectrumNumBands = new SimpleIntegerProperty();
    protected final IntegerProperty audioSpectrumThreshold = new SimpleIntegerProperty();
    protected final BooleanProperty autoPlay = new SimpleBooleanProperty();
    protected final DoubleProperty balance = new SimpleDoubleProperty();
    protected final ReadOnlyObjectWrapper<Duration> bufferProgressTime = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyIntegerWrapper currentCount = new ReadOnlyIntegerWrapper();
    protected final ReadOnlyDoubleWrapper currentRate = new ReadOnlyDoubleWrapper();
    protected final ReadOnlyObjectWrapper<Duration> currentTime = new ReadOnlyObjectWrapper<>();
    protected final IntegerProperty cycleCount = new SimpleIntegerProperty();
    protected final ReadOnlyObjectWrapper<Duration> cycleDuration = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyObjectWrapper<Throwable> error = new ReadOnlyObjectWrapper<>() {
        @Override
        protected void invalidated() {
            if (get() != null) {
            Runnable r = getOnError();
            if (r != null) {
                Platform.runLater(r);
            }
            }
        }
    };
    protected final BooleanProperty mute = new SimpleBooleanProperty();
    protected final ObjectProperty<Runnable> onEndOfMedia = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onError = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onHalted = new SimpleObjectProperty<>();
    protected final ObjectProperty<EventHandler<MediaMarkerEvent>> onMarker = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onPaused = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onPlaying = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onReady = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onRepeat = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onStalled = new SimpleObjectProperty<>();
    protected final ObjectProperty<Runnable> onStopped = new SimpleObjectProperty<>();
    protected final DoubleProperty rate = new SimpleDoubleProperty();
    protected final ObjectProperty<Duration> startTime = new SimpleObjectProperty<>();
    protected final ReadOnlyObjectWrapper<MediaPlayer.Status> status = new ReadOnlyObjectWrapper<>() {
        @Override
        protected void invalidated() {
            MediaPlayer.Status v = get();
            Runnable r = switch (v) {
                case READY -> getOnReady();
                case PLAYING -> getOnPlaying();
                case PAUSED -> getOnPaused();
                case STOPPED -> getOnStopped();
                case STALLED -> getOnStalled();
                default -> null;
            };
            if (r != null) {
                Platform.runLater(r);
            }
            if (v == STOPPED && getCurrentTime().equals(getTotalDuration())) {
                r = getOnEndOfMedia();
                if (r != null) {
                    Platform.runLater(r);
                }
            }
        }
    };
    protected final ObjectProperty<Duration> stopTime = new SimpleObjectProperty<>();
    protected final ReadOnlyObjectWrapper<Duration> totalDuration = new ReadOnlyObjectWrapper<>();
    protected final DoubleProperty volume = new SimpleDoubleProperty();

    public DoubleProperty audioSpectrumIntervalProperty() {
        return audioSpectrumInterval;
    }

    @Override
    public ObjectProperty<AudioSpectrumListener> audioSpectrumListenerProperty() {
        return audioSpectrumListener;
    }

    @Override
    public IntegerProperty audioSpectrumNumBandsProperty() {
        return audioSpectrumNumBands;
    }

    @Override
    public IntegerProperty audioSpectrumThresholdProperty() {
        return audioSpectrumThreshold;
    }

    @Override
    public BooleanProperty autoPlayProperty() {
        return autoPlay;
    }

    @Override
    public DoubleProperty balanceProperty() {
        return balance;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty() {
        return bufferProgressTime.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty currentCountProperty() {
        return currentCount.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyDoubleProperty currentRateProperty() {
        return currentRate.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return currentTime.getReadOnlyProperty();
    }

    @Override
    public IntegerProperty cycleCountProperty() {
        return cycleCount;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> cycleDurationProperty() {
        return cycleDuration.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> errorProperty() {
        return error.getReadOnlyProperty();
    }

    @Override
    public BooleanProperty muteProperty() {
        return mute;
    }

    @Override
    public ObjectProperty<Runnable> onEndOfMediaProperty() {
        return onEndOfMedia;
    }

    @Override
    public ObjectProperty<Runnable> onErrorProperty() {
        return onError;
    }

    @Override
    public ObjectProperty<Runnable> onHaltedProperty() {
        return onHalted;
    }

    @Override
    public ObjectProperty<EventHandler<MediaMarkerEvent>> onMarkerProperty() {
        return onMarker;
    }

    @Override
    public ObjectProperty<Runnable> onPausedProperty() {
        return onPaused;
    }

    @Override
    public ObjectProperty<Runnable> onPlayingProperty() {
        return onPlaying;
    }

    @Override
    public ObjectProperty<Runnable> onReadyProperty() {
        return onReady;
    }

    @Override
    public ObjectProperty<Runnable> onRepeatProperty() {
        return onRepeat;
    }

    @Override
    public ObjectProperty<Runnable> onStalledProperty() {
        return onStalled;
    }

    @Override
    public ObjectProperty<Runnable> onStoppedProperty() {
        return onStopped;
    }

    @Override
    public DoubleProperty rateProperty() {
        return rate;
    }

    @Override
    public ObjectProperty<Duration> startTimeProperty() {
        return startTime;
    }

    @Override
    public ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty() {
        return status.getReadOnlyProperty();
    }

    @Override
    public ObjectProperty<Duration> stopTimeProperty() {
        return stopTime;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        return totalDuration.getReadOnlyProperty();
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }

    @Override
    public AudioEqualizer getAudioEqualizer() {
        return null;
    }


}
