/*
 * @(#)SimpleScreenRecorder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.FormatKeys;
import org.monte.media.math.Rational;

import javax.sound.sampled.Mixer;
import java.io.IOException;
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
    private SampleProducer executor;

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
            executor = new SimpleSampleProducer(samplers);
            executor.start();
            ;
        } catch (IOException e) {
            tryToSetState(State.FAILED);
            throw new RuntimeException(e);
        }
    }

    private SequencedSet<Sampler> createSamplers() throws IOException {
        var samplers = new LinkedHashSet<Sampler>();
        if (config.audioFormat() != null) {
            samplers.add(new AudioSampler(config.mixer(), config.audioFormat(), samplers.size(), Rational.valueOf(1, 2)));
        }
        if (config.screenFormat() != null) {
            samplers.add(new ScreenSampler(config.captureArea(), config.graphicsDevice(), config.screenFormat(), samplers.size(),
                    config.screenFormat().get(FormatKeys.FrameRateKey).inverse()));
        }
        if (config.mouseFormat() != null) {
            samplers.add(new MouseSampler(config.captureArea(), config.graphicsDevice(), config.mouseFormat(), samplers.size(),
                    config.mouseFormat().get(FormatKeys.FrameRateKey).inverse(),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_IMAGE_KEY),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_PRESSED_IMAGE_KEY),
                    config.mouseFormat().get(MouseFormatKeys.CURSOR_OFFSET_KEY)));
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
        if (executor == null) return;
        executor.close();
        if (samplers == null) return;

    }
}
