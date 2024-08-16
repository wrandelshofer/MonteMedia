/*
 * @(#)AbstractSubtitleTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

import java.util.Locale;
import java.util.Map;

public class AbstractSubtitleTrack extends AbstractTrack implements SubtitleTrackInterface {

    public AbstractSubtitleTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }
}
