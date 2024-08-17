/*
 * @(#)MonteVideoTrack.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.WritableImage;
import org.monte.demo.javafx.movieplayer.model.AbstractAudioTrack;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

import javax.sound.sampled.SourceDataLine;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MonteAudioTrack extends AbstractAudioTrack implements MonteTrackInterface {
    protected long renderTimeValidUntilNanoTime;
    private SourceDataLine sourceDataLine;
    /**
     * The dispatcher.
     */
    protected ExecutorService dispatcher = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, MonteAudioTrack.this + "-worker");
        }
    });
    public MonteAudioTrack(Locale locale, long trackId, String name, Map<String, Object> metadata) {
        super(locale, trackId, name, metadata);
    }

    protected Buffer inBuffer = new Buffer();
    protected Buffer outBufferA = new Buffer();
    protected Buffer outBufferB = new Buffer();
    private Codec codec;
    protected final ReadOnlyObjectWrapper<WritableImage> videoImage = new ReadOnlyObjectWrapper<>();
    private Rational renderedStartTime = Rational.ZERO;
    private Rational renderedEndTime = Rational.ZERO;
    private Format format;


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

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setSourceDataLine(SourceDataLine sourceDataLine) {
        this.sourceDataLine = sourceDataLine;
    }


    @Override
    public Buffer swapOutBuffers() {
        var swap = outBufferA;
        outBufferA = outBufferB;
        outBufferB = swap;
        return outBufferA;
    }

    public SourceDataLine getSourceDataLine() {
        return sourceDataLine;
    }

    public Rational getRenderedEndTime() {
        return renderedEndTime;
    }

    public void setRenderedEndTime(Rational seconds) {
        this.renderedEndTime = seconds;
    }

    public Rational getRenderedStartTime() {
        return renderedStartTime;
    }

    public void setRenderedStartTime(Rational seconds) {
        this.renderedStartTime = seconds;
    }

    @Override
    public void dispose() {
        if (sourceDataLine != null) {
            sourceDataLine.close();
            sourceDataLine = null;
        }
    }
}
