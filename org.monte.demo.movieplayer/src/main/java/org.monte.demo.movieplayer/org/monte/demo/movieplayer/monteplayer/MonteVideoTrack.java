/*
 * @(#)MonteVideoTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.image.WritableImage;
import org.monte.demo.movieplayer.model.AbstractVideoTrack;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.math.Rational;

import java.util.Locale;
import java.util.Map;

public class MonteVideoTrack extends AbstractVideoTrack {
    protected Buffer inBuffer = new Buffer();
    protected Buffer[] outBuffer = {new Buffer(), new Buffer()};
    protected int outBufferIndex = 0;
    private Codec codec;
    protected final ReadOnlyObjectWrapper<WritableImage> videoImage = new ReadOnlyObjectWrapper<>();
    private Rational currentStartTime = Rational.ZERO;
    private Rational currentEndTime = Rational.ZERO;

    public MonteVideoTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }

    Codec getCodec() {
        return codec;
    }

    void setCodec(Codec newValue) {
        codec = newValue;

    }

    void setWidth(int newValue) {
        width = newValue;
    }

    WritableImage getVideoImage() {
        return videoImage.get();
    }

    ReadOnlyProperty<WritableImage> videoImageProperty() {
        return videoImage.getReadOnlyProperty();
    }

    void setVideoImage(WritableImage newValue) {
        videoImage.set(newValue);
    }

    void setHeight(int newValue) {
        height = newValue;
    }

    public Rational getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(Rational currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public Rational getCurrentStartTime() {
        return currentStartTime;
    }

    public void setCurrentStartTime(Rational currentStartTime) {
        this.currentStartTime = currentStartTime;
    }
}
