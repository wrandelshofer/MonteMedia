
/*
 * @(#)FXTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.scene.media.SubtitleTrack;
import org.monte.demo.javafx.movieplayer.model.SubtitleTrackInterface;

import java.util.Locale;
import java.util.Map;

public class FXSubtitleTrack implements SubtitleTrackInterface {
    private final SubtitleTrack track;

    public FXSubtitleTrack(SubtitleTrack track) {
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
