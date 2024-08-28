/*
 * @(#)MonteMediaPlayer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.AbstractMediaPlayer;
import org.monte.media.math.Rational;

public class MonteMediaPlayer extends AbstractMediaPlayer {
    private final MonteMedia media;
    private final PlayerEngine engine;

    public MonteMediaPlayer(MonteMedia media) {
        this.media = media;
        error.bind(media.errorProperty());
        totalDuration.bind(media.durationProperty());

        engine = new PlayerEngine(this);
        engine.realize();
    }

    @Override
    public MonteMedia getMedia() {
        return media;
    }

    @Override
    public void dispose() {
        engine.close();
        media.dispose();
    }

    @Override
    public void pause() {
        engine.stop();
    }

    @Override
    public void play() {
        engine.start();
    }

    @Override
    public void seek(Duration seekTime) {
        engine.seek(Rational.valueOf(seekTime.toSeconds()));
    }

    @Override
    public void stop() {
        engine.stop();
    }

    @Override
    public Duration getFrameAfter(Duration timestamp) {
        Rational t = engine.getFrameAfter(Rational.valueOf((long) timestamp.toMillis(), 1000));
        Duration seconds = Duration.millis(t.multiply(1000).doubleValue());
        if (Math.abs(timestamp.toMillis() - seconds.toMillis()) < 0.01) {
            t = engine.getFrameAfter(t);
            seconds = Duration.millis(t.multiply(1000).doubleValue());
        }
        return seconds;
    }

    @Override
    public Duration getFrameBefore(Duration timestamp) {
        Rational t = engine.getFrameBefore(Rational.valueOf((long) timestamp.toMillis(), 1000));
        Duration seconds = Duration.millis(t.multiply(1000).doubleValue());
        /*
        if(Math.abs(timestamp.toMillis()-seconds.toMillis())<0.01){
            t=engine.getFrameBefore(t);
            seconds = Duration.millis(t.multiply(1000).doubleValue());
        }*/
        return seconds;
    }

    /**
     * Package private method for {@link PlayerEngine}
     *
     * @param newValue the new status
     */
    void setStatus(MediaPlayer.Status newValue) {
        status.set(newValue);
    }

    void setCurrentTime(Duration newValue) {
        currentTime.set(newValue);
    }

    void setCurrentCount(int newValue) {
        currentCount.set(newValue);
    }

    void setCurrentRate(double newValue) {
        currentRate.set(newValue);
    }

    void setBufferProgressTime(Duration newValue) {
        bufferProgressTime.set(newValue);
    }
}
