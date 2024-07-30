/*
 * @(#)ScreenRecorder.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.CodecChain;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.ScaleImageCodec;
import org.monte.media.avi.AVIWriter;
import org.monte.media.beans.AbstractStateModel;
import org.monte.media.color.Colors;
import org.monte.media.image.Images;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.QuickTimeWriter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static org.monte.media.av.BufferFlag.SAME_DATA;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_TWOS_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * A screen recorder written in pure Java.
 * <p>
 * Captures the screen, the mouse cursor and audio.
 * <p>
 * This class records mouse clicks occurring on other Java Windows running in
 * the same JVM. Mouse clicks occurring in other JVM's and other processes are
 * not recorded. This ability is useful for performing in-JVM recordings of an
 * application that is being tested.
 * <p>
 * This recorder uses four threads. Three capture threads for screen, mouse
 * cursor and audio, and one output thread for the movie writer.
 * <p>
 * FIXME - This class is a horrible mess.
 *
 * @author Werner Randelshofer
 */
public class ScreenRecorder extends AbstractStateModel {

    private State state = State.DONE;
    private Throwable stateMessage = null;
    
    /**
     * The file format. "AVI" or "QuickTime"
     */
    private final       Format fileFormat;
    /**
     * The input video format for cursor capture. "black" or "white".
     */
    protected           Format mouseFormat;
    /**
     * The input video format for screen capture.
     */
    private final Format    screenFormat;
    /**
     * The input and output format for audio capture.
     */
    private final Format      audioFormat;
    /**
     * The bounds of the graphics device that we capture with AWT Robot.
     */
    private final Rectangle   captureArea;
    /**
     * The writer for the movie file.
     */
    private       MovieWriter w;
    /**
     * The start time of the recording.
     */
    protected long recordingStartTime;
    /**
     * The stop time of the recording.
     */
    protected volatile long recordingStopTime;
    /**
     * The start time of the current movie file.
     */
    private long fileStartTime;
    /**
     * Holds the mouse captures made with {@code MouseInfo}.
     */
    private ArrayBlockingQueue<Buffer> mouseCaptures;
    /**
     * Timer for screen captures.
     */
    private ScheduledThreadPoolExecutor screenCaptureTimer;
    /**
     * Timer for mouse captures.
     */
    protected ScheduledThreadPoolExecutor mouseCaptureTimer;
    /**
     * Thread for audio capture.
     */
    private ScheduledThreadPoolExecutor audioCaptureTimer;
    /**
     * Thread for file writing.
     */
    private volatile Thread writerThread;
    /**
     * Mouse cursor.
     */
    private BufferedImage cursorImg;
    private BufferedImage cursorImgPressed;
    /**
     * Hot spot of the mouse cursor in cursorImg.
     */
    private Point cursorOffset;
    /**
     * Object for thread synchronization.
     */
    private final Object sync = new Object();
    private ArrayBlockingQueue<Buffer> writerQueue;
    /**
     * This codec encodes a video frame.
     */
    private Codec frameEncoder;
    /**
     * outputTime and ffrDuration are needed for conversion of the video stream
     * from variable frame rate to fixed frame rate. FIXME - Do this with a
     * CodecChain.
     */
    private Rational outputTime;
    private       Rational        ffrDuration;
    private final ArrayList<File> recordedFiles = new ArrayList<>();
    
    protected int          videoTrackId = 0;
    
    protected     int            audioTrackId = 1;
    /**
     * The device from which screen captures are generated.
     */
    private final GraphicsDevice captureDevice;
    private       AudioGrabber   audioGrabber;
    private ScreenGrabber screenGrabber;
    protected MouseGrabber mouseGrabber;
    private ScheduledFuture<?> audioFuture;
    private ScheduledFuture<?> screenFuture;
    protected ScheduledFuture<?> mouseFuture;
    /**
     * Where to store the movie.
     */
    protected File movieFolder;
    private AWTEventListener awtEventListener;
    private long maxRecordingTime = 60 * 60 * 1000;
    private long maxFileSize = Long.MAX_VALUE;
    /**
     * Audio mixer used for audio input. Set to null for default audio input.
     */
    private Mixer mixer;
    
