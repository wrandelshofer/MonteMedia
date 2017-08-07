/* @(#)JDK13AppletAudioClip.java
 * Copyright © 2003-2013 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.eightsvx;

import javax.sound.sampled.*;
import java.util.*;
import java.io.*;
/**
 * JDK13AppletAudioClip.
 * Supports playback of JDK13_SAMPLE_RATE Hz linear 8 encoded PCM data.
 * This class is designed and tuned for use in applets. It is not recommended
 * to use this class in Java applications.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id: JDK13AppletAudioClip.java 364 2016-11-09 19:54:25Z werner $
 */
@SuppressWarnings("unchecked")
public class JDK13AppletAudioClip implements LoopableAudioClip, Runnable {
    /**
     * This buffer holds the audio samples of the clip.
     */
    private byte[] samples;
    /**
     * All instances share this mixer.
     * Aquiring a line from a mixer is faster than aquiring it from
     * AudioSystem directly.
     */
    private static Mixer mixer;
    
    /**
     * This vector holds a pool of SourceDataLine objects.
     * All SourceDataLine objects in this vector are open() and stopped.
     * This vector is used to improve the audio playback performance
     * because opening a SourceDataLine may take a long time.
     */
    private static LinkedList<SourceDataLine> lines = new LinkedList<SourceDataLine>();
    
    /**
     * The only place where the workerThread variable is changed is in the loop(int)
     * method and in the stop() method.
     * For all other methods this variable is strictly <bold>read only</bold>!
     */
    private volatile Thread workerThread;
    
    /**
     * Loop count.
     * LOOP_CONTINUOUSLY indicates an endless loop.
     */
    private int loopCount;
    
    /**
     * Represents a control for the volume on a line. 64 is the maximal
     * volume, 0 mutes the line.
     */
    private int volume;
    
    /**
     * The sample rate of the audio data.
     */
    private int sampleRate;
    
    /**
     * The relative pan of a stereo signal between two stereo
     * speakers. The valid range of values is -1.0 (left channel only) to 1.0
     * (right channel  only). The default is 0.0 (centered).
     */
    private float pan;
    /**
     * Holds the loop start value.
     */
    private int loopStart;
    /**
     * Holds the loop end value + 1.
     */
    private int loopEnd;
    
    
    
    /**
     * Creates a new instance.
     *
     * @param samples Array of signed linear 8-bit encoded audio samples.
     * @param volume The volume setting controls the loudness of the sound.
     * range 0 (mute) to 64 (maximal volume).
     * @param pan The relative pan of a stereo signal between two stereo
     * speakers. The valid range of values is -1.0 (left channel only) to 1.0
     * (right channel  only). The default is 0.0 (centered).
     */
    public JDK13AppletAudioClip(byte[] samples, int sampleRate, int volume, float pan)
    throws IOException {
        this.samples = samples;
        this.volume = volume;
        this.pan = pan;
        this.loopStart = 0;
        this.loopEnd = samples.length;
        this.sampleRate = sampleRate;
        // We get the mixer here to make sure it has been created
        // before the first time loop or play is called.
        try {
            getMixer();
        } catch (LineUnavailableException e) {
            throw new IOException(e.toString());
        }
        /*
        AudioInputStream src = new AudioInputStream(
        new ByteArrayInputStream(samples),
        new AudioFormat(
        (float) sampleRate, // sample rate
        8, //sampleSizeInBits
        1, //number of channels
        true, //isSigned
        true //isBigEndian
        ),
        samples.length);
        AudioInputStream in = AudioSystem.getAudioInputStream(
        new AudioFormat(
        (float) JDK13_SAMPLE_RATE, // sample rate
        8, //sampleSizeInBits
        1, //number of channels
        true, //isSigned
        true //isBigEndian
        ),
        src
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count;
        byte[] buf = new byte[512];
        while ((count = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, count);
        }
        samples = out.toByteArray();
         */
    }
    /**
     * Lazily creates the shared mixer instance and returns it.
     * Puts 8 lines into the pool.
     */
    private static Mixer getMixer() throws LineUnavailableException {
        if (mixer == null) {
            mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
            SourceDataLine[] l = new SourceDataLine[16];
            for (int i=0; i < 16; i++) {
                l[i] = aquireLine();
            }
            for (int i=0; i < 16; i++) {
                poolLine(l[i]);
            }
        }
        return mixer;
    }
    
    /**
     * Tries to get a SourceDataLine from the cache.
     * If none is available, a new line is created and opened.
     */
    private synchronized static SourceDataLine aquireLine()
    throws LineUnavailableException {
        SourceDataLine line;
        if (!lines.isEmpty()) {
            line = lines.removeFirst();
        } else {
            AudioFormat audioFormat = new AudioFormat(
            (float) 8000, // sample rate
            8, //sampleSizeInBits
            1, //number of channels
            true, //isSigned
            true //isBigEndian
            );
            
            Line.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            line = (SourceDataLine) getMixer().getLine(lineInfo);
            line.open();
            line.start();
            //System.out.println("aquireLine elapsed:"+(System.currentTimeMillis() - start));
        }
        return line;
    }
    
