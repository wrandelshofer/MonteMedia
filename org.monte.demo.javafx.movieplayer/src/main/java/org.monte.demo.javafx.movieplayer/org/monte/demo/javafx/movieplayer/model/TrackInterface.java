/*
 * @(#)TrackInterface.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.model;

import java.util.Locale;
import java.util.Map;

/// An interface for [javafx.scene.media.Track].
public interface TrackInterface {
    Locale getLocale();

    Map<String, Object> getMetadata();

    String getName();

    long getTrackID();
}
