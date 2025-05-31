/*
 * @(#)SimpleScreenRecorder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.QuickTimeWriter;
import org.monte.media.quicktime.codec.sprite.SpriteFormatKeys;

import javax.sound.sampled.Mixer;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SimpleScreenRecorder implements ScreenRecorder {
    private final ScreenRecorderConfig config;
    private final CopyOnWriteArraySet<BiConsumer<State, State>> listeners = new CopyOnWriteArraySet<>();
    private AtomicReference<State> state = new AtomicReference<>(State.CREATED);
    private SequencedSet<Sampler> samplers;
    private SampleProducer producer;
    private SampleConsumer consumer;
    private MovieWriter w;

    public SimpleScreenRecorder(ScreenRecorderConfig config) {
        this.config = config;
    }


    @Override
    public void addChangeListener(BiConsumer<State, State> handler) {
        listeners.add(handler);
    }

    @Override
    public void removeChangeListener(BiConsumer<State, State> handler) {
        listeners.remove(handler);
    }

    @Override
    public void setAudioMixer(Mixer mixer) {

    }

    private boolean tryToSetState(State newState) {
        switch (newState) {
            case CREATED -> {
                return false;
            }
            case RECORDING -> {
                if (state.compareAndSet(State.CREATED, State.RECORDING)) {
                    notifyListeners(State.CREATED, State.RECORDING);
                    return true;
                }
            }
            case DONE -> {
                if (state.compareAndSet(State.RECORDING, State.DONE)) {
                    notifyListeners(State.RECORDING, State.DONE);
                    return true;
                }
            }
            case FAILED -> {
                if (state.compareAndSet(State.RECORDING, State.FAILED)) {
                    notifyListeners(State.RECORDING, State.FAILED);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (!tryToSetState(State.RECORDING)) {
            return;
        }
        try {
            samplers = createSamplers();
            producer = new SimpleSampleProducer(samplers);
            producer.start();
            consumer = new SimpleSampleConsumer(producer.getSamples(), w);
            consumer.start();
        } catch (IOException e) {
            tryToSetState(State.FAILED);
            throw new RuntimeException(e);
        }
    }

    private SequencedSet<Sampler> createSamplers() throws IOException {
        var samplers = new LinkedHashSet<Sampler>();
        w = new QuickTimeWriter(new File(config.movieFolder(), "Screen Recording " +
                DateTimeFormatter.ofPattern("uuuu-MM-dd 'at' HH.mm.ss.SSS").format(LocalDateTime.now()) + ".mov"));
        w.setFileFormat(config.fileCodecFormat().append(VideoFormatKeys.HeightKey, config.captureArea().height,
                VideoFormatKeys.WidthKey, config.captureArea().width));


        Sampler s;
        if (config.audioFormat() != null) {
            samplers.add(s = new AudioSampler(config.mixer(), config.audioFormat(), w.getTrackCount(), Rational.valueOf(1, 2), Rational.ZERO));
            w.addTrack(s.getFormat());
        }
        if (config.screenFormat() != null) {
            Rational interval = config.screenFormat().get(FormatKeys.FrameRateKey).inverse();
            Format screenFormat = config.screenFormat().append(VideoFormatKeys.HeightKey, config.captureArea().height,
                    VideoFormatKeys.WidthKey, config.captureArea().width);
            samplers.add(s = new ScreenSampler(config.captureArea(), config.graphicsDevice(),
                    screenFormat, w.getTrackCount(),
                    interval.multiply(4), Rational.ZERO));
            samplers.add(s = new ScreenSampler(config.captureArea(), config.graphicsDevice(),
                    screenFormat, w.getTrackCount(),
                    interval.multiply(4), interval));
            samplers.add(s = new ScreenSampler(config.captureArea(), config.graphicsDevice(),
                    screenFormat, w.getTrackCount(),
                    interval.multiply(4), interval.multiply(2)));
            samplers.add(s = new ScreenSampler(config.captureArea(), config.graphicsDevice(),
                    screenFormat, w.getTrackCount(),
                    interval.multiply(4), interval.multiply(3)));
            w.addTrack(s.getFormat());
        }
        if (config.mouseFormat() != null) {
            samplers.add(s = new MouseSampler(config.captureArea(), config.graphicsDevice(),
                    config.mouseFormat().append(VideoFormatKeys.EncodingKey, SpriteFormatKeys.ENCODING_QUICKTIME_SPRITE), w.getTrackCount(),
                    config.mouseFormat().get(FormatKeys.FrameRateKey).inverse(),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_IMAGE_KEY),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_PRESSED_IMAGE_KEY),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_OFFSET_KEY, new Point(0, 0)), Rational.ZERO));
            w.addTrack(s.getFormat());
        }
        return samplers;
    }

    private void notifyListeners(State oldState, State newState) {
        for (var l : listeners) {
            l.accept(oldState, newState);
        }
    }

    @Override
    public void stop() {
        if (!tryToSetState(State.DONE)) {
            return;
        }
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }

    }
}
