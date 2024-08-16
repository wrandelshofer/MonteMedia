/*
 * @(#)MonteVideoTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;

import org.monte.demo.movieplayer.model.AbstractAudioTrack;

import java.util.Locale;
import java.util.Map;

public class MonteAudioTrack extends AbstractAudioTrack {
    public MonteAudioTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }
}
