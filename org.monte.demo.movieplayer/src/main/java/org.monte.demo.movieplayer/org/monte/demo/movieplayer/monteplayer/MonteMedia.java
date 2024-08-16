/*
 * @(#)MonteMedia.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import org.monte.demo.movieplayer.model.AbstractMedia;

import java.io.File;

public class MonteMedia extends AbstractMedia {
    protected final ReadOnlyObjectWrapper<WritableImage> videoImage = new ReadOnlyObjectWrapper<>();

    public MonteMedia(String source) {
        super(source);
    }

    public MonteMedia(File source) {
        super(source.toURI().toString());
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

    void setWidth(int newValue) {
        width.set(newValue);
    }

    void setHeight(int newValue) {
        height.set(newValue);
    }

    public WritableImage getVideoImage() {
        return videoImage.get();
    }

    public ReadOnlyProperty<WritableImage> videoImageProperty() {
        return videoImage.getReadOnlyProperty();
    }

    void setVideoImage(WritableImage newValue) {
        videoImage.set(newValue);
    }

}
