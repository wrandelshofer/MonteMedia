/*
 * @(#)ScreenGrabber.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.color.Colors;
import org.monte.media.math.Rational;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/**
 * ScreenGrabber is responsible for capturing screen updates.
 * It runs periodically and enqueues these changes for processing by the {@link ScreenRecorder}.
 */
class ScreenGrabber implements Runnable, AutoCloseable {

    private final Point prevDrawnMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private volatile long stopTime = Long.MAX_VALUE;
    private boolean prevMousePressed;
    /**
     * Holds the screen capture made with AWT Robot.
     */
    private BufferedImage screenCapture;
    private final ScreenRecorder recorder;
    /**
     * The AWT Robot which we use for capturing the screen.
     */
    private final Robot robot;
    private final Rectangle captureArea;
    /**
     * Holds the composed image (screen capture and super-imposed mouse
     * cursor). This is the image that is written into the video track of
     * the file.
     */
    private final BufferedImage videoImg;
    /**
     * Graphics object for drawing into {@code videoImg}.
     */
    private final Graphics2D videoGraphics;
    private final Format mouseFormat;
    /**
     * Holds the mouse captures made with {@code MouseInfo}.
     */
    private final ArrayBlockingQueue<Buffer> mouseCaptures;
    /**
     * The time the previous screen frame was captured.
     */
    private Rational prevScreenCaptureTime;
    private final BufferedImage cursorImg;
    private final BufferedImage cursorImgPressed;
    /**
     * Previously draw mouse location. This is used to have the last mouse
     * location at hand, when a new screen capture has been created, but the
     * mouse has not been moved.
     */
    private final Point cursorOffset;
    private final int videoTrack;
    private final long startTime;
    private ScheduledFuture<?> future;
    private long sequenceNumber;

    public ScreenGrabber(ScreenRecorder recorder, long startTime) throws AWTException, IOException {
        this.recorder = recorder;
        this.captureArea = recorder.getCaptureArea();
        this.robot = new Robot(recorder.getCaptureDevice());
        this.mouseFormat = recorder.mouseFormat;
        this.mouseCaptures = recorder.getMouseCaptures();
        this.cursorImg = recorder.getCursorImg();
        this.cursorImgPressed = recorder.getCursorImgPressed();
        this.cursorOffset = recorder.getCursorOffset();
        this.videoTrack = recorder.videoTrackId;
        this.prevScreenCaptureTime = new Rational(startTime, 1000);
        this.startTime = startTime;

        Format screenFormat = recorder.getScreenFormat();
        this.videoImg = createVideoImage(screenFormat);
        this.videoGraphics = initializeVideoGraphics();
    }

