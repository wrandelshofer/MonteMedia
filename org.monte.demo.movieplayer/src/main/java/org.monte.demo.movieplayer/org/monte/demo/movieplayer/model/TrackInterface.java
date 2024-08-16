/*
 * @(#)TrackInterface.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

import java.util.Locale;
import java.util.Map;

/**
 * An interface for {@link javafx.scene.media.Track}.
 */
public interface TrackInterface {
    Locale getLocale();

    Map<String, Object> getMetadata();

    String getName();

    long getTrackID();
}
