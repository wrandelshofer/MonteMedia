/*
 * @(#)JDK13ShortAudioClip.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.eightsvx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JDK13ShortAudioClip.
 *
 * @author Werner Randelshofer, Switzerland
 */
public class JDK13ShortAudioClip implements LoopableAudioClip {
    private Clip clip;
    /**
     * This buffer holds the audio samples of the clip.
     */
    private byte[] samples;
    /**
     * The sample rate of the audio data.
     */
    private int sampleRate;

    /**
     * Represents a control for the volume on a line. 64 is the maximal
     * volume, 0 mutes the line.
     */
    private int volume;

    /**
     * The relative pan of a stereo signal between two stereo
     * speakers. The valid range of values is -1.0 (left channel only) to 1.0
     * (right channel  only). The default is 0.0 (centered).
     */
    private float pan;
    private final int sampleCount;
    private AudioFormat audioFormat;
    private static Timer TIMER;
    private volatile TimerTask preventPopping;

    /**
     * Creates a new instance.
     *
     * @param samples    Array of signed linear 8-bit encoded audio samples.
     * @param sampleRate sampleRate of the audio samples.
     * @param volume     The volume setting controls the loudness of the sound.
     *                   range 0 (mute) to 64 (maximal volume).
     * @param pan        The relative pan of a stereo signal between two stereo
     *                   speakers. The valid range of values is -1.0 (left channel only) to 1.0
     *                   (right channel  only). The default is 0.0 (centered).
     */
    public JDK13ShortAudioClip(byte[] samples, int sampleRate, int volume, float pan) {
        // Add 1 second worth of silence to prevent popping
        this.samples = Arrays.copyOf(samples, samples.length + sampleRate);
        this.sampleRate = sampleRate;
        this.volume = volume;
        this.pan = pan;
        this.sampleCount = samples.length;
    }

    public synchronized void loop() {
        loop(LOOP_CONTINUOUSLY);
    }

    public synchronized void play() {
        stop();
        getOrCreateClip();
        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            control.setValue(pan);
        }
        if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            control.setValue(volume / 64f);
        }

        clip.setFramePosition(0);

        // This will start the clip with one second extra time
        clip.start();

        // We stop the clip before it has been completely played to prevent the popping sound
        preventPopping = new TimerTask() {
            @Override
            public void run() {
                if (preventPopping == this) {
                    stop();
                }
            }
        };
        getTimer().schedule(preventPopping, 1000L * sampleCount / sampleRate);

    }

    private Timer getTimer() {
        if (TIMER == null) {
            TIMER = new Timer();
        }
        return TIMER;
    }

    private Clip getOrCreateClip() {
        if (clip == null) {
            try {
                clip = createClip();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        return clip;
    }

    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            preventPopping = null;
        }
    }

    private AudioFormat getAudioFormat() {
        if (audioFormat == null) {
            audioFormat = new AudioFormat(
                    (float) sampleRate,
                    8, //int sampleSizeInBits
                    1, //int channels
                    true, //boolean signed,
                    true //boolean bigEndian
            );
        }
        return audioFormat;
    }

    private Clip createClip() throws LineUnavailableException {
        Clip c = AudioSystem.getClip();
        c.open(getAudioFormat(), samples, 0, samples.length);
        return c;
    }

    /**
     * Starts looping playback from the current position.   Playback will
     * continue to the loop's end point, then loop back to the loop start point
     * <code>count</code> times, and finally continue playback to the end of
     * the clip.
     * <p>
     * If the current position when this method is invoked is greater than the
     * loop end point, playback simply continues to the
     * end of the clip without looping.
     * <p>
     * A <code>count</code> value of 0 indicates that any current looping should
     * cease and playback should continue to the end of the clip.  The behavior
     * is undefined when this method is invoked with any other value during a
     * loop operation.
     * <p>
     * If playback is stopped during looping, the current loop status is
     * cleared; the behavior of subsequent loop and start requests is not
     * affected by an interrupted loop operation.
     *
     * @param count the number of times playback should loop back from the
     *              loop's end position to the loop's  start position, or
     *              <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     *              continue until interrupted
     */
    public void loop(int count) {
        stop();
        clip = getOrCreateClip();
        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            control.setValue(pan);
        }
        if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            control.setValue(volume / 64f);
        }
        clip.setFramePosition(0);
        clip.setLoopPoints(0, sampleCount);
        clip.loop(count);
    }

}
