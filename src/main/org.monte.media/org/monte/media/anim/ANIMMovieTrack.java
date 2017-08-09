/* @(#)ANIMMovieTrack.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.anim;

import org.monte.media.anim.ANIMAudioCommand;
import org.monte.media.ilbm.ColorCycle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.applet.AudioClip;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.*;
import org.monte.media.anim.ANIMFrame;

/**
 * A movie track gives access to the static resources of
 * a movie (image and audio data, global informations).
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id: ANIMMovieTrack.java 364 2016-11-09 19:54:25Z werner $
 */
public class ANIMMovieTrack {

    /** Raster width and heigth in pixels. */
    private int width, height;
    /** Pixel position for this image. */
    private int xPosition, yPosition;
    /** Number of source bitplanes. */
    private int nbPlanes;
    /** Number of palettes (cmap chunks).
     * The value -1 is used to invalidate this variable.
     */
    private int nbPalettes = -1;
    /** Masking. */
    private int masking;
    public final static int MSK_NONE = 0,
            MSK_HAS_MASK = 1,
            MSK_HAS_TRANSPARENT_COLOR = 2,
            MSK_LASSO = 3;
    /** Index of transparent color. */
    private int transparentColor;
    /** Pixel aspect ratio. */
    private int xAspect, yAspect;
    /** Page size in pixels. */
    private int pageWidth, pageHeight;
    /** Screenmode. */
    private int screenMode;
    /**
     * Jiffies is the number of frames or fields per second.
     */
    private int jiffies;
    /**
     * Set this to true to treat the two wrapup frames at the end of the
     * animation like regular frames.
     */
    private boolean isPlayWrapupFrames = false;
    /** Indicates wether left and right speakers are swapped. */
    private boolean isSwapSpeakers = false;
    /** Screenmodes. */
    public final static int MODE_INDEXED_COLORS = 0,
            MODE_DIRECT_COLORS = 1,
            MODE_EHB = 2,
            MODE_HAM6 = 3,
            MODE_HAM8 = 4;
    /**
     * Compression method of key frame.
     * XXX Should not be stored here since it is
     *     only needed during the decoding of the
     *     ANIM file.
     */
    private int compression;
    public final static int CMP_NONE = 0,
            CMP_BYTE_RUN_1 = 1,
            CMP_VERTICAL = 2;
    /**
     * Total playback time of the movie in Jiffies (1/60 second).
     */
    private long totalDuration;
    /** User defined properties. */
    private HashMap<String,Object> properties = new HashMap<String,Object>();
    /** Anim frames. */
    private ArrayList<ANIMFrame> frames = new ArrayList<ANIMFrame>();
    /** Sound clips. */
    private ArrayList<AudioClip> audioClips = new ArrayList<AudioClip>();
    /** Property Change support. */
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    private ArrayList<ColorCycle> colorCycles = new ArrayList<ColorCycle>();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    /**
     * Sets a property.
     *
     * @param  name    The name of the property.
     * @param  newValue  The value of the property.
     */
    public void setProperty(String name, Object newValue) {
        Object oldValue = properties.get(name);
        if (newValue == null) {
            properties.remove(name);
        } else {
            properties.put(name, newValue);
        }
        listeners.firePropertyChange(name, oldValue, newValue);
    }

    /**
     * Set this to true to treat the two wrapup frames at the end of the
     * movie track like regular frames.
     */
    public void setPlayWrapupFrames(boolean b) {
        isPlayWrapupFrames = b;

        // Invalidate number of palettes
        nbPalettes = -1;
        totalDuration=-1;
    }

    /**
     * Returns true, if the two wrapup frames at the end of the movie track
     * are treated like regular frames.
     */
    public boolean isPlayWrapupFrames() {
        return isPlayWrapupFrames;
    }

    /** Returns the interleave of frames in this movie track.
     * This is 2 for double buffered animations, and 1 one for animations without
     * double buffering.
     */
    public int getInterleave() {
        return frames.size() > 0 && frames.get(frames.size()-1).getInterleave() == 1 ? 1 : 2;
    }


