/*
 * @(#)MonteVideoTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.image.WritableImage;
import org.monte.demo.javafx.movieplayer.model.AbstractVideoTrack;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

import java.util.Locale;
import java.util.Map;

public class MonteVideoTrack extends AbstractVideoTrack implements MonteTrackInterface {
    protected Buffer inBuffer = new Buffer();
    protected Buffer outBufferA = new Buffer();
    protected Buffer outBufferB = new Buffer();
    private Codec codec;
    protected final ReadOnlyObjectWrapper<WritableImage> videoImage = new ReadOnlyObjectWrapper<>();
    private Rational renderedStartTIme = Rational.ZERO;
    private Rational renderedEndTime = Rational.ZERO;
    private Format format;

    public MonteVideoTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }

    public Codec getCodec() {
        return codec;
    }

    @Override
    public Buffer getInBuffer() {
        return inBuffer;
    }

    @Override
    public Buffer getOutBufferA() {
        return outBufferA;
    }

    @Override
    public Buffer getOutBufferB() {
        return outBufferB;
    }

    @Override
    public Format getFormat() {
        return format;
    }


    @Override
    public void setCodec(Codec newValue) {
        codec = newValue;

    }

    void setFormat(Format newValue) {
        format = newValue;

    }


    void setWidth(int newValue) {
        width = newValue;
    }

    WritableImage getVideoImage() {
        return videoImage.get();
    }

    @Override
    public Buffer swapOutBuffers() {
        var swap = outBufferA;
        outBufferA = outBufferB;
        outBufferB = swap;
        return outBufferA;
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

    public Rational getRenderedEndTime() {
        return renderedEndTime;
    }

    public void setRenderedEndTime(Rational seconds) {
        this.renderedEndTime = seconds;
    }

    public Rational getRenderedStartTime() {
        return renderedStartTIme;
    }

    public void setRenderedStartTime(Rational seconds) {
        this.renderedStartTIme = seconds;
    }

    @Override
    public void dispose() {

    }
}
