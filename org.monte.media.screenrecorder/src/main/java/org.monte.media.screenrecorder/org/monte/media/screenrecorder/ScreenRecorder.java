/*
 * @(#)ScreenRecorder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import javax.sound.sampled.Mixer;
import java.util.function.BiConsumer;

public interface ScreenRecorder {
    void addChangeListener(BiConsumer<State, State> handler);

    void removeChangeListener(BiConsumer<State, State> handler);

    void setAudioMixer(Mixer mixer);

    void start();

    void stop();

}
