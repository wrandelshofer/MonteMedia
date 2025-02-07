/*
 * @(#)AudioGrabber.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.codec.audio.AudioFormatKeys;
import org.monte.media.math.Rational;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.monte.media.av.codec.audio.AudioFormatKeys.SilenceBugKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.fromAudioFormat;

/**
 * Samples audio in fixed intervals .
 */
public class AudioSampler implements Sampler {

    private final TargetDataLine line;
    private final int track;
    private final Rational interval;
    private long totalSampleCount;
    private ScheduledFuture<?> future;
    private long sequenceNumber;
    private float audioLevelLeft = AudioSystem.NOT_SPECIFIED;
    private float audioLevelRight = AudioSystem.NOT_SPECIFIED;
    private final AtomicLong stopTime = new AtomicLong(Long.MAX_VALUE);
    private Throwable exception;

    public AudioSampler(final Mixer mixer, final Format audioFormat, final int audioTrack, Rational interval) throws IOException {
        this.track = audioTrack;
        this.interval = interval;

        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, AudioFormatKeys.toAudioFormat(audioFormat));
        try {
            line = initializeAudioLine(mixer, lineInfo);
            configureAudioLine();
        } catch (LineUnavailableException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Buffer sample() {
        Buffer buf = new Buffer();
        buf.timeStamp = new Rational(System.nanoTime(), 1_000_000_000);
        if (exception != null) {
            buf.exception = exception;
            buf.setFlag(BufferFlag.DISCARD);
            return buf;
        }

        AudioFormat lineFormat = line.getFormat();
        buf.format = fromAudioFormat(lineFormat).append(SilenceBugKey, true);

        int bufferSize = calculateBufferSize(lineFormat);
        byte[] bdat = new byte[bufferSize];
        buf.data = bdat;

        int count = line.read(bdat, 0, bdat.length);
        if (count > 0) {
            setBufferProperties(buf, lineFormat, count);
            computeAudioLevel(bdat, count, lineFormat);
            totalSampleCount += buf.sampleCount;
        }
        return buf;
    }

    @Override
    public Rational getInterval() {
        return interval;
    }

    private void setBufferProperties(Buffer buf, AudioFormat lineFormat, int count) {
        Rational sampleRate = Rational.valueOf(lineFormat.getSampleRate());
        buf.sampleCount = count / (lineFormat.getSampleSizeInBits() / 8 * lineFormat.getChannels());
        buf.sampleDuration = sampleRate.inverse();
        buf.offset = 0;
        buf.sequenceNumber = sequenceNumber++;
        buf.length = count;
        buf.track = track;
    }

    /**
     * Calculates the root-mean-square average of continuous samples. For
     * four samples, the formula looks like this:
     * <pre>
     * rms = sqrt( (x0^2 + x1^2 + x2^2 + x3^2) / 4)
     * </pre> Resources:
     * <a href="http://www.jsresources.org/faq_audio.html#calculate_power">jsresources.org</a>
     *
     * @param data   an array with audio data samples
     * @param length the number of bytes in data that contain samples
     * @param format the format of the samples
     */
    private void computeAudioLevel(byte[] data, int length, AudioFormat format) {
        audioLevelLeft = audioLevelRight = AudioSystem.NOT_SPECIFIED;
        if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            switch (format.getSampleSizeInBits()) {
                case 8:
                    computeAudioLevel8Bit(data, length, format);
                    break;
                case 16:
                    computeAudioLevel16Bit(data, length, format);
                    break;
            }
        }
    }

    private void computeAudioLevel8Bit(byte[] data, int length, AudioFormat format) {
        switch (format.getChannels()) {
            case 1:
                audioLevelLeft = computeAudioLevelSigned8(data, 0, length, format.getFrameSize());
                break;
            case 2:
                audioLevelLeft = computeAudioLevelSigned8(data, 0, length, format.getFrameSize());
                audioLevelRight = computeAudioLevelSigned8(data, 1, length, format.getFrameSize());
                break;
        }
    }

    private void computeAudioLevel16Bit(byte[] data, int length, AudioFormat format) {
        if (format.isBigEndian()) {
            switch (format.getChannels()) {
                case 1:
                    audioLevelLeft = computeAudioLevelSigned16BE(data, 0, length, format.getFrameSize());
                    break;
                case 2:
                    audioLevelLeft = computeAudioLevelSigned16BE(data, 0, length, format.getFrameSize());
                    audioLevelRight = computeAudioLevelSigned16BE(data, 2, length, format.getFrameSize());
                    break;
            }
        } else {
            switch (format.getChannels()) {
                case 1:
                    audioLevelLeft = computeAudioLevelSigned16LE(data, 0, length, format.getFrameSize());
                    break;
                case 2:
                    audioLevelLeft = computeAudioLevelSigned16LE(data, 0, length, format.getFrameSize());
                    audioLevelRight = computeAudioLevelSigned16LE(data, 2, length, format.getFrameSize());
                    break;
            }
        }
    }

    private float computeAudioLevelSigned16BE(byte[] data, int offset, int length, int stride) {
        long sum = 0;
        for (int i = offset; i < length; i += stride) {
            int value = ((data[i]) << 8) | (data[i + 1] & 0xff);
            sum += (long) value * value;
        }
        double rms = Math.sqrt(sum / ((double) (length - offset) / stride));
        return (float) (rms / 32768);
    }

    private float computeAudioLevelSigned16LE(byte[] data, int offset, int length, int stride) {
        long sum = 0;
        for (int i = offset; i < length; i += stride) {
            int value = ((data[i + 1]) << 8) | (data[i] & 0xff);
            sum += (long) value * value;
        }
        double rms = Math.sqrt(sum / ((double) (length - offset) / stride));
        return (float) (rms / 32768);
    }

    private float computeAudioLevelSigned8(byte[] data, int offset, int length, int stride) {
        long sum = 0;
        for (int i = offset; i < length; i += stride) {
            int value = data[i];

            // FIXME - The java audio system records silence as -128 instead of 0.
            if (value != -128) {
                sum += value * value;
            }
        }
        double rms = Math.sqrt(sum / ((double) length / stride));
        return (float) (rms / 128);
    }

    private int calculateBufferSize(AudioFormat lineFormat) {
        int bufferSizeForOneSecond = lineFormat.getFrameSize() * (int) lineFormat.getSampleRate();
        Rational bufferSizeForInterval = interval.multiply(bufferSizeForOneSecond);
        return bufferSizeForInterval.intValue();
    }

    private TargetDataLine initializeAudioLine(Mixer mixer, DataLine.Info lineInfo) throws LineUnavailableException {
        if (mixer != null) {
            return (TargetDataLine) mixer.getLine(lineInfo);
        } else {
            return (TargetDataLine) AudioSystem.getLine(lineInfo);
        }
    }

    private void unmuteLine() {
        if (line == null) return;
        try {// Make sure the line is not muted
            BooleanControl muteControl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
            muteControl.setValue(false);
        } catch (IllegalArgumentException e) {
            // We can't unmute the line from Java
        }
    }

    private void setMinimumVolume() {
        if (line == null) return;
        try { // Make sure the volume of the line is bigger than 0.2
            FloatControl volumeControl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
            volumeControl.setValue(Math.max(volumeControl.getValue(), 0.2f));
        } catch (IllegalArgumentException e) {
            // We can't change the volume from Java
        }
    }

    private void configureAudioLine() throws LineUnavailableException {
        unmuteLine();
        setMinimumVolume();
        if (line != null) {
            line.open();
            line.start();
        }
    }

    @Override
    public void close() {
        if (line != null) {
            line.close();
        }
    }
}
