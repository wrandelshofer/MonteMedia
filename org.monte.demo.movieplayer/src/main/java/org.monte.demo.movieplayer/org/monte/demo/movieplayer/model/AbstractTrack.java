/*
 * @(#)AbstractTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

import java.util.Locale;
import java.util.Map;

public class AbstractTrack implements TrackInterface {
    private final long trackId;
    private final String name;
    private final Locale locale;
    private final Map<String, Object> metadata;

    public AbstractTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        this.locale = locale;
        this.trackId = trackId;
        this.name = name;
        this.metadata = metadata;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTrackID() {
        return trackId;
    }
}
