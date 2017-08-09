/* @(#)JDK13ShortAudioClip.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

package org.monte.media.eightsvx;

import java.applet.*;
import javax.sound.sampled.*;
/**
 * JDK13ShortAudioClip.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0.1 2005-07-09 Removed unnecessary System.out.println call.
 * <br>1.0 April 21, 2003 Created.
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
    
    private AudioFormat audioFormat;
    
    /** Creates a new instance.
     *
     * @param samples Array of signed linear 8-bit encoded audio samples.
     * @param sampleRate sampleRate of the audio samples.
     * @param volume The volume setting controls the loudness of the sound.
     * range 0 (mute) to 64 (maximal volume).
     * @param pan The relative pan of a stereo signal between two stereo
     * speakers. The valid range of values is -1.0 (left channel only) to 1.0
     * (right channel  only). The default is 0.0 (centered).
     */
    public JDK13ShortAudioClip(byte[] samples, int sampleRate, int volume, float pan) {
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.volume = volume;
        this.pan = pan;
    }
    
    public synchronized void loop() {
        loop(LOOP_CONTINUOUSLY);
    }
    
    public synchronized void play() {
        stop();
        if (clip == null) {
            try {
                clip = createClip();
                clip.open(getAudioFormat(), samples.clone(), 0, samples.length);
                if (clip.isControlSupported(FloatControl.Type.PAN)) {
                    FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
                    control.setValue(pan);
                }
                if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                    FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                    control.setValue(volume / 64f);
                }    
                
                clip.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                throw new InternalError(e.getMessage());
            }
        }
    }
    
    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
    
    private AudioFormat getAudioFormat() {
        if (audioFormat == null) {
            audioFormat = new AudioFormat(
            (float) sampleRate,
            8, //int�sampleSizeInBits
            1, //int�channels
            true, //boolean�signed,
            true //boolean�bigEndian
            );
        }
        return audioFormat;
    }
    
    private Clip createClip() throws LineUnavailableException {
        Line.Info lineInfo = new DataLine.Info(Clip.class, getAudioFormat());
        Clip c;
        /*Mixer m = JDK13LongAudioClip.getMixer();
        if (m != null) {
        c = (Clip) m.getLine(lineInfo);
        } else {*/
        c = (Clip) AudioSystem.getLine(lineInfo);
        //}
        return c;
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
    public void loop(int count) {
        stop();
        try {
            clip = createClip();
            clip.open(getAudioFormat(), samples.clone(), 0, samples.length);
            if (clip.isControlSupported(FloatControl.Type.PAN)) {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
                control.setValue(pan);
            }
            if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                control.setValue(volume / 64f);
            }
            clip.loop(count);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }
    
}
