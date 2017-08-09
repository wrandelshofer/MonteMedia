/* @(#)SEQPlayer.java
 * Copyright © 2010-2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.seq;

import org.monte.media.player.ColorCyclePlayer;
import org.monte.media.player.AbstractPlayer;
import org.monte.media.swing.player.JPlayerControlAqua;
import org.monte.media.swing.ImagePanel;
import org.monte.media.bitmap.BitmapImage;
import org.monte.media.io.BoundedRangeInputStream;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import static java.lang.Math.*;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.*;
import org.monte.media.player.PlayerControl;


/**
 * Player for Cyberpaint Sequence animations (*.SEQ).
 * <p>
 * Reference:<br>
 * <a href="http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats"
 * >http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats</a>
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 3.0.2 2011-08-23 Frame duration was too long by one jiffie.
 * <br>1.0 2010-12-25 Created.
 */
public class SEQPlayer
        extends AbstractPlayer
        implements ColorCyclePlayer {

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
     * The movie track built from the movie data.
     */
    private SEQMovieTrack track;
    /** Two bitmaps are needed for double buffering. */
    private BitmapImage bitmapEven, bitmapOdd;
    /**
     * Index of the frame, that has been prepared
     * in its even or odd bitmap buffer for display.
     */
    private int preparedEven, preparedOdd;
    /**
     * Index of the frame which has been delta
     * decoded in its even or odd bitmap buffer.
     */
    private int fetchedEven, fetchedOdd;
    /**
     * Index of the frame currently being displayed.
     */
    private int displayFrame = -1;
    /** Indicates wether frames may be skipped or not. */
    private boolean isPlayEveryFrame = false;
    /** Indicates wether playback shall loop or not. */
    private volatile boolean isLoop = true;
    /** Indicates wether the player is in pause mode. */
    //private volatile boolean isPaused = true;
    /**
     * Jiffies are used be IFF ANIM's for timing.
     * Jiffies is the number of frames or fields per second.
     * The variable jiffieMillis is a conversion of Jiffies into milliseconds.
     */
    private float jiffieMillis = 1000f / 60f;
    /**
     * Setting the global frame duration overrides all
     * frame duration settings in the frames of the the movie track.
     *
     * Frame Duration in Jiffies. Set this to
     * -1 if you do not want to override the frame durations in
     * the frames of the movie track.
     */
    private int globalFrameDuration = -1;
    /**
     * The visual component contains the display area
     * for movie images.
     */
    private ImagePanel /*ImagePanelAWT*/ visualComponent;
    /**
     * The visual component contains control elements
     * for starting and stopping the movie.
     */
    private PlayerControl controlComponent;
    /**
     * This lock is being used to coordinate the
     * decoder with the player.
     */
    private Object decoderLock = new Object();
    /**
     * The preferred color model for this player.
     */
    private ColorModel preferredColorModel = null;
    /**
     * Indicates wether all data has been cached.
     * Acts like a latch: Once set to true never changes
     * its value anymore.
     */
    private volatile boolean isCached = false;
    /**
     * The amiga has four audio channels.
     * There can be only four active audio commands at all times.
     */
    private SEQAudioCommand[] audioChannels = new SEQAudioCommand[4];
    /**
     * Turns audio on or off.
     */
    private boolean isAudioEnabled = true;
    /**
     * Determines whether audio is being loaded or not.
     */
    private boolean isLoadAudio;
    /** */
    private boolean debug = false;
    /** */
    private Hashtable<String,Object> properties;
    /**
     * This variable is set to true when during decoding of the
     * input stream at least one audio clip is detected.
     */
    private boolean isAudioAvailable;
    /**
     * This variable is set to true when during decoding of the
     * input stream at least one color cycle is detected.
     */
    private boolean isColorCyclingAvailable;
    /** Whether color cycling is started. */
    private boolean isColorCyclingStarted;
    /**
     * Set this to true, if the delta frames of the animation can be decoded
     * relative to the previous frame and relative to the subsequent frame.
     */
    private boolean isPingPong = true;
    /**
     * Direction of the play head: +1 for forward playing, -1 for backward playing.
     */
    private int playDirection = 1;

    private class Handler implements MouseListener, PropertyChangeListener, ChangeListener {

        // ------------------------
        // MouseListener
        // ------------------------
        @Override
        public void mouseClicked(MouseEvent event) {
            if (getState() != CLOSED && event.getModifiers() == InputEvent.BUTTON1_MASK) {
                if (getState() == STARTED && getTargetState() == STARTED && event.getClickCount() == 1) {
                    stop();
                } else if (getState() != STARTED && getTargetState() != STARTED && event.getClickCount() == 2) {
                    start();
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }

        @Override
        public void mousePressed(MouseEvent event) {
        }

        @Override
        public void mouseReleased(MouseEvent event) {
        }

        // ------------------------
        // PropertyChangeListener
        // ------------------------
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (timeModel != null) {
                int count = track.getFrameCount();
                timeModel.setMaximum(count > 0 ? count - 1 : 0);
                synchronized (decoderLock) {
                    decoderLock.notifyAll();
                }
            }
            if (event.getPropertyName().equals("audioClipCount")) {
                setAudioAvailable(track.getAudioClipCount() > 0);
            } else if (event.getPropertyName().equals("colorCyclesCount")) {
                setColorCyclingAvailable(track.getColorCyclesCount() > 0);
            }
        }

        // ------------------------
        // ChangeListener
        // ------------------------
        @Override
        public void stateChanged(ChangeEvent evt) {
            // Time model changed and player in prefetched state?
            if (evt.getSource() == timeModel) {
                if (getState() == STARTED) {
                    // Wake the worker thread up.
                    synchronized (this) {
                        notifyAll();
                    }
                } else {
                    // Render the video on the worker thread.
                    dispatcher.dispatch(
                            new Runnable() {

                                @Override
                                public void run() {
                                    renderVideo(getTimeModel().getValue());
                                }
                            });
                }
            }
        }
    }
    private Handler handler = new Handler();

    public SEQPlayer(InputStream in) {
        this(in, -1, true);
    }

    /**
     * Creates a new instance.
     * @param in InputStream containing an IFF ANIM file.
     * @param inputFileSize The size of the input file. Provide the value -1
     * if this is not known.
     * @param loadAudio Provide value false if this player should not load audio
     * data.
     */
    public SEQPlayer(InputStream in, int inputFileSize, boolean loadAudio) {
        this.in = in;
        this.inputFileSize = inputFileSize;
        this.isLoadAudio = loadAudio;
    }

    /**
     * Sets the preferred color model.
     * If this color model is the same as the one used by the
     * screen device showing the animation, then this may considerably
     * improve the performance of the player.
     * Setting this to null will let the player choose a color model
     * that best suits the media being played.
     * Calling this method has no effect, if the player is already realized.
     */
    public void setPreferredColorModel(ColorModel cm) {
        if (bitmapEven == null) {
            preferredColorModel = cm;
        }
    }

    /**
     * Returns the bounded range model that represents
     * the time line of the player.
     */
    @Override
    public BoundedRangeModel getTimeModel() {
        return timeModel;
    }

    /**
     * Enables or disables audio playback.
     */
    @Override
    public void setAudioEnabled(boolean newValue) {
        boolean oldValue = isAudioEnabled;
        isAudioEnabled = newValue;
        propertyChangeSupport.firePropertyChange("audioEnabled",
                (oldValue) ? Boolean.TRUE : Boolean.FALSE,
                (newValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Returns true if audio playback is enabled.
     */
    @Override
    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    /**
     * Swaps left and right speakers if set to true.
     */
    public void setSwapSpeakers(boolean newValue) {
        boolean oldValue = track.isSwapSpeakers();
        track.setSwapSpeakers(newValue);
        propertyChangeSupport.firePropertyChange("swapSpeakers",
                (oldValue) ? Boolean.TRUE : Boolean.FALSE,
                (newValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Returns true if left and right speakers are swapped.
     */
    public boolean isSwapSpeakers() {
        return track.isSwapSpeakers();
    }

    /**
     * Returns the bounded range model that represents
     * the amount of data being fetched from the file
     * the movie is stored in.
     */
    @Override
    public BoundedRangeModel getCachingModel() {
        return cachingControlModel;
    }

    /**
     * Returns the image producer that produces
     * the animation frames.
     */
    protected ImageProducer getImageProducer() {
        return memoryImage;
    }

    /**
     * Returns the movie track.
     */
    public SEQMovieTrack getMovieTrack() {
        return track;
    }

    /**
     * Obtain the display Component for this Player.
     * The display Component is where visual media is rendered.
     * If this Player has no visual component, getVisualComponent
     * returns null. For example, getVisualComponent might return
     * null if the Player only plays audio.
     */
    @Override
    public synchronized Component getVisualComponent() {
        if (visualComponent == null) {
            visualComponent = /*new ImagePanelAWT()*/ new ImagePanel();
            if (getImageProducer() != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }
            visualComponent.addMouseListener(handler);
        }
        return visualComponent;
    }

    /**
     * Obtain the Component that provides the default user
     * interface for controlling this Player. If this Player
     * has no default control panel, getControlPanelComponent
     * returns null.
     */
    @Override
    public synchronized Component getControlPanelComponent() {
        if (controlComponent == null) {
            controlComponent = new JPlayerControlAqua();
            controlComponent.setPlayer(this);
        }
        return controlComponent.getComponent();
    }

    /**
     * Does the unrealized state.
     */
    @Override
    protected void doUnrealized() {
    }

    /**
     * Does the realizing state.
     */
    @Override
    protected void doRealizing() {
        timeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
        timeModel.addChangeListener(handler);
        cachingControlModel = new BoundedRangeInputStream(in);
        /*if (inputFileSize != -1) {
            cachingControlModel.setMaximum(inputFileSize);
        }*/

        track = new SEQMovieTrack();
        track.addPropertyChangeListener(handler);

        // If the components of the player have been created before
        // we arrived here, then they may not have been initialized properly.
        // So we reinitialize them here.
        synchronized (this) {
            if (controlComponent != null) {
                controlComponent.setPlayer(this);
            }
        }

        // Decode the file asynchronously. So the player
        // can play files while they are being decoded.
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    SEQDecoder decoder = new SEQDecoder(cachingControlModel);
                    decoder.produce(track, isLoadAudio);
                    isCached = true;
                    cachingControlModel.setValue(cachingControlModel.getMaximum());
                    propertyChangeSupport.firePropertyChange("cached", Boolean.FALSE, Boolean.TRUE);
                    //setPaused(false);

                    // No frames in track? Close player.
                    if (track.getFrameCount() == 0) {
                        synchronized (decoderLock) {
                            setTargetState(CLOSED);
                            decoderLock.notifyAll();
                        }
                    }
                } catch (Throwable e) {
                    synchronized (decoderLock) {
                        if (visualComponent != null) {
                            visualComponent.setMessage(e.toString());
                        }
                        setTargetState(CLOSED);
                        decoderLock.notifyAll();
                        e.printStackTrace();
                    }
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        };
        t.start();

        // Wait until enough information has been decoded.
        // (The player is not realized until at least the
        // header data of the movie has been decoded.)
        synchronized (decoderLock) {
            while (track.getFrameCount() < 1 && getTargetState() != CLOSED) {
                try {
                    decoderLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // Initialize the player. Needs header information
        // from the movie track to do this.
        ColorModel cm;
        int width = track.getWidth();
        int height = track.getHeight();
        int nbPlanes = track.getNbPlanes();
        int masking = track.getMasking();

        if (track.getFrameCount() > 0) {
            SEQFrame frame = track.getFrame(0);
            cm = frame.getColorModel();
        } else {
            cm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        }
        bitmapEven = new BitmapImage(
                width,
                height,
                nbPlanes + (masking == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                cm);
        bitmapOdd = new BitmapImage(
                width,
                height,
                nbPlanes + (masking == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                cm);
//bitmapOdd=bitmapEven;
        jiffieMillis = 1000f / (float) track.getJiffies();

        if (track.getColorCycles().isEmpty()) {
            bitmapEven.setPreferredChunkyColorModel(preferredColorModel);
            bitmapOdd.setPreferredChunkyColorModel(preferredColorModel);
        }

        /*Hashtable*/ properties = new Hashtable<String,Object>();
        properties.put(
                "aspect",
                new Double((double) track.getXAspect() / (double) track.getYAspect()));
        Object comment = track.getProperty("comment");
        if (comment != null) {
            properties.put("comment", comment);
        }
        String s;
        switch (track.getScreenMode()) {
            case SEQMovieTrack.MODE_INDEXED_COLORS:
                s = "Indexed Colors";
                break;
            case SEQMovieTrack.MODE_DIRECT_COLORS:
                s = "Direct Colors";
                break;
            case SEQMovieTrack.MODE_EHB:
                s = "EHB";
                break;
            case SEQMovieTrack.MODE_HAM6:
                s = "HAM 6";
                break;
            case SEQMovieTrack.MODE_HAM8:
                s = "HAM 8";
                break;
            default:
                s = "unknown";
                break;
        }
        properties.put("screenMode", s);
        properties.put("nbPlanes", "" + track.getNbPlanes());
        properties.put("jiffies", "" + track.getJiffies());
        properties.put("colorCycling", "" + track.getColorCycles().size());

        if (bitmapEven.isEnforceDirectColors()) {
            cm = (preferredColorModel instanceof DirectColorModel) ? preferredColorModel : new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new int[width * height], 0, width, properties);
        } else if (cm instanceof DirectColorModel) {
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new int[width * height], 0, width, properties);
        } else {
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new byte[width * height], 0, width, properties);
            if (track.getColorCycles().size() > 0) {
                for (ColorCycle cc : track.getColorCycles()) {
                    memoryImage.addColorCycle(cc);
                }
                if (isColorCyclingStarted()) {
                    memoryImage.start();
                }
            }

        }
        memoryImage.setAnimated(true);
        preparedEven = preparedOdd = Integer.MAX_VALUE;
        fetchedEven = fetchedOdd = Integer.MAX_VALUE;
        if (track.getFrameCount() > 0) {
            renderVideo(0);
            properties.put("renderMode", bitmapEven.getChunkyColorModel());
        }

        // If the components of the player have been created before
        // we arrived here, then they may not have been initialized properly.
        // So we reinitialize them here.
        synchronized (this) {
            if (visualComponent != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }
        }
    }

    /**
     * Does the realized state.
     */
    @Override
    protected void doRealized() {
        // Free resources being achieved during prefetch.
    }

    /**
     * Does the prefetching state.
     */
    @Override
    protected void doPrefetching() {
        renderVideo(timeModel.getValue());
    }

    /**
     * Does the prefetched state.
     */
    @Override
    protected void doPrefetched() {
    }

    public void setPlayEveryFrame(boolean newValue) {
        isPlayEveryFrame = newValue;
    }

    /**
     * Set this to true to treat the two wrapup frames at the end of the
     * animation like regular frames.
     * /
    public void setPlayWrapupFrames(boolean newValue) {
        track.setPlayWrapupFrames(newValue);

        int count = track.getFrameCount();
        System.out.println("SEQPLayer.setPlayWrapupFrames count="+count);
        timeModel.setMaximum(count > 0 ? count - 1 : 0);
    }*/

    /**
     * Set this to true to treat the two wrapup frames at the end of the
     * animation like regular frames.
     */
    public void setDebug(boolean newValue) {
        this.debug = newValue;
        if (newValue == false && visualComponent != null) {
            visualComponent.setMessage(null);
        }
    }

    /**
     * Always returns true.
     */
    public boolean isPlayWrapupFrames() {
        return true;
        //return track.isPlayWrapupFrames();
    }

    /**
     * Setting frames per second overrides all
     * frame duration settings in the frames of the the movie track.
     *
     * @param framesPerSecond Frames per section. Set this to
     * 0f if you do not want to override the frame durations in
     * the frames of the movie track.
     */
    public void setFramesPerSecond(float framesPerSecond) {
        if (framesPerSecond <= 0f) {
            setGlobalFrameDuration(-1);
        } else {
            setGlobalFrameDuration((int) (1000f / framesPerSecond));
        }
    }

    /**
     * Setting the global frame duration overrides all
     * frame duration settings in the frames of the the movie track.
     *
     * @param frameDuration Frame Duration in milliseconds. Set this to
     * -1 if you do not want to override the frame durations in
     * the frames of the movie track.
     */
    public void setGlobalFrameDuration(int frameDuration) {
        this.globalFrameDuration = frameDuration;
    }

    public boolean isPlayEveryFrame() {
        return isPlayEveryFrame;
    }

    public void setLoop(boolean newValue) {
        boolean oldValue = isLoop;
        isLoop = newValue;
        propertyChangeSupport.firePropertyChange("isLoop", oldValue, newValue);
    }

    public boolean isLoop() {
        return isLoop;
    }

    public String getDeltaOperationDescription() {
        String s;
        int op = track.getDeltaOperation();
        switch (op) {
            case SEQDeltaFrame.OP_Copy:
                s = "OP Direct";
                break;
            case SEQDeltaFrame.OP_XOR:
                s = "XOR";
                break;
            default:
                s = "unknown";
                break;
        }
        return s + " OP(" + op + ")";
    }

    /**
     * Does the started state.
     * Is called by run().
     * Does not change the value of targetState but may
     * change state in case of an error.
     */
    @Override
    protected void doStarted() {
        long mediaTime = System.currentTimeMillis() + (long) jiffieMillis;
        int index;
        long sleepTime;

        // Start from beginning when playhead is at end of timeline
        if (timeModel.getValue() == timeModel.getMaximum()) {
            timeModel.setValue(timeModel.getMinimum());
        }

        while (getTargetState() == STARTED) {
            index = timeModel.getValue();
            if (isPlayEveryFrame) {
                if (isAudioEnabled) {
                    prepareAudio(index);
                }
                prepareVideo(index);
                if (mediaTime > System.currentTimeMillis()) {
                    sleepTime = mediaTime - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (globalFrameDuration == -1) {
                    mediaTime = System.currentTimeMillis() + (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                } else {
                    mediaTime = System.currentTimeMillis() + globalFrameDuration;
                }
                if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                    renderAudio(index);
                } else {
                    muteAudio();
                }
                renderVideo(index);
            } else {
                if (mediaTime > System.currentTimeMillis()) {
                    if (isAudioEnabled) {
                        prepareAudio(index);
                    }
                    prepareVideo(index);
                    sleepTime = mediaTime - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (globalFrameDuration == -1) {
                        mediaTime += (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                    } else {
                        mediaTime += (long) globalFrameDuration;
                    }
                    if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                        renderAudio(index);
                    } else {
                        muteAudio();
                    }
                    renderVideo(index);
                } else {
                    if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                        renderAudio(index);
                    } else {
                        muteAudio();
                    }
                    if (globalFrameDuration == -1) {
                        mediaTime += (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                    } else {
                        mediaTime += (long) globalFrameDuration;
                    }
                }
            }

            if (!timeModel.getValueIsAdjusting()) {
                if (timeModel.getValue() == timeModel.getMaximum()) {
                    if (isCached && isLoop && !isPingPong) {
                        timeModel.setValue(timeModel.getMinimum());
                    } else if (isCached && isPingPong && playDirection == 1) {
                        playDirection = -1;
                        timeModel.setValue(timeModel.getValue() + playDirection);
                    } else {
                        break;
                    }
                } else if (timeModel.getValue() == timeModel.getMinimum() && isPingPong && playDirection == -1) {
                    playDirection = 1;
                    timeModel.setValue(timeModel.getValue() + playDirection);
                } else {
                    timeModel.setValue(timeModel.getValue() + playDirection);
                }
            }
        }
        /*}*/

        renderVideo(timeModel.getValue());
        muteAudio();
    }

    private void muteAudio() {
        for (int i = 0; i < audioChannels.length; i++) {
            if (audioChannels[i] != null) {
                audioChannels[i].stop(track);
                audioChannels[i] = null;
            }
        }
    }

    /**
     * Closes the player.
     */
    @Override
    protected void doClosed() {
        try {
            in.close();
        } catch (IOException e) {
        }
    }

    private void fetchFrame(int index) {
        SEQFrame frame = null;
        int fetched;
        int interleave = track.getInterleave();
        BitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {
            // even?
            if (fetchedEven == index) {
                return;
            }
            fetched = fetchedEven;
            bitmap = bitmapEven;
            fetchedEven = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = track.getFrame(fetched);
                frame.decode(bitmap, track);
                return;
            } else {
                if (fetched > index) {
                    frame = track.getFrame(0);
                    frame.decode(bitmap, track);
                    fetched = 0;
                }
            }
        } else {
            // odd?
            if (fetchedOdd == index) {
                return;
            }
            fetched = fetchedOdd;
            bitmap = bitmapOdd;
            fetchedOdd = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = track.getFrame(fetched);
                frame.decode(bitmap, track);
                return;
            } else {
                if (fetched > index) {
                    frame = track.getFrame(0);
                    frame.decode(bitmap, track);
                    frame = track.getFrame(1);
                    frame.decode(bitmap, track);
                    fetched = 1;
                }
            }
        }
        for (int i = fetched + interleave; i <= index; i += interleave) {
            frame = track.getFrame(i);
            frame.decode(bitmap, track);
        }
    }

    /**
     * Prepare video data for the specified frame index.
     */
    private void prepareVideo(int index) {
        BitmapImage bitmap;
        int prepared;
        int interleave = track.getInterleave();

        if (interleave == 1 || (index & 1) == 0) {
            // even?
            if (preparedEven == index) {
                return;
            }
            prepared = preparedEven;
            preparedEven = index;
            bitmap = bitmapEven;
        } else {
            // odd?
            if (preparedOdd == index) {
                return;
            }
            prepared = preparedOdd;
            preparedOdd = index;
            bitmap = bitmapOdd;
        }

        // Fetch the frame from the underlying storage system
        // and decode delta information.
        fetchFrame(index);

        // Convert planar to chunky.
        SEQFrame frame = track.getFrame(index);
        ColorModel cm = frame.getColorModel();
        bitmap.setPlanarColorModel(cm);
        if (prepared == index - interleave && //
                (bitmap.getPixelType() == BitmapImage.BYTE_PIXEL || //
                cm == track.getFrame(prepared).getColorModel())) {
            bitmap.convertToChunky(
                    frame.getTopBound(track),
                    frame.getLeftBound(track),
                    frame.getBottomBound(track),
                    frame.getRightBound(track));

        } else if (isPingPong && prepared == index + interleave &&//
                (bitmap.getPixelType() == BitmapImage.BYTE_PIXEL || //
                cm == track.getFrame(prepared).getColorModel())) {
            frame = track.getFrame(index + interleave);
            bitmap.convertToChunky(
                    frame.getTopBound(track),
                    frame.getLeftBound(track),
                    frame.getBottomBound(track),
                    frame.getRightBound(track));
        } else {
            bitmap.convertToChunky();
        }
    }

    /**
     * Prepare audio data for the specified frame index.
     */
    private void prepareAudio(int index) {
        SEQFrame frame = track.getFrame(index);
        SEQAudioCommand[] audioCommands = frame.getAudioCommands();
        if (audioCommands != null) {
            for (int i = 0; i < audioCommands.length; i++) {
                audioCommands[i].prepare(track);
            }
        }
    }

    /**
     * Show the video data for the specified frame index.
     */
    private void renderVideo(int index) {
        if (displayFrame == index) {
            return;
        }
        int interleave = track.getInterleave();

        BitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {
            // even?
            bitmap = bitmapEven;
        } else {
            // odd?
            bitmap = bitmapOdd;
        }

        prepareVideo(index);
        ColorModel cm = bitmap.getChunkyColorModel();
        if (bitmap.getPixelType() == BitmapImage.INT_PIXEL) {
            memoryImage.newPixels(bitmap.getIntPixels(), cm, 0, track.getWidth());
        } else {
            memoryImage.newPixels(bitmap.getBytePixels(), cm, 0, track.getWidth());
        }
        displayFrame = index;

        if (debug && visualComponent != null) {
            SEQFrame frame = track.getFrame(index);
            StringBuilder buf = new StringBuilder();
            buf.append("frame:");
            buf.append(index);
            buf.append(" duration:");
            buf.append(frame.getRelTime());
            buf.append(", seq op:");
            buf.append(frame.getOperation());

            SEQAudioCommand[] audioCommands = frame.getAudioCommands();
            if (audioCommands != null) {
                for (int i = 0; i < audioCommands.length; i++) {
                    switch (audioCommands[i].getCommand()) {
                        case SEQAudioCommand.COMMAND_PLAY_SOUND:
                            buf.append("\nplay");
                            break;
                        case SEQAudioCommand.COMMAND_STOP_SOUND:
                            buf.append("\nstop");
                            break;
                        case SEQAudioCommand.COMMAND_SET_FREQVOL:
                            buf.append("\nfreqvol");
                            break;
                        default:
                            buf.append("ILLEGAL COMMAND:");
                            buf.append(audioCommands[i].getCommand());
                            break;
                    }
                    buf.append(" sound:");
                    buf.append(audioCommands[i].getSound());
                    buf.append(" freq:");
                    buf.append(audioCommands[i].getFrequency());
                    buf.append(" vol:");
                    buf.append(audioCommands[i].getVolume());
                    buf.append(" channels:");
                    int channelMask = audioCommands[i].getChannelMask();
                    boolean first = true;
                    for (int j = 0; j < 4; j++) {
                        if (((1 << j) & channelMask) != 0) {
                            if (!first) {
                                buf.append(", ");
                            }
                            buf.append(j);
                            buf.append((j % 2 == 0) ? "(l)" : "(r)");
                            first = false;
                        }
                    }
                }
            }

            visualComponent.setMessage(buf.toString());
        }
    }

    /**
     * Show the audio data for the specified frame index.
     */
    private synchronized void renderAudio(int index) {
        prepareAudio(index);

        // Play audio data
        if (isActive()) {

            SEQFrame frame = track.getFrame(index);
            SEQAudioCommand[] audioCommands = frame.getAudioCommands();
            if (audioCommands != null) {
                for (int i = 0; i < audioCommands.length; i++) {
                    audioCommands[i].doCommand(track, audioChannels);
                }
            }
        }
    }

    /**
     * Returns the total duration in milliseconds.
     */
    @Override
    public long getTotalDuration() {
        if (globalFrameDuration == -1) {
            return (long) (track.getTotalDuration() * jiffieMillis);
        } else {
            return track.getFrameCount() * globalFrameDuration;
        }
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

    /** Returns true if audio is available.
     *
     */
    @Override
    public boolean isAudioAvailable() {
        return isAudioAvailable;
    }

    private void setAudioAvailable(boolean newValue) {
        boolean oldValue = isAudioAvailable;
        isAudioAvailable = newValue;
        propertyChangeSupport.firePropertyChange("audioAvailable", oldValue, newValue);
    }

    public void setPingPong(boolean newValue) {
        boolean oldValue = isPingPong;
        isPingPong = newValue;
        if (!newValue) {
            playDirection = 1;
        }
        propertyChangeSupport.firePropertyChange("pingPong", oldValue, newValue);
    }

    public boolean isPingPong() {
        return isPingPong;
    }

    private void setColorCyclingAvailable(boolean newValue) {
        boolean oldValue = isColorCyclingAvailable;
        isColorCyclingAvailable = newValue;
        propertyChangeSupport.firePropertyChange("colorCyclingAvailable", oldValue, newValue);
    }

    /** Returns true if color cycling is available in the movie track. */
    @Override
    public boolean isColorCyclingStarted() {
        return isColorCyclingStarted;
    }

    /** Starts or stops color cycling. */
    @Override
    public void setColorCyclingStarted(boolean newValue) {
        boolean oldValue = isColorCyclingStarted;
        isColorCyclingStarted = newValue;
        if (memoryImage != null) {
            memoryImage.setColorCyclingStarted(newValue);
            propertyChangeSupport.firePropertyChange("colorCyclingStarted", oldValue, newValue);
        }
    }

    /** Starts or stops color cycling. */
    @Override
    public boolean isColorCyclingAvailable() {
        return isColorCyclingAvailable;
    }

    /** Sets whether colors are blended during color cycling. */
    @Override
    public void setBlendedColorCycling(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isBlendedColorCycling();
            memoryImage.setBlendedColorCycling(newValue);
            propertyChangeSupport.firePropertyChange("blendedColorCycling", oldValue, newValue);
        }
    }

    /** Returns true if colors are blended during color cycling. */
    @Override
    public boolean isBlendedColorCycling() {
        return memoryImage == null ? false : memoryImage.isBlendedColorCycling();
    }
}