    /**
     * Swaps left and right speakers if set to true.
     */
    public void setSwapSpeakers(boolean newValue) {
        boolean oldValue = isSwapSpeakers;
        isSwapSpeakers = newValue;

        if (newValue != oldValue) {
            for (ANIMFrame f : frames) {
                ANIMAudioCommand[] aac = f.getAudioCommands();
                if (aac != null) {
                    for (int j = 0; j < aac.length; j++) {
                        aac[j].dispose();
                    }
                }
            }
        }
        /*
        propertyChangeSupport.firePropertyChange("swapSpeakers",
        (oldValue) ? Boolean.TRUE : Boolean.FALSE,
        (newValue) ? Boolean.TRUE : Boolean.FALSE
        );*/
    }

    /**
     * Returns true if left and right speakers are swapped.
     */
    public boolean isSwapSpeakers() {
        return isSwapSpeakers;
    }

    /**
     * Gets a property.
     *
     * @param  name    The name of the property.
     * @return  The value of the property or null if the property
     *      is not defined.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    private void firePropertyChange(String name, int oldValue, int newValue) {
        listeners.firePropertyChange(name, new Integer(oldValue), new Integer(newValue));
    }

    public void setJiffies(int newValue) {
        int oldValue = jiffies;
        jiffies = newValue;
        firePropertyChange("jiffies", oldValue, newValue);
    }

    public void setCompression(int newValue) {
        int oldValue = compression;
        compression = newValue;
        firePropertyChange("compression", oldValue, newValue);
    }

    public void setWidth(int newValue) {
        int oldValue = width;
        width = newValue;
        firePropertyChange("width", oldValue, newValue);
    }

    public void setHeight(int newValue) {
        int oldValue = height;
        height = newValue;
        firePropertyChange("height", oldValue, newValue);
    }

    public void setXPosition(int newValue) {
        int oldValue = xPosition;
        xPosition = newValue;
        firePropertyChange("xPosition", oldValue, newValue);
    }

    public void setYPosition(int newValue) {
        int oldValue = yPosition;
        yPosition = newValue;
        firePropertyChange("yPosition", oldValue, newValue);
    }

    public void setNbPlanes(int newValue) {
        int oldValue = nbPlanes;
        nbPlanes = newValue;
        firePropertyChange("nbPlanes", oldValue, newValue);
    }

    public void setMasking(int newValue) {
        int oldValue = masking;
        masking = newValue;
        firePropertyChange("masking", oldValue, newValue);
    }

    public void setTransparentColor(int newValue) {
        int oldValue = transparentColor;
        transparentColor = newValue;
        firePropertyChange("transparentColor", oldValue, newValue);
    }

    public void setXAspect(int newValue) {
        int oldValue = xAspect;
        xAspect = newValue;
        firePropertyChange("xAspect", oldValue, newValue);
    }

    public void setYAspect(int newValue) {
        int oldValue = yAspect;
        yAspect = newValue;
        firePropertyChange("yAspect", oldValue, newValue);
    }

    public void setPageWidth(int newValue) {
        int oldValue = pageWidth;
        pageWidth = newValue;
        firePropertyChange("pageWidth", oldValue, newValue);
    }

    public void setPageHeight(int newValue) {
        int oldValue = pageHeight;
        pageHeight = newValue;
        firePropertyChange("pageHeight", oldValue, newValue);
    }

    public void setScreenMode(int newValue) {
        int oldValue = screenMode;
        screenMode = newValue;
        firePropertyChange("screenMode", oldValue, newValue);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public int getNbPlanes() {
        return nbPlanes;
    }

    public int getNbPalettes() {
        if (nbPalettes == -1) {
            int n = getFrameCount();
            if (n > 0) {
                ColorModel cm = getFrame(0).getColorModel();
                if (cm instanceof IndexColorModel) {
                    IndexColorModel prev = (IndexColorModel) cm;
                    int size = prev.getMapSize();
                    byte[] pr = new byte[size];
                    byte[] pg = new byte[size];
                    byte[] pb = new byte[size];
                    byte[] cr = new byte[size];
                    byte[] cg = new byte[size];
                    byte[] cb = new byte[size];
                    prev.getReds(pr);
                    prev.getGreens(pg);
                    prev.getBlues(pb);
                    nbPalettes = 1;
                    for (int i = 1; i < n; i++) {
                        IndexColorModel cur = (IndexColorModel) getFrame(i).getColorModel();
                        if (cur != prev) {
                            cur.getReds(cr);
                            cur.getGreens(cg);
                            cur.getBlues(cb);
                            if (!Arrays.equals(cr, pr) || !Arrays.equals(cg, pg) || !Arrays.equals(cb, pb)) {
                                nbPalettes++;
                                prev = cur;
                                System.arraycopy(cr, 0, pr, 0, cr.length);
                                System.arraycopy(cg, 0, pg, 0, cg.length);
                                System.arraycopy(cb, 0, pb, 0, cb.length);
                            }
                        }
                    }
                } else {
                    nbPalettes = 1;
                    ColorModel prev = cm;
                    for (int i = 1; i < n; i++) {
                        ColorModel cur = getFrame(i).getColorModel();
                        if (cur != prev) {
                            nbPalettes++;
                            prev = cur;
                        }
                    }
                    //nbPalettes_ = n;
                }
            }
        }
        return nbPalettes;
    }

    public int getMasking() {
        return masking;
    }

    public int getTransparentColor() {
        return transparentColor;
    }

    public int getXAspect() {
        return xAspect;
    }

    public int getYAspect() {
        return yAspect;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getScreenMode() {
        return screenMode;
    }

    public int getJiffies() {
        return jiffies;
    }

    public int getCompression() {
        return compression;
    }

    /**
     * The return value of this method is only reliable when all frames of
     * the movie have been loaded.
     */
    public int getDeltaOperation() {
        int lastFrame = frames.size() - 1;
        if (lastFrame < 0) {
            return -1; // no frames available
        }
        return frames.get(lastFrame).getOperation();
    }

