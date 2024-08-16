/*
 * @(#)AbstractVideoTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.model;

import java.util.Locale;
import java.util.Map;

public abstract class AbstractVideoTrack extends AbstractTrack implements VideoTrackInterface {
    protected int height;
    protected int width;

    public AbstractVideoTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
