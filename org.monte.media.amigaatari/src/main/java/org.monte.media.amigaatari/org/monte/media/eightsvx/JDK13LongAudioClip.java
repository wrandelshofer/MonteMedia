/*
 * @(#)JDK13LongAudioClip.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.eightsvx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * JDK13AudioClip.
 *
 * @author Werner Randelshofer
 */
public class JDK13LongAudioClip implements LoopableAudioClip, Runnable {
    /**
     * The data line used for audio output.
     */
    private SourceDataLine dataLine;
    /**
     * This buffer holds the audio samples of the clip.
     */
    private byte[] samples;
    /**
     * The sample rate of the audio data.
     */
    private int sampleRate;
    /**
     * The position of the play head (counted in sample frames).
     */
    private int framePosition;
    /**
     * Holds the loop start value.
     */
    private int loopStart;
    /**
     * Holds the loop end value + 1.
     */
    private int loopEnd;

    /**
     * Loop count.
     * LOOP_CONTINUOUSLY indicates an endless loop.
     */
    private int loopCount;
    /**
     * The only place where the thread variable is changed is in the loop(int)
     * method and in the stop() method.
     * For all other methods this variable is strictly <bold>read only</bold>!
     */
    private volatile Thread thread;

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
    public JDK13LongAudioClip(byte[] samples, int sampleRate, int volume, float pan) {
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.volume = volume;
        this.pan = pan;
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.loopStart = 0;
        this.loopEnd = samples.length;
    }

