/*
 * @(#)FXTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.scene.media.AudioTrack;
import org.monte.demo.javafx.movieplayer.model.AudioTrackInterface;

import java.util.Locale;
import java.util.Map;

public class FXAudioTrack implements AudioTrackInterface {
    private final AudioTrack track;

    public FXAudioTrack(AudioTrack track) {
        this.track = track;
    }

    @Override
    public Locale getLocale() {
        return track.getLocale();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return track.getMetadata();
    }

    @Override
    public String getName() {
        return track.getName();
    }

    @Override
    public long getTrackID() {
        return track.getTrackID();
    }
}
