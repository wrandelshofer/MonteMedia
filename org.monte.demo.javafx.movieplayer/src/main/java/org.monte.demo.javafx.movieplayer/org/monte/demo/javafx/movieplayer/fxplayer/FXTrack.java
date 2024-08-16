/*
 * @(#)FXTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.scene.media.Track;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;

import java.util.Locale;
import java.util.Map;

public class FXTrack implements TrackInterface {
    private final Track track;

    public FXTrack(Track track) {
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