    private BufferedImage createVideoImage(Format screenFormat) throws IOException {
        int depth = screenFormat.get(DepthKey, 24);
        return switch (depth) {
            case 24 -> new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_INT_RGB);
            case 16 -> new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_USHORT_555_RGB);
            case 8 ->
                    new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
            default -> throw new IOException("Unsupported color depth: " + depth);
        };
    }

    private Graphics2D initializeVideoGraphics() {
        Graphics2D graphics = videoImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        return graphics;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public void setStopTime(long newValue) {
        stopTime = newValue;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    @Override
    public void run() {
        try {
            grabScreen();
        } catch (Throwable ex) {
            recorder.recordingFailed(ex);
        }
    }

    /**
     * Grabs a screen, generates video images with pending mouse captures
     * and writes them into the movie file.
     */
    private void grabScreen() throws IOException, InterruptedException {
        BufferedImage previousScreenCapture = screenCapture;
        long timeBeforeCapture = (System.nanoTime() / 1_000_000);

        try {
            screenCapture = robot.createScreenCapture(captureArea);
        } catch (IllegalMonitorStateException e) {
            // Log the error and return instead of throwing an exception
            System.err.println("Screen capture failed: " + e.getMessage());
            return;
        }

        long timeAfterCapture = (System.nanoTime() / 1_000_000);
        previousScreenCapture = (previousScreenCapture == null) ? screenCapture : previousScreenCapture;
        videoGraphics.drawImage(previousScreenCapture, 0, 0, null);

        processMouseCaptures(timeBeforeCapture, timeAfterCapture, previousScreenCapture);

        if (timeBeforeCapture > getStopTime()) {
            future.cancel(false);
        }
    }

    private void processMouseCaptures(long timeBeforeCapture, long timeAfterCapture, BufferedImage previousScreenCapture) throws IOException, InterruptedException {
        Buffer buf = new Buffer();
        buf.format = new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, ENCODING_BUFFERED_IMAGE);

        boolean hasMouseCapture = processMouseCapturesWithCursor(timeBeforeCapture, timeAfterCapture, previousScreenCapture, buf);

        if (!hasMouseCapture && shouldCaptureFrame(timeAfterCapture)) {
            captureFrameWithoutMouseMovement(timeAfterCapture, buf);
        }
    }

    private boolean processMouseCapturesWithCursor(long timeBeforeCapture, long timeAfterCapture, BufferedImage previousScreenCapture, Buffer buf) throws IOException, InterruptedException {
        boolean hasMouseCapture = false;
        if (mouseFormat != null && mouseFormat.get(FrameRateKey).intValue() > 0) {
            while (!mouseCaptures.isEmpty() && shouldProcessMouseCapture(timeAfterCapture)) {
                Buffer mouseCapture = mouseCaptures.poll();
                if (isValidMouseCapture(mouseCapture, timeBeforeCapture)) {
                    hasMouseCapture = true;
                    processMouseCapture(Objects.requireNonNull(mouseCapture), previousScreenCapture, buf);
                }
            }
        }
        return hasMouseCapture;
    }

    private boolean shouldProcessMouseCapture(long timeAfterCapture) {
        return Objects.requireNonNull(mouseCaptures.peek()).timeStamp.compareTo(new Rational(timeAfterCapture, 1000)) < 0;
    }

    private boolean isValidMouseCapture(Buffer mouseCapture, long timeBeforeCapture) {
        return Objects.requireNonNull(mouseCapture).timeStamp.compareTo(prevScreenCaptureTime) > 0
                && mouseCapture.timeStamp.compareTo(new Rational(timeBeforeCapture, 1000)) < 0
                && mouseCapture.timeStamp.compareTo(new Rational(getStopTime(), 1000)) <= 0;
    }

    private void processMouseCapture(Buffer mouseCapture, BufferedImage previousScreenCapture, Buffer buf) throws IOException, InterruptedException {
        Point mcp = (Point) mouseCapture.data;
        prevMousePressed = (Boolean) mouseCapture.header;
        prevDrawnMouseLocation.setLocation(mcp.x - captureArea.x, mcp.y - captureArea.y);
        Point p = prevDrawnMouseLocation;

        drawCursor(p);
        writeBuffer(buf, mouseCapture.timeStamp, p);
        eraseCursor(p, previousScreenCapture);
    }

    private void drawCursor(Point p) {
        BufferedImage cursorToDraw = prevMousePressed ? cursorImgPressed : cursorImg;
        videoGraphics.drawImage(cursorToDraw, p.x + cursorOffset.x, p.y + cursorOffset.y, null);
    }

    private void writeBuffer(Buffer buf, Rational timestamp, Point p) throws IOException, InterruptedException {
        buf.clearFlags();
        buf.data = videoImg;
        buf.sampleDuration = timestamp.subtract(prevScreenCaptureTime);
        buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
        buf.track = videoTrack;
        buf.sequenceNumber = sequenceNumber++;
        buf.header = p.x == Integer.MAX_VALUE ? null : p;
        recorder.write(buf);
        prevScreenCaptureTime = timestamp;
    }

    private void eraseCursor(Point p, BufferedImage previousScreenCapture) {
        videoGraphics.drawImage(previousScreenCapture,
                p.x + cursorOffset.x, p.y + cursorOffset.y,
                p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,
                p.x + cursorOffset.x, p.y + cursorOffset.y,
                p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,
                null);
    }

    private boolean shouldCaptureFrame(long timeAfterCapture) {
        return prevScreenCaptureTime.compareTo(new Rational(getStopTime(), 1000)) < 0;
    }

    private void captureFrameWithoutMouseMovement(long timeAfterCapture, Buffer buf) throws IOException, InterruptedException {
        Point p = prevDrawnMouseLocation;
        drawCursor(p);

        buf.data = videoImg;
        buf.sampleDuration = new Rational(timeAfterCapture, 1000).subtract(prevScreenCaptureTime);
        buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
        buf.track = videoTrack;
        buf.sequenceNumber = sequenceNumber++;
        buf.header = p.x == Integer.MAX_VALUE ? null : p;
        recorder.write(buf);
        prevScreenCaptureTime = new Rational(timeAfterCapture, 1000);

        eraseCursor(p, screenCapture);
    }

    @Override
    public void close() {
        videoGraphics.dispose();
        videoImg.flush();
    }
}