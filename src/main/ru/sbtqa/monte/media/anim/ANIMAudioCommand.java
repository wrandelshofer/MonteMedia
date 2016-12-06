/* @(#)ANIMAudioCommand.java
 * Copyright © 2003-2005 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.anim;

import org.monte.media.eightsvx.*;


/**
 * An ANIMAudioCommand handles an audio command that is associated to
 * a single ANIMFrame of a ANIMMovieTrack. An ANIMFrame may be associated
 * to multiple ANIMAudioCommands.
 * 
 * This version of ANIMAudioCommand is designed to handle audio commands
 * as specified by the ANIM+SLA Sound Control collection chunk (ILBM SCTL).
 * 
 * Here's the specification of the SCTL collection chunk:
 * 
 * typedef UBYTE Command; // Choice of commands
 * #define cmdPlaySound 1 // Start playing a sound
 * #define cmdStopSound 2 // Stop the sound in a given channelMask
 * #define cmdSetFreqvol 3 // Change frequency/volume for a channelMask
 *
 * typedef USHORT Flags; // Choice of flags
 * #define flagNoInterrupt 1 // Play the sound, but only if
 *                           // the channelMask isn't in use
 *
 * typedef struct {
 * Command  command;   // What to do, see above
 * UBYTE    volume;    // Volume 0..64
 * UWORD    sound,     // Sound number (one based)
 *          repeats,   // Number of times to play the sound
 *          channelMask,   // Channel(s) to use for playing (bit mask)
 *          frequency; // If non-zero, overrides the VHDR value
 * Flags    flags;     // Flags, see above
 * UBYTE    pad[4];       // For future use
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.2.1 2010-12-25 Minor fixes for J2SE 5.
 * <br>1.2 2005-09-16 Support for swapping left and right speakers added.
 * <br>1.1 2003-04-25 Revised.
 * <br>1.0 April 3, 2003 Created.
 */
public class ANIMAudioCommand {
    /** Start playing a sound. */
    public final static int COMMAND_PLAY_SOUND = 1;
    /** Stop the sound in a given channelMask. */
    public final static int COMMAND_STOP_SOUND = 2;
    /** Change frequency/volume for a channelMask. */
    public final static int COMMAND_SET_FREQVOL = 3;
    
    /** Play the sound, but only if
     * the channelMask isn't in use. */
    public final static int FLAG_NO_INTERRUPT = 1;
    /** What to do. */
    private int command;
    /** Volume 0..64 */
    private int volume;
    /** Sound number (one based). */
    private int sound;
    /** Number of times to play the sound. */
    private int repeats;
    /** Channel(s) to use for playing (bit mask).
     * The channel mask tells which channel(s) we want.
     * The code is 1=channel0 (left), 2=channel1 (right), 4=channel2 (left),
     * 8=channel3 (right). If you want more than one channel, add the codes up.
     */
    private int channelMask;
    
    private final static int CHANNEL0_MASK = 1, CHANNEL1_MASK = 2, CHANNEL2_MASK = 4,
    CHANNEL3_MASK = 8;
    private final static int CHANNEL_LEFT_MASK = CHANNEL0_MASK | CHANNEL2_MASK;
    private final static int CHANNEL_RIGHT_MASK = CHANNEL1_MASK | CHANNEL3_MASK;
    
    /** If non-zero, overrides the VHDR value. */
    private int frequency;
    /** Flags, see above. */
    private int flags;
    
    /** Channel(s) that are in use now for playing (bit mask).
     * If this mask is != zero, then this audio command is playing sound.
     */
    private int activeChannelMask;
    
    /** The prepared audio data. */
    private LoopableAudioClip audioClip;
    
    /** Creates a new instance. */
    public ANIMAudioCommand(int command, int volume, int sound, int repeats, int channelMask, int frequency, int flags) {
        this.command = command;
        this.volume = volume;
        this.sound = sound;
        this.repeats = repeats;
        this.channelMask = channelMask;
        this.frequency = frequency;
        this.flags = flags;
    }
    
    public int getChannelMask() {
        return channelMask;
    }
    public int getFrequency() {
        return frequency;
    }
    public int getSound() {
        return sound;
    }
    public int getVolume() {
        return volume;
    }
    public int getCommand() {
        return command;
    }
    
    public void prepare(ANIMMovieTrack track) {
        if (command == COMMAND_PLAY_SOUND && audioClip == null) {
            float pan;
            if ((channelMask & CHANNEL_LEFT_MASK) != 0 
            && (channelMask & CHANNEL_RIGHT_MASK) == 0) {
                pan = -1f; // left speakers only
            } else if ((channelMask & CHANNEL_RIGHT_MASK) != 0 
            && (channelMask & CHANNEL_LEFT_MASK) == 0) {
                pan = 1f; // right speakers only
            } else {
                pan = 0f; // both speakers
            }

            EightSVXAudioClip eightSVXAudioClip = (EightSVXAudioClip) track.getAudioClip(sound - 1);
            audioClip = eightSVXAudioClip.createAudioClip(
            (frequency == 0) ? eightSVXAudioClip.getSampleRate() : frequency,
            volume,
            track.isSwapSpeakers() ? -pan : pan
            );
        }
    }
    public void play(ANIMMovieTrack track) {
        prepare(track);
        if (audioClip != null) {
            if (repeats < 2) {
            audioClip.play();
            } else {
            audioClip.loop(repeats);
            }
        }
        activeChannelMask = channelMask;
    }
    
    public void stop(ANIMMovieTrack track) {
        activeChannelMask = 0;
        if (audioClip != null) {
            audioClip.stop();
        }
    }
    
    /**
     * Stops playback of this audio command on the specified channels.
     *
     */
    public void stop(ANIMMovieTrack track, int channelMask) {
        activeChannelMask &= ~channelMask;
        if (activeChannelMask == 0) {
            audioClip.stop();
        }
    }
    
    public void doCommand(ANIMMovieTrack track, ANIMAudioCommand[] runningCommands) {
    //    long start = System.currentTimeMillis();
        switch (command) {
            case COMMAND_PLAY_SOUND : {
                boolean isPlayingOnOneChannel = false;
                for (int j=0; j < 4; j++) {
                    if ((channelMask & (1 << j)) != 0) {
                        // We stop all audio commands that are playing on
                        // the channels specified by the channel mask.
                        if (runningCommands[j] != null) {
                            runningCommands[j].stop(track, 1 << j);
                        }
                        
                        if (! isPlayingOnOneChannel) {
                            // We only play an audio command on the first
                            // channel defined by the channel mask.
                            // This is hopefully suff�cient for all cases we
                            // encounter.
                            isPlayingOnOneChannel = true;
                            play(track);
                        }
                        runningCommands[j] = this;
                    }
                }
            }
            break;
            case COMMAND_STOP_SOUND : {
                for (int j=0; j < 4; j++) {
                    if ((channelMask & (1 << j)) != 0) {
                        // We stop all audio commands that are playing on
                        // the channels specified by the channel mask.
                        if (runningCommands[j] != null) {
                            runningCommands[j].stop(track, 1 << j);
                            runningCommands[j] = null;
                        }
                        
                    }
                }
            }
            break;
            case COMMAND_SET_FREQVOL :
                break;
        }
    }
    
    public void dispose() {
        if (audioClip != null) {
            audioClip = null;
        }
    }
}
