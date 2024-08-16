/*
 * @(#)AbstractAudioTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.model;

import java.util.Locale;
import java.util.Map;

public class AbstractAudioTrack extends AbstractTrack implements AudioTrackInterface {

    public AbstractAudioTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }
}