    /**
     * Creates a screen recorder.
     *
     * @param cfg Graphics configuration of the capture screen.
     */
    public ScreenRecorder( GraphicsConfiguration cfg ) throws IOException, AWTException {
        this(cfg, null,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_QUICKTIME_ANIMATION,CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_ANIMATION, DepthKey, 24,
                        FrameRateKey, new Rational(15, 1)),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, MouseConfigs.ENCODING_BLACK_CURSOR,
                        FrameRateKey, new Rational(30, 1)),
                new Format(MediaTypeKey, MediaType.AUDIO, EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,
                        FrameRateKey, new Rational(48000, 1), SampleSizeInBitsKey, 16, ChannelsKey, 2,
                        SampleRateKey, new Rational(48000, 1), SignedKey, true, ByteOrderKey, ByteOrder.BIG_ENDIAN));
    }
    
    /**
     * Creates a screen recorder with custom formats.
     *
     * @param cfg Graphics configuration of the capture screen.
     * @param fileFormat The file format "AVI" or "QuickTime".
     * @param screenFormat The video format for screen capture.
     * @param mouseFormat The video format for mouse capture.
     * @param audioFormat The audio format for audio capture.
     */
    public ScreenRecorder(GraphicsConfiguration cfg, Format fileFormat, Format screenFormat,
                          Format mouseFormat, Format audioFormat) throws IOException, AWTException {
        this(cfg, null, fileFormat, screenFormat, mouseFormat, audioFormat);
    }
    
    /**
     * Creates a screen recorder with a defined capture area.
     *
     * @param cfg Graphics configuration of the capture screen.
     * @param captureArea Defines the area of the screen that shall be captured.
     * @param fileFormat The file format "AVI" or "QuickTime".
     * @param screenFormat The video format for screen capture.
     * @param mouseFormat The video format for mouse capture.
     * @param audioFormat The audio format for audio capture.
     */
    public ScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                          Format screenFormat, Format mouseFormat, Format audioFormat)
            throws IOException, AWTException {
        this(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, null);
    }
    
    /**
     * Creates a screen recorder with a defined capture area and movie folder.
     *
     * @param cfg Graphics configuration of the capture screen.
     * @param captureArea Defines the area of the screen that shall be captured.
     * @param fileFormat The file format "AVI" or "QuickTime".
     * @param screenFormat The video format for screen capture.
     * @param mouseFormat The video format for mouse capture.
     * @param audioFormat The audio format for audio capture.
     * @param movieFolder Where to store the movie.
     */
    public ScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                          Format screenFormat, Format mouseFormat, Format audioFormat,
                          File movieFolder) throws IOException, AWTException {
        
        this.fileFormat = fileFormat;
        this.screenFormat = screenFormat;
        this.mouseFormat = mouseFormat != null ? mouseFormat : new Format(FrameRateKey, new Rational(0, 0), EncodingKey,  MouseConfigs.ENCODING_BLACK_CURSOR);
        this.audioFormat = audioFormat;
       
        this.captureDevice = cfg.getDevice();
        this.captureArea = captureArea != null ? captureArea : cfg.getBounds();
        initializeMouseCapture(mouseFormat);
        this.movieFolder = initializeMovieFolder(movieFolder);
    }
    
    private void initializeMouseCapture(Format mouseFormat) throws IOException {
        if (mouseFormat != null && mouseFormat.get(FrameRateKey).intValue() > 0) {
            mouseCaptures = new ArrayBlockingQueue<>(mouseFormat.get(FrameRateKey).intValue() * 2);
            switch (mouseFormat.get(EncodingKey)) {
                case  MouseConfigs.ENCODING_BLACK_CURSOR:
                    cursorImg = loadCursorImage("Cursor.black.png");
                    cursorImgPressed = loadCursorImage("Cursor.black.pressed.png");
                    break;
                case  MouseConfigs.ENCODING_YELLOW_CURSOR:
                    cursorImg = loadCursorImage("Cursor.yellow.png");
                    cursorImgPressed = loadCursorImage("Cursor.yellow.pressed.png");
                    break;
                default:
                    cursorImg = loadCursorImage("Cursor.white.png");
                    cursorImgPressed = loadCursorImage("Cursor.white.pressed.png");
                    break;
            }
            cursorOffset = new Point(cursorImg.getWidth() / -2, cursorImg.getHeight() / -2);
        }
    }
    // screenFormat
    public Format getScreenFormat() {
        return screenFormat;
    }
    public Point getCursorOffset() {
        return cursorOffset;
    }
    public BufferedImage getCursorImgPressed() {
        return cursorImgPressed;
    }
    public BufferedImage getCursorImg() {
        return cursorImg;
    }
    
    public Object getSync() {
        return sync;
    }
    
    public ArrayBlockingQueue<Buffer> getMouseCaptures() {
        return mouseCaptures;
    }
    
    public GraphicsDevice getCaptureDevice() {
        return captureDevice;
    }
    
    public Rectangle getCaptureArea() {
        return captureArea;
    }
    
    
    
    private BufferedImage loadCursorImage(String imagePath) throws IOException {
        return Images.toBufferedImage(Images.createImage(ScreenRecorder.class, "images/" + imagePath));
    }
    
    private File initializeMovieFolder(File movieFolder) {
        if (movieFolder != null) {
            return movieFolder;
        }
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        String folderName = osName.startsWith("windows") ? "Videos" : "Movies";
        return new File(userHome + File.separator + folderName);
    }

    protected MovieWriter createMovieWriter() throws IOException {
        File f = createMovieFile(fileFormat);
        recordedFiles.add(f);

        MovieWriter mw = w = Registry.getInstance().getWriter(fileFormat, f);
        if (w == null) {
            throw new IOException("Error no writer found for file format: " + fileFormat + ".");
        }

        // Create the video encoder
        Rational videoRate = Rational.max(screenFormat.get(FrameRateKey), mouseFormat.get(FrameRateKey));
        ffrDuration = videoRate.inverse();
        Format videoInputFormat = screenFormat
                .prepend(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_BUFFERED_IMAGE, WidthKey, captureArea.width, HeightKey, captureArea.height,FrameRateKey, videoRate);
        Format videoOutputFormat = screenFormat
                                    .prepend( FrameRateKey, videoRate, MimeTypeKey, fileFormat.get(MimeTypeKey))
                                    .append( WidthKey, captureArea.width, HeightKey, captureArea.height);

        videoTrackId = w.addTrack(videoOutputFormat);
        if (audioFormat != null) {
            audioTrackId = w.addTrack(audioFormat);
        }

        Codec encoder = Registry.getInstance().getEncoder(w.getFormat(videoTrackId));
        if (encoder == null) {
            throw new IOException("No encoder for format " + w.getFormat(videoTrackId));
        }
        frameEncoder = encoder;
        frameEncoder.setInputFormat(videoInputFormat);
        frameEncoder.setOutputFormat(videoOutputFormat);
        if (frameEncoder.getOutputFormat() == null) {
            throw new IOException("Unable to encode video frames in this output format:\n" + videoOutputFormat);
        }

        // If the capture area does not have the same dimensions as the
        // video format, create a codec chain which scales the image before
        // performing the frame encoding.
        if (!videoInputFormat.intersectKeys(WidthKey, HeightKey).matches(videoOutputFormat.intersectKeys(WidthKey, HeightKey))) {
            ScaleImageCodec sic = new ScaleImageCodec();
            sic.setInputFormat(videoInputFormat);
            sic.setOutputFormat(videoOutputFormat.intersectKeys(WidthKey, HeightKey).append(videoInputFormat));
            frameEncoder = new CodecChain(sic, frameEncoder);
        }

        // FIXME - There should be no need for format-specific code.
        if (screenFormat.get(DepthKey) == 8) {
            if (w instanceof AVIWriter) {
                AVIWriter aviw = (AVIWriter) w;
                aviw.setPalette(videoTrackId, Colors.createMacColors());
            } else if (w instanceof QuickTimeWriter) {
                QuickTimeWriter qtw = (QuickTimeWriter) w;
                // do not set palette due to a bug
                //qtw.setVideoColorTable(videoTrack, Colors.createMacColors());
            }
        }

        fileStartTime = System.currentTimeMillis();
        return mw;
    }

    /**
     * Returns a list of all files that the screen recorder created.
     */
    public List<File> getCreatedMovieFiles() {
        return Collections.unmodifiableList(recordedFiles);
    }

    /**
     * Creates a file for recording the movie.
     * <p>
     * This implementation creates a file in the users "Video" folder on
     * Windows, or in the users "Movies" folders on Mac OS X.
     * <p>
     * You can override this method, if you would like to create a movie file at
     * a different location.
     *
     * @return the file
     */
    protected File createMovieFile(Format fileFormat) throws IOException {
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
	    
	    //
	    return new File(movieFolder,  "ScreenRecording " + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
    }

    /**
     * Returns the state of the recorder.
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the state of the recorder.
     */
    public Throwable getStateMessage() {
        return stateMessage;
    }

    /**
     * Sets the state of the recorder and fires a ChangeEvent.
     */
    private void setState(State newValue, Throwable msg) {
        state = newValue;
        stateMessage = msg;
        fireStateChanged();
    }

    public long getStartTime() {
        return recordingStartTime;
    }

    /**
     * Starts the screen recorder.
     */
    public void start() throws IOException {
        stop();
        recordedFiles.clear();
        createMovieWriter();
        try {
            recordingStartTime = System.currentTimeMillis();
            recordingStopTime = Long.MAX_VALUE;

            outputTime = new Rational(0, 0);
            startWriter();
            try {
                startScreenCapture();
            } catch (AWTException e) {
                IOException ioe = new IOException("Start screen capture failed");
                ioe.initCause(e);
                stop();
                throw ioe;
            } catch (IOException ioe) {
                stop();
                throw ioe;
            }
            if (mouseFormat != null && mouseFormat.get(FrameRateKey).intValue() > 0) {
                startMouseCapture();
            }
            if (audioFormat != null) {
                try {
                    startAudioCapture();
                } catch (LineUnavailableException e) {
                    IOException ioe = new IOException("Start audio capture failed");
                    ioe.initCause(e);
                    stop();
                    throw ioe;
                }
            }
            setState(State.RECORDING, null);
        } catch (IOException e) {
            setState(State.FAILING, e);
            stop();
            throw e;
        } catch (Throwable t) {
            setState(State.FAILING, t);
            stop();
            throw new IOException(t);
        }
    }

    /**
     * Starts screen capture.
     */
    private void startScreenCapture() throws AWTException, IOException {
        screenCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = max(1, (int) (1000 / screenFormat.get(FrameRateKey).doubleValue()));
        screenGrabber = new ScreenGrabber(this, recordingStartTime);
        screenFuture = screenCaptureTimer.scheduleAtFixedRate(screenGrabber, delay, delay, TimeUnit.MILLISECONDS);
        screenGrabber.setFuture(screenFuture);
    }

    /**
     * Stops screen capture.
     */
    private void stopScreenCapture() {
        if (screenCaptureTimer != null) {
            screenGrabber.setStopTime(recordingStopTime);
        }
    }

    /**
     * Aborts screen capture.
     */
    private void abortScreenCapture() {
        if (screenCaptureTimer != null) {
            screenGrabber.setStopTime(recordingStopTime);
            screenCaptureTimer.shutdownNow();
        }
    }

   
    /**
     * Starts mouse capture.
     */
    protected void startMouseCapture() throws IOException {
        mouseCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = max(1, (int) (1000 / mouseFormat.get(FrameRateKey).doubleValue()));
        mouseGrabber = new MouseGrabber(this, recordingStartTime, mouseCaptureTimer);
        mouseFuture = mouseCaptureTimer.scheduleAtFixedRate(mouseGrabber, delay, delay, TimeUnit.MILLISECONDS);
        final MouseGrabber mouseGrabberF = mouseGrabber;
        awtEventListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                    mouseGrabberF.setMousePressed(true);
                } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                    mouseGrabberF.setMousePressed(false);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, AWTEvent.MOUSE_EVENT_MASK);
        mouseGrabber.setFuture(mouseFuture);
    }

    /**
     * Stops mouse capturing. Use method {@link #waitUntilMouseCaptureStopped}
     * to wait until the capturing stopped.
     */
    protected void stopMouseCapture() {
        if (mouseCaptureTimer != null) {
            mouseGrabber.setStopTime(recordingStopTime);
        }
        if (awtEventListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
            awtEventListener = null;
        }
    }

    /**
     * Aborts mouse capture.
     */
    private void abortMouseCapture() {
        if (mouseCaptureTimer != null) {
            mouseGrabber.setStopTime(recordingStopTime);
            mouseCaptureTimer.shutdownNow();
        }
        if (awtEventListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
            awtEventListener = null;
        }
    }

    /**
     * Waits until mouse capturing stopped. Invoke this method only after you
     * invoked {@link #stopMouseCapture}.
     */
    protected void waitUntilMouseCaptureStopped() throws InterruptedException {
        if (mouseCaptureTimer != null) {
            try {
                mouseFuture.get();
            } catch (InterruptedException | CancellationException | ExecutionException ignored) {
            }
	        mouseCaptureTimer.shutdown();
            mouseCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
            mouseCaptureTimer = null;
            mouseGrabber.close();
            mouseGrabber = null;
        }
    }

   

    /**
     * Starts audio capture.
     */
    private void startAudioCapture() throws LineUnavailableException {
        audioCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = 500;
        audioGrabber = new AudioGrabber(mixer, audioFormat, audioTrackId, recordingStartTime, writerQueue);
        audioFuture = audioCaptureTimer.scheduleWithFixedDelay(audioGrabber, 0, 10, TimeUnit.MILLISECONDS);
        audioGrabber.setFuture(audioFuture);
    }

    /**
     * Stops audio capture.
     */
    private void stopAudioCapture() {
        if (audioCaptureTimer != null) {
            audioGrabber.setStopTime(recordingStopTime);
        }
    }

    /**
     * Aborts audio capture.
     */
    private void abortAudioCapture() {
        if (audioCaptureTimer != null) {
            audioGrabber.setStopTime(recordingStopTime);
            audioCaptureTimer.shutdownNow();
        }
    }

    /**
     * Returns the audio level of the left channel or of the mono channel.
     *
     * @return A value in the range [0.0,1.0] or AudioSystem.NOT_SPECIFIED.
     */
    public float getAudioLevelLeft() {
        AudioGrabber ag = audioGrabber;
        if (ag != null) {
            return ag.getAudioLevelLeft();
        }
        return AudioSystem.NOT_SPECIFIED;
    }

    /**
     * Returns the audio level of the right channel.
     *
     * @return A value in the range [0.0,1.0] or AudioSystem.NOT_SPECIFIED.
     */
    public float getAudioLevelRight() {
        AudioGrabber ag = audioGrabber;
        if (ag != null) {
            return ag.getAudioLevelRight();
        }
        return AudioSystem.NOT_SPECIFIED;
    }

    /**
     * Starts file writing.
     */
    private void startWriter() {
        writerQueue = new ArrayBlockingQueue<Buffer>(
                max(screenFormat.get(FrameRateKey).intValue(), mouseFormat.get(FrameRateKey).intValue()) + 1);
        writerThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (writerThread == this || !writerQueue.isEmpty()) {
                        try {
                            Buffer buf = writerQueue.take();
                            doWrite(buf);
                        } catch (InterruptedException ex) {
                            // We have been interrupted, terminate
                            break;
                        }
                    }
                } catch (Throwable e) {
                    //e.printStackTrace();
                    recordingFailed(e);
                }
            }
        };
        writerThread.start();
    }

    public void recordingFailed(final Throwable msg) {
        SwingUtilities.invokeLater(() -> {
            try {
                stop();
                setState(State.FAILED, msg);
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        });
    }

    /**
     * Stops the screen recorder.
     * <p>
     * Stopping the screen recorder may take several seconds, because audio
     * capture uses a large capture buffer. Also, the MovieWriter has to finish
     * up a movie file which may take some time depending on the amount of
     * meta-data that needs to be written.
     */
    public void stop() throws IOException {
        if (state == State.RECORDING || state == State.FAILING) {
            recordingStopTime = System.currentTimeMillis();
            stopMouseCapture();
            stopScreenCapture();
            stopAudioCapture();
            try {
                waitUntilMouseCaptureStopped();
                if (screenCaptureTimer != null) {
                    try {
                        screenFuture.get();
                    } catch (InterruptedException ex) {
                    } catch (CancellationException ex) {
                    } catch (ExecutionException ex) {
                    }
                    screenCaptureTimer.shutdown();
                    screenCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
                    screenCaptureTimer = null;
                    screenGrabber.close();
                    screenGrabber = null;
                }
                if (audioCaptureTimer != null) {
                    try {
                        audioFuture.get();
                    } catch (InterruptedException ex) {
                    } catch (CancellationException ex) {
                    } catch (ExecutionException ex) {
                    }
                    audioCaptureTimer.shutdown();
                    audioCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
                    audioCaptureTimer = null;
                    audioGrabber.close();
                    audioGrabber = null;
                }
            } catch (InterruptedException ex) {
                // nothing to do
            }
            stopWriter();
            setState(state == State.FAILING ? State.FAILED : State.DONE, getStateMessage());
        }
    }

    private void stopWriter() throws IOException {
        Thread pendingWriterThread = writerThread;
        writerThread = null;

        try {
            if (pendingWriterThread != null) {
                pendingWriterThread.interrupt();
                pendingWriterThread.join();
            }
        } catch (InterruptedException ex) {
            // nothing to do
            ex.printStackTrace();
        }
        if (w != null) {
            w.close();
            w = null;
        }
    }

    /**
     * Aborts the screen recorder.
     * <p>
     * Aborting the screen recorder may take some time, but is generally faster
     * than stopping the recorder. All recorded files are deleted.
     */
    public void abort() throws IOException {
        if (state == State.RECORDING || state == State.FAILING) {
            setState(State.FAILING, null);

            recordingStopTime = recordingStartTime;
            abortMouseCapture();
            abortScreenCapture();
            abortAudioCapture();
            stopWriter();
            setState(state == State.FAILING ? State.FAILED : State.DONE, getStateMessage());
        }
    }

    long counter = 0;

    /**
     * Writes a buffer into the movie. Since the file system may not be
     * immediately available at all times, we do this asynchronously.
     * <p>
     * The buffer is copied and passed to the writer queue, which is consumed by
     * the writer thread. See method startWriter().
     * <p>
     * AVI does not support a variable frame rate for the video track. Since we
     * can not capture frames at a fixed frame rate we have to resend the same
     * captured screen multiple times to the writer.
     * <p>
     * This method is called asynchronously from different threads.
     * <p>
     * You can override this method if you wish to process the media data.
     *
     * @param buf A buffer with un-encoded media data. If
     *            {@code buf.track==videoTrack}, then the buffer contains a
     *            {@code BufferedImage} in {@code buffer.data} and a {@code Point} in
     *            {@code buffer.header} with the recorded mouse location. The header is
     *            null if the mouse is outside the capture area, or mouse recording has not
     *            been enabled.
     * @throws IOException
     */
    protected void write(Buffer buf) throws IOException, InterruptedException {
        MovieWriter writer = this.w;
        if (writer == null) {
            return;
        }
        if (buf.track == videoTrackId) {
            if (writer.getFormat(videoTrackId).get(FixedFrameRateKey, false) == false) {
                // variable frame rate is supported => easy
                Buffer wbuf = new Buffer();
                frameEncoder.process(buf, wbuf);
                writerQueue.put(wbuf);
            } else {// variable frame rate not supported => convert to fixed frame rate

                // FIXME - Use CodecChain for this
                Rational inputTime = buf.timeStamp.add(buf.sampleDuration);
                boolean isFirst = true;
                while (outputTime.compareTo(inputTime) < 0) {
                    buf.timeStamp = outputTime;
                    buf.sampleDuration = ffrDuration;
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        buf.setFlag(SAME_DATA);
                    }
                    Buffer wbuf = new Buffer();
                    if (frameEncoder.process(buf, wbuf) != Codec.CODEC_OK) {
                        throw new IOException("Codec failed or could not process frame in a single step.");
                    }
                    writerQueue.put(wbuf);
                    outputTime = outputTime.add(ffrDuration);
                }
            }
        } else {
            Buffer wbuf = new Buffer();
            wbuf.setMetaTo(buf);
            wbuf.data = ((byte[]) buf.data).clone();
            wbuf.length = buf.length;
            wbuf.offset = buf.offset;
            writerQueue.put(wbuf);
        }
    }

    /**
     * The actual writing of the buffer happens here.
     * <p>
     * This method is called exclusively from the writer thread in
     * startWriter().
     *
     * @param buf
     * @throws IOException
     */
    private void doWrite(Buffer buf) throws IOException {
        MovieWriter mw = w;
        // Close file on a separate thread if file is full or an hour
        // has passed.
        // The if-statement must ensure that we only start a new video file
        // at a key-frame.
        // FIXME - this assumes that all audio frames are key-frames
        // FIXME - this does not guarantee that audio and video track have
        //         the same duration
        long now = System.currentTimeMillis();
        if (buf.track == videoTrackId && buf.isFlag(BufferFlag.KEYFRAME)
            && (mw.isDataLimitReached() || now - fileStartTime > maxRecordingTime)) {
            final MovieWriter closingWriter = mw;
            new Thread() {
                @Override
                public void run() {
                    try {
                        closingWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        //recordingFailed(ex);
                    }

                }
            }.start();
            mw = createMovieWriter();

        }
        //}
        mw.write(buf.track, buf);
    }

    /**
     * Maximal recording time in milliseconds. If this time is exceeded, the
     * recorder creates a new file.
     */
    public long getMaxRecordingTime() {
        return maxRecordingTime;
    }

    /**
     * Maximal recording time in milliseconds.
     */
    public void setMaxRecordingTime(long maxRecordingTime) {
        this.maxRecordingTime = maxRecordingTime;
    }

    /**
     * Maximal file size. If this size is exceeded, the recorder creates a new
     * file.
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Gets the audio mixer used for sound input. Returns null, if the default
     * mixer is used.
     */
    public Mixer getAudioMixer() {
        return mixer;
    }

    /**
     * Sets the audio mixer for sound input. Set to null for the default audio
     * mixer.
     */
    public void setAudioMixer(Mixer mixer) {
        this.mixer = mixer;
    }
}