    ;

    public void addFrame(ANIMFrame frame) {
        int oldValue;
        synchronized (this) {
            oldValue = frames.size();
            frames.add(frame);

            // invalidate total duration
            totalDuration = -1;
        }
        firePropertyChange("frameCount", oldValue, oldValue + 1);
    }

    public void addAudioClip(AudioClip clip) {
        int oldValue = audioClips.size();
        audioClips.add(clip);
        firePropertyChange("audioClipCount", oldValue, oldValue + 1);
    }

    public int getAudioClipCount() {
        return audioClips.size();
    }

    public AudioClip getAudioClip(int index) {
        return audioClips.get(index);
    }

    public int getFrameCount() {
        int size = frames.size();
        if (isPlayWrapupFrames) {
            return size;
        } else {
            int interleave = getInterleave();
            return (size > 1+interleave) ? size - interleave : size;
        }
    }

    public ANIMFrame getFrame(int index) {
        return frames.get(index);
    }

    /**
     * Timing for frame relative to previous frame.
     */
    public long getFrameDuration(int index) {
        return frames.get(index).getRelTime();
    }

    /**
     * Total playback time of the movie in Jiffies (1/60 second).
     */
    public synchronized long getTotalDuration() {
        if (totalDuration == -1) {
            totalDuration = 0;
            for (int i = getFrameCount() - 1; i >= 0; i--) {
                totalDuration += Math.max(getFrameDuration(i),1);
            }
        }
        return totalDuration;
    }

    public void addColorCycle(ColorCycle cc) {
        int oldCount = colorCycles.size();
        colorCycles.add(cc);
        firePropertyChange("colorCyclesCount", oldCount, colorCycles.size());
    }
    public List<ColorCycle> getColorCycles() {
        return colorCycles;
    }
    public int getColorCyclesCount() {
        return colorCycles.size();
    }
}
