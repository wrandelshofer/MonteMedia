/*
 * @(#)AudioGrabber.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.monte.media.av.codec.audio.AudioFormatKeys.SilenceBugKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.fromAudioFormat;

/**
 * This runnable grabs audio samples and enqueues them into the specified
 * BlockingQueue. This runnable must be called twice a second.
 */
class AudioGrabber implements Runnable, AutoCloseable {

    private final TargetDataLine line;
    private final BlockingQueue<Buffer> queue;
    private final Integer audioTrack;
    private final long startTime;
    private long totalSampleCount;
    private ScheduledFuture<?> future;
    private long sequenceNumber;
    private final ScreenRecorder recorder;
    private float audioLevelLeft = AudioSystem.NOT_SPECIFIED;
    private float audioLevelRight = AudioSystem.NOT_SPECIFIED;
    private final AtomicLong stopTime = new AtomicLong(Long.MAX_VALUE);

    public AudioGrabber(ScreenRecorder recorder, final Mixer mixer, final Format audioFormat, final int audioTrack, long startTime, BlockingQueue<Buffer> queue)
            throws LineUnavailableException {
        this.audioTrack = audioTrack;
        this.queue = queue;
        this.startTime = startTime;
        this.recorder = recorder;

        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, AudioFormatKeys.toAudioFormat(audioFormat));
        this.line = initializeAudioLine(mixer, lineInfo);

        configureAudioLine();
    }

    private TargetDataLine initializeAudioLine(Mixer mixer, DataLine.Info lineInfo) throws LineUnavailableException {
        if (mixer != null) {
            return (TargetDataLine) mixer.getLine(lineInfo);
        } else {
            return (TargetDataLine) AudioSystem.getLine(lineInfo);
        }
    }

    private void configureAudioLine() throws LineUnavailableException {
        unmuteLine();
        setMinimumVolume();
        line.open();
        line.start();
    }

    private void unmuteLine() {
        try {// Make sure the line is not muted
            BooleanControl muteControl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
            muteControl.setValue(false);
        } catch (IllegalArgumentException e) {
            // We can't unmute the line from Java
        }
    }

    private void setMinimumVolume() {
        try { // Make sure the volume of the line is bigger than 0.2
            FloatControl volumeControl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
            volumeControl.setValue(Math.max(volumeControl.getValue(), 0.2f));
        } catch (IllegalArgumentException e) {
            // We can't change the volume from Java
        }
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    @Override
    public void close() {
        line.close();
    }

    public void setStopTime(long newValue) {
        this.stopTime.set(newValue);
    }

    public long getStopTime() {
        return this.stopTime.get();
    }

    @Override
    public void run() {
        try {
            grabAudio();
        } catch (Throwable ex) {
            recorder.recordingFailed(ex);
        }
    }

    public void grabAudio() {
        Buffer buf = new Buffer();
        AudioFormat lineFormat = line.getFormat();
        buf.format = fromAudioFormat(lineFormat).append(SilenceBugKey, true);

        int bufferSize = calculateBufferSize(lineFormat);
        byte[] bdat = new byte[bufferSize];
        buf.data = bdat;

        int count = line.read(bdat, 0, bdat.length);
        if (count > 0) {
            processAudioData(buf, lineFormat, bdat, count);
        }
    }

    private int calculateBufferSize(AudioFormat lineFormat) {
        // For even sample rates, we select a buffer size that can
        // hold half a second of audio. This allows audio/video interleave
        // twice a second, as recommended for AVI and QuickTime movies.
        // For odd sample rates, we have to select a buffer size that can hold
        // one second of audio.
        int bufferSize = lineFormat.getFrameSize() * (int) lineFormat.getSampleRate();
        if (((int) lineFormat.getSampleRate() & 1) == 0) {
            bufferSize /= 2;
        }
        return bufferSize;
    }

    private void processAudioData(Buffer buf, AudioFormat lineFormat, byte[] bdat, Integer count) {
        computeAudioLevel(bdat, count, lineFormat);
        setBufferProperties(buf, lineFormat, count);

        if (isRecordingComplete(buf)) {
            truncateBuffer(buf, lineFormat);
            future.cancel(false);
        }

        if (buf.sampleCount > 0) {
            enqueueBuffer(buf);
        }

        totalSampleCount += buf.sampleCount;
    }

    private void setBufferProperties(Buffer buf, AudioFormat lineFormat, int count) {
        Rational sampleRate = Rational.valueOf(lineFormat.getSampleRate());
        Rational frameRate = Rational.valueOf(lineFormat.getFrameRate());

        buf.sampleCount = count / (lineFormat.getSampleSizeInBits() / 8 * lineFormat.getChannels());
        buf.sampleDuration = sampleRate.inverse();
        buf.offset = 0;
        buf.sequenceNumber = sequenceNumber++;
        buf.length = count;
        buf.track = audioTrack;
        buf.timeStamp = new Rational(totalSampleCount, 1).divide(frameRate);
    }

    private boolean isRecordingComplete(Buffer buf) {
        Rational stopTS = new Rational(getStopTime() - startTime, 1000);
        return buf.timeStamp.add(buf.sampleDuration.multiply(buf.sampleCount)).compareTo(stopTS) > 0;
    }

    private void truncateBuffer(Buffer buf, AudioFormat lineFormat) {
        Rational stopTS = new Rational(getStopTime() - startTime, 1000);
        buf.sampleCount = Math.max(0, (int) Math.ceil(stopTS.subtract(buf.timeStamp).divide(buf.sampleDuration).floatValue()));
        buf.length = buf.sampleCount * (lineFormat.getSampleSizeInBits() / 8 * lineFormat.getChannels());
    }

    private void enqueueBuffer(Buffer buf) {
        try {
            queue.put(buf);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculates the root-mean-square average of continuous samples. For
     * four samples, the formula looks like this:
     * <pre>
     * rms = sqrt( (x0^2 + x1^2 + x2^2 + x3^2) / 4)
     * </pre> Resources:
     * http://www.jsresources.org/faq_audio.html#calculate_power
     *
     * @param data
     * @param length
     * @param format
     */
    private void computeAudioLevel(byte[] data, Integer length, AudioFormat format) {
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

    private void computeAudioLevel8Bit(byte[] data, Integer length, AudioFormat format) {
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

    private void computeAudioLevel16Bit(byte[] data, Integer length, AudioFormat format) {
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
        double sum = 0;
        for (int i = offset; i < length; i += stride) {
            int value = ((data[i]) << 8) | (data[i + 1] & 0xff);
            sum += value * value;
        }
        double rms = Math.sqrt(sum / ((length - offset) / stride));
        return (float) (rms / 32768);
    }

    private float computeAudioLevelSigned16LE(byte[] data, int offset, int length, int stride) {
        double sum = 0;
        for (int i = offset; i < length; i += stride) {
            int value = ((data[i + 1]) << 8) | (data[i] & 0xff);
            sum += value * value;
        }
        double rms = Math.sqrt(sum / ((length - offset) / stride));
        return (float) (rms / 32768);
    }

    private float computeAudioLevelSigned8(byte[] data, int offset, int length, int stride) {
        double sum = 0;
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

    public float getAudioLevelLeft() {
        return audioLevelLeft;
    }

    public float getAudioLevelRight() {
        return audioLevelRight;
    }
}