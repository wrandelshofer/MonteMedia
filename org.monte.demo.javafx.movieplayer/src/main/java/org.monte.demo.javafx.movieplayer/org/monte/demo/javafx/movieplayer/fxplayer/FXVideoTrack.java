/*
 * @(#)FXTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.scene.media.VideoTrack;
import org.monte.demo.javafx.movieplayer.model.VideoTrackInterface;

import java.util.Locale;
import java.util.Map;

public class FXVideoTrack implements VideoTrackInterface {
    private final VideoTrack track;

    public FXVideoTrack(VideoTrack track) {
        this.track = track;
    }

    @Override
    public int getHeight() {
        return track.getHeight();
    }

    @Override
    public int getWidth() {
        return track.getWidth();
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