    /**
     * Adds a line to the pool and makes it available for reuse.
     * If more than 16 lines are in the cache, then the line is closed.
     */
    private synchronized static void poolLine(SourceDataLine line) {
        if (lines.size() < 16) {
            //line.flush();
            //line.stop();
            lines.add(line);
        } else {
            line.close();
        }
    }
    
    /** Sets the first and last sample frames that will be played in
     * the loop.  The ending point must be greater than
     * or equal to the starting point, and both must fall within the
     * the size of the loaded media.  A value of 0 for the starting
     * point means the beginning of the loaded media.  Similarly, a value of -1
     * for the ending point indicates the last frame of the media.
     * @param start the loop's starting position, in sample frames (zero-based)
     * @param end the loop's ending position, in sample frames (zero-based), or
     * -1 to indicate the final frame
     * @throws IllegalArgumentException if the requested
     * loop points cannot be set, usually because one or both falls outside
     * the media's duration or because the ending point is
     * before the starting point
     */
    public void setLoopPoints(int start, int end) {
        if (start < 0 || start >= samples.length || end < start && end != -1 || end >= samples.length)
            throw new IllegalArgumentException("start:"+start+" end:"+end);
        loopStart = start;
        loopEnd = (end == -1) ? samples.length : end + 1;
    }
    
    public void loop() {
        loop(LOOP_CONTINUOUSLY);
    }
    
    /** Starts looping playback from the current position.   Playback will
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
     * loop's end position to the loop's  start position, or
     * <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     * continue until interrupted
     *
     */
    public synchronized void loop(int count) {
        stop();
        loopCount = count;
        workerThread = new Thread(this, this.toString());
        workerThread.setPriority(Thread.NORM_PRIORITY + 1);
        workerThread.start();
    }
    
    public void play() {
        loop(0);
    }
    
    public synchronized void stop() {
        if (workerThread != null) {
            Thread t = workerThread;
            workerThread = null;
            /*
            try {
            t.join();
            } catch (InterruptedException e) {
            }*/
        }
    }
    
    /**
     * Sets the pan and volume settings of the data line.
     */
    private void configureDataLine(DataLine clip) {
        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            control.setValue(pan);
System.out.println("setPan:"+pan);            
        } else {
System.out.println("panning not supported "+pan);            
        }
        if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            control.setValue(volume / 64f);
        }
        
        if (clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
            control.setValue((float) sampleRate);
        } /*else {
           
           XXX we should resample our sound samples here to the current
           sample rate of the data line.
           
        } */
    }
    
    /** When an object implementing interface <code>Runnable</code> is used
     * to create a workerThread, starting the workerThread causes the object's
     * <code>run</code> method to be called in that separately executing
     * workerThread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     *
     */
    public void run() {
        //System.out.println("run "+hashCode());
        long start = System.currentTimeMillis();
        long mediaDuration = (samples.length * Math.max(loopCount, 1)) / 8;
        int framePosition = 0;
        
        SourceDataLine out = null;
        try {
            out = aquireLine();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("configureDataLine");
        configureDataLine(out);
        //out.start();
        byte[] buf = new byte[100];
        
        if (loopCount > 0 && framePosition < loopEnd) {
            while (workerThread == Thread.currentThread()
            && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                // Play until we reach the loop end marker
                while (workerThread == Thread.currentThread()
                && framePosition < loopEnd) {
                    System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, loopEnd - framePosition));
                    framePosition += out.write(buf, 0, Math.min(buf.length, loopEnd - framePosition));
                    //System.out.println("JDK13AudioClip loop feed:"+framePosition);
                }
                // decrement the loop counter and set the framePosition to the
                // loop start marker
                if (workerThread == Thread.currentThread()
                && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                    if (loopCount != Clip.LOOP_CONTINUOUSLY) {
                        loopCount--;
                        if (loopCount != 0) framePosition = loopStart;
                    }
                }
            }
            
            // Play until we reach the end of the samples
            while (workerThread == Thread.currentThread()
            && framePosition < samples.length) {
                System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, samples.length - framePosition));
                framePosition += out.write(buf, 0, Math.min(buf.length, samples.length - framePosition));
            }
        } else {
            // Play it once
            while (workerThread == Thread.currentThread()
            && framePosition < samples.length) {
                System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, samples.length - framePosition));
                framePosition += out.write(buf, 0, Math.min(buf.length, samples.length - framePosition));
            }
        }
        
        // We wait until the line has played all the samples
        // that we have provided
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            while (workerThread == Thread.currentThread() && mediaDuration > elapsed) {
                try {
                    Thread.sleep(Math.max(1, Math.min(mediaDuration - elapsed, 100)));
                } catch (InterruptedException e) {
                }
                elapsed = System.currentTimeMillis() - start;
            }
            
        // Flus the line if the user aborted playback
        if (workerThread != Thread.currentThread()) {
            out.flush();
        }
        
        // We put the line into the pool for future reuse.
        poolLine(out);
    }
}
