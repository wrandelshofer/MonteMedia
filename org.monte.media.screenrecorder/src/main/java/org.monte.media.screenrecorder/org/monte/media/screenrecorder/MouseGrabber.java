/*
 * @(#)MouseGrabber.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * MouseGrabber is responsible for capturing mouse movements and clicks
 * during screen recording. It runs periodically to check for mouse state
 * changes and enqueues these changes for processing by the {@link ScreenRecorder}.
 */
class MouseGrabber implements Runnable, AutoCloseable {

    /**
     * Previously captured mouse location. This is used to coalesce mouse
     * captures if the mouse has not been moved.
     */
    private final Point prevCapturedMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final ScheduledThreadPoolExecutor timer;
    private final ScreenRecorder recorder;
    private final GraphicsDevice captureDevice;
    private final Rectangle captureArea;
    private final BlockingQueue<Buffer> mouseCaptures;
    private volatile long stopTime = Long.MAX_VALUE;
    private final long startTime;
    private final Format format;
    private ScheduledFuture<?> future;
    private volatile boolean mousePressed;
    private volatile boolean mouseWasPressed;
    private volatile boolean mousePressedRecorded;
    private final Rectangle cursorImageArea;
    private final Point cursorOffset;

    /**
     * Constructs a new MouseGrabber.
     *
     * @param recorder  The ScreenRecorder instance
     * @param startTime The start time of the recording
     * @param timer     The ScheduledThreadPoolExecutor for scheduling tasks
     */
    public MouseGrabber(ScreenRecorder recorder, long startTime, ScheduledThreadPoolExecutor timer) {
        this.timer = timer;
        this.recorder = recorder;
        this.format = recorder.mouseFormat;
        this.startTime = startTime;
        this.captureDevice = recorder.getCaptureDevice();
        this.captureArea = recorder.getCaptureArea();
        this.mouseCaptures = recorder.getMouseCaptures();
        this.cursorImageArea = new Rectangle(0, 0, recorder.getCursorImg().getWidth(), recorder.getCursorImg().getHeight());
        this.cursorOffset = recorder.getCursorOffset();
    }

    /**
     * Sets the future for this MouseGrabber task.
     *
     * @param future The ScheduledFuture for this task
     */
    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    /**
     * Sets the mouse pressed state.
     *
     * @param newValue The new mouse pressed state
     */
    public void setMousePressed(boolean newValue) {
        if (newValue) {
            mouseWasPressed = true;
        }
        mousePressed = newValue;
    }

    @Override
    public void run() {
        try {
            grabMouse();
        } catch (Throwable ex) {
            timer.shutdown();
            recorder.recordingFailed(ex);
        }
    }

    /**
     * Sets the stop time for the mouse grabber.
     *
     * @param newValue The new stop time
     */
    public void setStopTime(long newValue) {
        this.stopTime = newValue;
    }

    /**
     * Gets the current stop time.
     *
     * @return The current stop time
     */
    public long getStopTime() {
        return this.stopTime;
    }

    /**
     * Captures the mouse cursor position and state.
     */
    private void grabMouse() throws InterruptedException {
        long now = (System.nanoTime() / 1_000_000);
        if (now > getStopTime()) {
            future.cancel(false);
            return;
        }

        PointerInfo info = MouseInfo.getPointerInfo();
        Point currentMouseLocation = getCursorPosition(info);

        if (hasMouseStateChanged(currentMouseLocation)) {
            enqueueMouse(currentMouseLocation, now);
            updatePreviousState(currentMouseLocation);
        }

        mouseWasPressed = mousePressed;
    }

    private Point getCursorPosition(PointerInfo info) {
        Point p = info.getLocation();
        cursorImageArea.setLocation(p.x + cursorOffset.x, p.y + cursorOffset.y);

        if (isCursorOutsideCaptureArea(info)) {
            // If the cursor is outside the capture region, we
            // assign Integer.MAX_VALUE to its location.
            // This ensures that all mouse movements outside of the
            // capture region get coalesced.
            p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        return p;
    }

    private boolean isCursorOutsideCaptureArea(PointerInfo info) {
        return !info.getDevice().equals(captureDevice)
                || !captureArea.intersects(cursorImageArea);
    }

    private boolean hasMouseStateChanged(Point currentMouseLocation) {
        return !currentMouseLocation.equals(prevCapturedMouseLocation)
                || mouseWasPressed != mousePressedRecorded;
    }

    private void enqueueMouse(Point currentMouseLocation, long timestamp) throws InterruptedException {
        Buffer buf = new Buffer();
        buf.format = format;
        buf.timeStamp = new Rational(timestamp, 1000);
        buf.data = currentMouseLocation;
        buf.header = mouseWasPressed;
        mouseCaptures.put(buf);
    }

    private void updatePreviousState(Point currentMouseLocation) {
        prevCapturedMouseLocation.setLocation(currentMouseLocation);
        mousePressedRecorded = mouseWasPressed;
    }

    @Override
    public void close() {
        //! Implement any necessary cleanup here
    }
}