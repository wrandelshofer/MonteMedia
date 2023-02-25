/*
 * @(#)AudioMixer.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.anim;

import org.monte.media.math.Rational;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.Arrays;

/**
 * Mixes multiple audio tracks into a single 16-bit PCM stereo audio track.
 */
public class AudioMixer {
    /**
     * The data array holds the mixed audio track.
     * <p>
     * The 16-bit samples are stored in 32-bit integers, so that the mixer
     * can handle intermediate results which are too loud for play-back.
     * <p>
     * Two data elements represent one stereo audio sample.
     * <p>
     * A data element at an even index contains an audio samples for the
     * left channel.
     * A data element at an odd index contains audio samples for the right
     * channel.
     */
    private int[] data = new int[0];

    private final static AudioFormat stereoFormat = new AudioFormat(
            44100,
            16,
            2, true, true);
    private final static AudioFormat monoFormat = new AudioFormat(
            44100,
            16,
            1, true, true);

    /**
     * Adds the provided audio samples to the audio track of the mixer.
     *
     * @param in        the audio samples to be added
     * @param startTime the start time in seconds at which the audio samples
     *                  are to be added to the audio track
     * @param repeats   the number of repeats
     * @param volume    the desired audio volume.
     *                  The valid range of values is 0.0 to Float.MAX_VALUE.
     * @param pan       the desired stereo pan (or balance).
     *                  Supported values are -1: left only, 0: left and right,
     */
    public void add(AudioInputStream in, Rational startTime, int repeats, float volume, int pan) throws IOException {
        if (!(volume >= 0f && !Float.isNaN(volume))) {
            throw new IllegalArgumentException("volume: " + volume);
        }

        float leftVolume, rightVolume;
        switch (pan) {
            case -1:
                leftVolume = volume;
                rightVolume = 0;
                mixMono(in, startTime, repeats, leftVolume, rightVolume);
                break;
            case 0:
                leftVolume = rightVolume = volume;
                mixStereo(in, startTime, repeats, leftVolume, rightVolume);
                break;
            case 1:
                leftVolume = 0;
                rightVolume = 1;
                mixMono(in, startTime, repeats, leftVolume, rightVolume);
                break;
            default:
                throw new IllegalArgumentException("pan: " + pan);
        }
    }

    private final byte[] tmp = new byte[1024];

    public int getSampleCount() {
        return data.length / 2;
    }

    private void mixMono(AudioInputStream in, Rational startTime, int repeats, float leftVolume, float rightVolume) throws IOException {
        int destOffset = (int) startTime.floorNumerator((int) monoFormat.getSampleRate());
        try (AudioInputStream converted = AudioSystem.getAudioInputStream(monoFormat, in)) {
            converted.mark(Integer.MAX_VALUE);
            for (int r = 0; r < repeats; r++) {
                for (int count = converted.read(tmp); count != -1; count = converted.read(tmp)) {
                    mixMono(tmp, 0, count, destOffset, leftVolume, rightVolume);
                    destOffset += count / 2;
                }
                converted.reset();
            }
        }
    }

    private void mixMono(byte[] bytes, int off, int len, int destOffset, float leftVolume, float rightVolume) {
        int j = destOffset * 2;
        ensureCapacity(destOffset + len / 2);
        for (int i = 0; i < len; i += 2) {
            short inMono = (short) (((bytes[off + i] & 0xff) << 8) | ((bytes[off + i + 1] & 0xff) << 0));
            int outLeft = (int) (inMono * leftVolume);
            int outRight = (int) (inMono * rightVolume);
            data[j++] += outLeft;
            data[j++] += outRight;
        }
    }

    /**
     * Ensures capacity for the specified number of stereo samples.
     */
    private void ensureCapacity(int size) {
        if (data.length < size * 2) {
            data = Arrays.copyOf(data, size * 2);
        }
    }


    private void mixStereo(AudioInputStream in, Rational startTime, int repeats, float leftVolume, float rightVolume) throws IOException {
        int destOffset = (int) startTime.floorNumerator((int) monoFormat.getSampleRate());
        try (AudioInputStream converted = AudioSystem.getAudioInputStream(stereoFormat, in)) {
            converted.mark(Integer.MAX_VALUE);
            for (int r = 0; r < repeats; r++) {
                for (int count = converted.read(tmp); count != -1; count = converted.read(tmp)) {
                    mixStereo(tmp, 0, count, destOffset, leftVolume, rightVolume);
                    destOffset += count / 4;
                }
                in.reset();
            }
        }
    }

    private void mixStereo(byte[] bytes, int off, int len, int destOffset, float leftVolume, float rightVolume) {
        int j = destOffset * 2;
        ensureCapacity(destOffset + len / 4);
        for (int i = 0; i < len; i += 4) {
            short inLeft = (short) (((bytes[off + i] & 0xff) << 8) | ((bytes[off + i + 1] & 0xff) << 0));
            short inRight = (short) (((bytes[off + i + 2] & 0xff) << 8) | ((bytes[off + i + 3] & 0xff) << 0));
            int outLeft = (int) (inLeft * leftVolume);
            int outRight = (int) (inRight * rightVolume);
            data[j++] += outLeft;
            data[j++] += outRight;
        }
    }

    public int getMaxAbsoluteValue() {
        int maxAbs = 0;
        for (int i = 0; i < data.length; i++) {
            int abs = Math.abs(data[i]);
            if (abs > maxAbs) {
                maxAbs = abs;
            }
        }
        return maxAbs;
    }

    public byte[] toByteArray() {
        int maxAbs = getMaxAbsoluteValue();
        byte[] b = new byte[getSampleCount() * 4];
        if (maxAbs <= Short.MAX_VALUE) {
            int j = 0;
            for (int i = 0, n = data.length; i < n; i++) {
                int sample = data[i];
                b[j++] = (byte) ((sample & 0xff00) >> 8);
                b[j++] = (byte) ((sample & 0xff));
            }
        } else {
            float volume = Short.MAX_VALUE / (float) maxAbs;
            int j = 0;
            for (int i = 0, n = data.length; i < n; i++) {
                int sample = (int) (data[i] * volume);
                b[j++] = (byte) ((sample & 0xff00) >> 8);
                b[j++] = (byte) ((sample & 0xff));
            }
        }
        return b;
    }
}
