/* @(#)LoopableAudioClip.java
 * Copyright © 2003 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.eightsvx;

import java.applet.*;

/**
 * LoopableAudioClip.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0 April 25, 2003 Created.
 */
public interface LoopableAudioClip extends AudioClip {

    /**
     * Use this as a parameter for method loop(int) to specify a continuous
     * loop.
     */
    public final static int LOOP_CONTINUOUSLY = -1;

    /**
     * Starts looping playback from the current position. Playback will continue
     * to the loop's end point, then loop back to the loop start point
     * <code>count</code> times, and finally continue playback to the end of the
     * clip.
     * 
     * If the current position when this method is invoked is greater than the
     * loop end point, playback simply continues to the end of the clip without
     * looping.
     * 
     * A <code>count</code> value of 0 indicates that any current looping should
     * cease and playback should continue to the end of the clip. The behavior
     * is undefined when this method is invoked with any other value during a
     * loop operation.
     * 
     * If playback is stopped during looping, the current loop status is
     * cleared; the behavior of subsequent loop and start requests is not
     * affected by an interrupted loop operation.
     *
     * @param count the number of times playback should loop back from the
     * loop's end position to the loop's start position, or
     * <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     * continue until interrupted
     */
    public void loop(int count);

}
