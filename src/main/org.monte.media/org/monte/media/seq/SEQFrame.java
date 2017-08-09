/* @(#)SEQFrame.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.seq;

import java.awt.image.ColorModel;
import java.util.*;
import org.monte.media.amigabitmap.AmigaBitmapImage;

/**
 * Represents a frame in a movie track.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0 2010-12-25 Created.
 */
public abstract class SEQFrame {
    protected ColorModel colorModel;
    protected byte[] data;
    private int operation;
    private int storageMethod;
    private long reltime;
    private int interleave;
    private int bits;
    protected final static int
    // common BITs
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
    private SEQAudioCommand[] audioCommands;
    
    /**
     * Adds an audio command to this anim frame.
     */
    public void addAudioCommand(SEQAudioCommand command) {
        if (audioCommands == null) {
            audioCommands = new SEQAudioCommand[1];
        } else {
            SEQAudioCommand[] old = audioCommands;
            audioCommands = new SEQAudioCommand[old.length + 1];
            System.arraycopy(old, 0, audioCommands, 0, old.length);
        }
        audioCommands[audioCommands.length - 1] = command;
    }
    
    /**
     * Returns audio commands associated with this frame.
     * Returns null if there are no audio commands available for this frame.
     */
    public SEQAudioCommand[] getAudioCommands() {
        return audioCommands;
    }
    
    /**
     * Removes duplicate audio commands.
     */
    public void cleanUpAudioCommands() {
        if (audioCommands != null && audioCommands.length > 1) {
        int i, j;
        ArrayList<SEQAudioCommand> v = new ArrayList<SEQAudioCommand>();
        v.add(audioCommands[0]);
        for (i=1; i < audioCommands.length; i++) {
            for (j=0; j < i; j++) {
                if ((audioCommands[j].getChannelMask() & audioCommands[i].getChannelMask()) != 0) {
                    break;
                }
            }
            if (j == i) v.add(audioCommands[i]);
            //else System.out.println("AudioCommand eliminiert "+audioCommands[i].getSound());
        }
        audioCommands = v.toArray(new SEQAudioCommand[v.size()]);
        }
        
    }
    
    
    public void setColorModel(ColorModel cm) { colorModel = cm; }
    public ColorModel getColorModel() { return colorModel; }
    public void setData(byte[] data) { this.data = data; }
    
    public void setOperation(int operation) { this.operation = operation; }
    public void setStorageMethod(int storageMethod) { this.storageMethod = storageMethod; }
    public void setWidth(int w) { /*w_ = w;*/ }
    public void setHeight(int h) { /*h_ = h;*/ }
    public void setX(int x) { /*x_ = x;*/ }
    public void setY(int y) { /*y_ = y;*/ }
    public void setAbsTime(long abstime) { /*abstime_ = abstime;*/ }
    public void setRelTime(long reltime) { this.reltime = reltime; }
    public void setInterleave(int interleave) { this.interleave = interleave; }
    public void setBits(int bits) { this.bits = bits; }
    
    public int getOperation() { return this.operation; }
    public int getStorageMethod() { return this.storageMethod; }
    public int getBits() { return this.bits; }
    //  public int getWidth() { /*return w_;*/ }
    //  public int getHeight() { /*return h_;*/ }
    public long getRelTime() { return this.reltime; }
    public int getInterleave() { return this.interleave; }
    
    public abstract void decode(AmigaBitmapImage bitmap, SEQMovieTrack track);
    
    public int getTopBound(SEQMovieTrack track) { return 0; }
    public int getBottomBound(SEQMovieTrack track) { return track.getHeight()-1; }
    public int getLeftBound(SEQMovieTrack track) { return 0; }
    public int getRightBound(SEQMovieTrack track) { return track.getWidth()-1; }
    
    /** Returns true if the frame can be decoded over both the previous frame
     * or the subsequent frame. Bidirectional frames can be used efficiently
     * for forward and backward playing a movie.
     * <p>
     * All key frames are bidirectional. Delta frames which use an XOR OP-mode
     * are bidirectional as well.
     */
    public boolean isBidirectional() {return true;}
}