    /**
     * Starts playing this audio clip in a loop.
     */
    public void loop() {
        stop();
        framePosition = 0;
        loop(LOOP_CONTINUOUSLY);
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
    public synchronized void loop(int count) {
        stop();
        try {
            dataLine = createDataLine();
            dataLine.open();
            if (dataLine.isControlSupported(FloatControl.Type.BALANCE)) {
                FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.BALANCE);
                control.setValue(pan);
            }
            if (dataLine.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.VOLUME);
                control.setValue(volume / 64f);
            }
            loopCount = count;
            thread = new Thread(this, "JDK13AudioClip");
            thread.setPriority(Thread.NORM_PRIORITY + 1);
            thread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Starts playing this audio clip. Each time this method is called,
     * the clip is restarted from the beginning.
     */
    public void play() {
        stop();
        framePosition = 0;
        loop(0);
    }

    /**
     * Starts the audio clip.
     */
    public void start() {
        loop(0);
    }

    /**
     * Stops playing this audio clip.
     */
    public synchronized void stop() {
        Thread t = thread;
        if (thread != null) {
            thread = null;
            try {
                t.join();
            } catch (InterruptedException e) {
            }
            dataLine = null;
        }
    }

    /**
     * Sets the media position in sample frames.  The position is zero-based;
     * the first frame is frame number zero.  When the clip begins playing the
     * next time, it will start by playing the frame at this position.
     * <p>
     * To obtain the current position in sample frames, use the
     * <code>{@link DataLine#getFramePosition getFramePosition}</code>
     * method of <code>DataLine</code>.
     *
     * @param param the desired new media position, expressed in sample frames
     */
    public void setFramePosition(int param) {
        framePosition = param;
    }

    /**
     * Obtains the media duration in microseconds
     *
     * @return the media duration, expressed in microseconds,
     * or <code>AudioSystem.NOT_SPECIFIED</code> if the line is not open.
     * @see AudioSystem#NOT_SPECIFIED
     */
    public long getMicrosecondLength() {
        //return dataLine.getMicrosecondLength();
        return samples.length / sampleRate;
    }

    /**
     * Obtains the current position in the audio data, in microseconds.
     * The microsecond position measures the time corresponding to the number
     * of sample frames captured by, or rendered from, the line since it was opened.
     * The level of precision is not guaranteed.  For example, an implementation
     * might calculate the microsecond position from the current frame position
     * and the audio sample frame rate.  The precision in microseconds would
     * then be limited to the number of microseconds per sample frame.
     *
     * @return the number of microseconds of data processed since the line was opened
     */
    public long getMicrosecondPosition() {
        SourceDataLine sdl = dataLine;
        return (sdl == null) ? 0 : sdl.getMicrosecondPosition();
    }

    /**
     * Sets the first and last sample frames that will be played in
     * the loop.  The ending point must be greater than
     * or equal to the starting point, and both must fall within the
     * the size of the loaded media.  A value of 0 for the starting
     * point means the beginning of the loaded media.  Similarly, a value of -1
     * for the ending point indicates the last frame of the media.
     *
     * @param start the loop's starting position, in sample frames (zero-based)
     * @param end   the loop's ending position, in sample frames (zero-based), or
     *              -1 to indicate the final frame
     * @throws IllegalArgumentException if the requested
     *                                  loop points cannot be set, usually because one or both falls outside
     *                                  the media's duration or because the ending point is
     *                                  before the starting point
     */
    public void setLoopPoints(int start, int end) {
        if (start < 0 || start >= samples.length || end < start && end != -1 || end >= samples.length)
            throw new IllegalArgumentException("start:" + start + " end:" + end);
        loopStart = start;
        loopEnd = (end == -1) ? samples.length : end + 1;
    }
/*    
    private static Mixer mixer;
    public synchronized static Mixer getMixer() {
        if (mixer == null) {
            mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
                if (mixer.isControlSupported(FloatControl.Type.BALANCE)) {
                    FloatControl control = (FloatControl) mixer.getControl(FloatControl.Type.BALANCE);
                    control.setValue(-1f);
                    System.out.println("MIXER pan supported");
                } else System.out.println("MIXER pan NOT supported");
        }
        return mixer;
    }*/


    private SourceDataLine createDataLine() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(
                (float) sampleRate,
                8, //int�sampleSizeInBits
                1, //int�channels
                true, //boolean�signed,
                true //boolean�bigEndian
        );
        Line.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sdl;
        /*Mixer m = getMixer();
        if (m != null) {
        sdl = (SourceDataLine) m.getLine(lineInfo);
        } else {*/
        sdl = (SourceDataLine) AudioSystem.getLine(lineInfo);
        //}
        /*
        Control[] c = sdl.getControls();
        for (int i=0; i < c.length; i++) {
            System.out.println(i+":"+c);
        }*/
        return sdl;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        dataLine.start();

        byte[] buf = new byte[512];
        if (loopCount > 0 && framePosition < loopEnd) {
            while (thread == Thread.currentThread()
                    && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                // Play until we reach the loop end marker
                while (thread == Thread.currentThread()
                        && framePosition < loopEnd) {
                    System.arraycopy(samples, framePosition, buf, 0, Math.min(512, loopEnd - framePosition));
                    framePosition += dataLine.write(buf, 0, Math.min(512, loopEnd - framePosition));
                    //System.out.println("JDK13AudioClip loop feed:"+framePosition);
                }
                // decrement the loop counter and set the framePosition to the
                // loop start marker
                if (thread == Thread.currentThread()
                        && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                    if (loopCount != Clip.LOOP_CONTINUOUSLY) loopCount--;
                    framePosition = loopStart;
                }
            }
        }

        // Play until we reach the end of the samples
        while (thread == Thread.currentThread()
                && framePosition < samples.length) {
            System.arraycopy(samples, framePosition, buf, 0, Math.min(512, samples.length - framePosition));
            framePosition += dataLine.write(buf, 0, Math.min(512, samples.length - framePosition));
        }

        // Wait until the line has played all the samples
        // that we have provided so far
        if (thread == Thread.currentThread()) {
            dataLine.drain();
        }

        // We stop and close the data line immediately after playback
        // to free resources as soon as possible
        dataLine.stop();
        dataLine.close();
        if (thread == null) System.out.println(this + " PRELIMINARY finish");
        else System.out.println(this + " liberate finish");
    }
}
