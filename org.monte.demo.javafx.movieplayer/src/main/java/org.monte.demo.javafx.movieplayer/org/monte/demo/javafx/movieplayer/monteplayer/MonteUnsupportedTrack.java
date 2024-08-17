/*
 * @(#)MonteUnsupportedTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import org.monte.demo.javafx.movieplayer.model.AbstractTrack;

import java.util.Locale;
import java.util.Map;

public class MonteUnsupportedTrack extends AbstractTrack {
    public MonteUnsupportedTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }
}
