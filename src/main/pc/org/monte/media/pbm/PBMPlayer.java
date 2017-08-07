/* @(#)PBMPlayer.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.pbm;

import org.monte.media.gui.ImagePanel;
import org.monte.media.io.BoundedRangeInputStream;
import org.monte.media.player.AbstractPlayer;
import org.monte.media.gui.JMovieControlAqua;
import org.monte.media.movie.MovieControl;
import org.monte.media.player.ColorCyclePlayer;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import java.awt.Component;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

/**
 * A Player for PBM images with color cycling.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-08-04 Created.
 */
public class PBMPlayer extends AbstractPlayer implements ColorCyclePlayer {

    /**
     * The memory image source handles the image
     * producer/consumer protocol.
     */
    private ColorCyclingMemoryImageSource memoryImage;
    /**
     * Bounded range indicates the number of frames and the
     * index of the current frame.
     */
    private BoundedRangeModel timeModel;
    /**
     * Bounded range indicates the amount of data being
     * fetched from the data source.
     */
    private BoundedRangeInputStream cachingControlModel;
    /**
     * The Input stream containing the movie data.
     */
    private InputStream in;
    /**
     * The size of the input file. If the size is not known then
     * this attribute is set to -1.
     */
    private int inputFileSize = -1;
    /**
     * The visual component contains the display area
     * for movie images.
     */
    private ImagePanel visualComponent;
    /**
     * The visual component contains control elements
     * for starting and stopping the movie.
     */
    private MovieControl controlComponent;
    /**
     * Indicates wether all data has been cached.
     * Acts like a latch: Once set to true never changes
     * its value anymore.
     */
    private volatile boolean isCached = false;

    public PBMPlayer(InputStream in) {
        this(in, -1);
    }

    /**
     * Creates a new instance.
     * @param in InputStream containing an IFF ANIM file.
     * @param inputFileSize The size of the input file. Provide the value -1
     * if this is not known.
     */
    public PBMPlayer(InputStream in, int inputFileSize) {
        this.in = in;
        this.inputFileSize = inputFileSize;
        timeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
    }

    @Override
    protected void doClosed() {
    }

    @Override
    protected void doUnrealized() {
    }

    @Override
    protected void doRealizing() {
        cachingControlModel = new BoundedRangeInputStream(in);
        if (inputFileSize != -1) {
            cachingControlModel.setMaximum(inputFileSize);
        }

        try {
            PBMDecoder decoder = new PBMDecoder(cachingControlModel);
            ArrayList<ColorCyclingMemoryImageSource> track = decoder.produce();
            isCached = true;
            cachingControlModel.setValue(cachingControlModel.getMaximum());
            propertyChangeSupport.firePropertyChange("cached", Boolean.FALSE, Boolean.TRUE);

            // No frames in track? Close player.
            if (track.size() == 0) {
                setTargetState(CLOSED);
            } else {
                memoryImage = track.get(0);
                memoryImage.setAnimated(true);
                if (memoryImage.isColorCyclingAvailable()) {
                    propertyChangeSupport.firePropertyChange("colorCyclingAvailable", false, true);
                }
            }
            timeModel.setRangeProperties(0, 1, 0, 1, false);


        } catch (Throwable e) {
            if (visualComponent != null) {
                visualComponent.setMessage(e.toString());
            }
            setTargetState(CLOSED);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void doRealized() {
        getVisualComponent();
        if (getImageProducer() != null) {
            visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
        }
    }

    @Override
    protected void doPrefetching() {
        //
    }

    @Override
    protected void doPrefetched() {
        //
    }

    @Override
    protected void doStarted() {
        //
    }

    @Override
    public void setAudioEnabled(boolean b) {
        // 
    }

    @Override
    public boolean isAudioEnabled() {
        return false;
    }

    @Override
    public boolean isAudioAvailable() {
        return false;
    }

    @Override
    public BoundedRangeModel getTimeModel() {
        return timeModel;
    }

    @Override
    public BoundedRangeModel getCachingModel() {
        return cachingControlModel;
    }

    @Override
    public synchronized Component getVisualComponent() {
        if (visualComponent == null) {
            visualComponent = new ImagePanel();
            if (getImageProducer() != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }
            //visualComponent.addMouseListener(handler);
        }
        return visualComponent;
    }

    @Override
    public Component getControlPanelComponent() {
        if (controlComponent == null) {
            controlComponent = new JMovieControlAqua();
            controlComponent.setPlayer(this);
        }
        return controlComponent.getComponent();
    }

    @Override
    public long getTotalDuration() {
        return 0;
    }

    /**
     * Returns the image producer that produces
     * the animation frames.
     */
    protected ImageProducer getImageProducer() {
        return memoryImage;
    }

    @Override
    public void setColorCyclingStarted(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isColorCyclingStarted();
            memoryImage.setColorCyclingStarted(newValue);
            propertyChangeSupport.firePropertyChange("colorCyclingStarted", oldValue, newValue);
        }
    }

    @Override
    public boolean isColorCyclingAvailable() {
        return memoryImage == null ? false : memoryImage.isColorCyclingAvailable();
    }

    @Override
    public boolean isColorCyclingStarted() {
        return (memoryImage == null) ? false : memoryImage.isColorCyclingStarted();
    }

    /**
     * Returns true when the player has completely cached all movie data.
     * This player informs all property change listeners, when the value of this
     * property changes. The name of the property is 'cached'.
     */
    @Override
    public boolean isCached() {
        return isCached;
    }

    /** Sets whether colors are blended during color cycling. */
    public void setBlendedColorCycling(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isBlendedColorCycling();
            memoryImage.setBlendedColorCycling(newValue);
            propertyChangeSupport.firePropertyChange("blendedColorCycling", oldValue, newValue);
        }
    }

    /** Returns true if colors are blended during color cycling. */
    public boolean isBlendedColorCycling() {
        return memoryImage == null ? false : memoryImage.isBlendedColorCycling();
    }
}
