/* @(#)ANIMFrame.java
 * Copyright © 1999-2013 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.anim;

import java.awt.image.ColorModel;
import java.util.*;
import ru.sbtqa.monte.media.image.BitmapImage;

/**
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id: ANIMFrame.java 364 2016-11-09 19:54:25Z werner $
 */
public abstract class ANIMFrame {

    protected ColorModel colorModel;
    protected byte[] data;
    private int operation;
    private int mask;
    // Currently unused
    //  private int w_;
    //  private int h_;
    //  private int y_;
    //  private int x_;
    //  private long abstime_;
    private long reltime;
    private int interleave;
    private int bits;
    protected final static int // common BITs
          BadBitsOP_GeneralDelta = 0xffc0,
          BIT_LongData = 1,
          BIT_XOR = 2,
          BIT_OneInfoListForAllPlanes = 4,
          BIT_RLC = 8,
          BIT_Vertical = 16,
          BIT_LongInfoOffsets = 32,
          // BITs for Vertical Delta 5 Kompression
          BadBitsOP_ByteVertical = 0xfff7;

    /**
     * Holds an array of audio commands associated with this ANM frame.
     */
    private ANIMAudioCommand[] audioCommands;

    /**
     * Adds an audio command to this anim frame.
     *
     * @param command TODO
     */
    public void addAudioCommand(ANIMAudioCommand command) {
        if (audioCommands == null) {
            audioCommands = new ANIMAudioCommand[1];
        } else {
            ANIMAudioCommand[] old = audioCommands;
            audioCommands = new ANIMAudioCommand[old.length + 1];
            System.arraycopy(old, 0, audioCommands, 0, old.length);
        }
        audioCommands[audioCommands.length - 1] = command;
    }

    /**
     * Returns audio commands associated with this frame. Returns null if there
     * are no audio commands available for this frame.
     *
     * @return TODO
     */
    public ANIMAudioCommand[] getAudioCommands() {
        return audioCommands;
    }

    /**
     * Removes duplicate audio commands.
     */
    public void cleanUpAudioCommands() {
        if (audioCommands != null && audioCommands.length > 1) {
            int i, j;
            ArrayList<ANIMAudioCommand> v = new ArrayList<ANIMAudioCommand>();
            v.add(audioCommands[0]);
            for (i = 1; i < audioCommands.length; i++) {
                for (j = 0; j < i; j++) {
                    if ((audioCommands[j].getChannelMask() & audioCommands[i].getChannelMask()) != 0) {
                        break;
                    }
                }
                if (j == i) {
                    v.add(audioCommands[i]);
                }
                //else System.out.println("AudioCommand eliminiert "+audioCommands[i].getSound());
            }
            audioCommands = new ANIMAudioCommand[v.size()];
            audioCommands = v.toArray(audioCommands);
        }

    }

    public void setColorModel(ColorModel cm) {
        colorModel = cm;
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public void setWidth(int w) {
        /*w_ = w;*/ }

    public void setHeight(int h) {
        /*h_ = h;*/ }

    public void setX(int x) {
        /*x_ = x;*/ }

    public void setY(int y) {
        /*y_ = y;*/ }

    public void setAbsTime(long abstime) {
        /*abstime_ = abstime;*/ }

    public void setRelTime(long reltime) {
        this.reltime = reltime;
    }

    public void setInterleave(int interleave) {
        this.interleave = interleave;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getOperation() {
        return this.operation;
    }

    public int getBits() {
        return this.bits;
    }

    //  public int getWidth() { /*return w_;*/ }
    //  public int getHeight() { /*return h_;*/ }
    public long getRelTime() {
        return this.reltime;
    }

    public int getInterleave() {
        return this.interleave;
    }

    public abstract void decode(BitmapImage bitmap, ANIMMovieTrack track);

    public int getTopBound(ANIMMovieTrack track) {
        return 0;
    }

    public int getBottomBound(ANIMMovieTrack track) {
        return track.getHeight() - 1;
    }

    public int getLeftBound(ANIMMovieTrack track) {
        return 0;
    }

    public int getRightBound(ANIMMovieTrack track) {
        return track.getWidth() - 1;
    }

    /**
     * Returns true if the frame can be decoded over both the previous frame or
     * the subsequent frame. Bidirectional frames can be used efficiently for
     * forward and backward playing a movie.
     * 
     * All key frames are bidirectional. Delta frames which use an XOR OP-mode
     * are bidirectional as well.
     *
     * @return TODO
     */
    public boolean isBidirectional() {
        return true;
    }
}
