/*
 * @(#)MonteMedia.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.AbstractMedia;
import org.monte.media.av.Format;

import java.io.File;

public class MonteMedia extends AbstractMedia {
    private Format format;

    public MonteMedia(String source) {
        super(source);
    }

    public MonteMedia(File source) {
        super(source.toURI().toString());
    }

    public void dispose() {
        for (var tr : tracks) {
            if (tr instanceof MonteTrackInterface) {
                MonteTrackInterface mtr = (MonteTrackInterface) tr;
                mtr.dispose();
            }
        }
    }

    /**
     * Package private method for {@link PlayerEngine}
     *
     * @param newValue the new value
     */
    void setError(Throwable newValue) {
        error.set(newValue);
    }

    /**
     * Package private method for {@link PlayerEngine}
     *
     * @param newValue the new value
     */
    void setDuration(Duration newValue) {
        duration.set(newValue);
    }

    public void setFormat(Format fileFormat) {
        this.format = fileFormat;
    }

    void setWidth(int newValue) {
        width.set(newValue);
    }

    void setHeight(int newValue) {
        height.set(newValue);
    }

}
