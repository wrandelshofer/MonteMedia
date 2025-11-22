/*
 * @(#)ScreenSampler.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/// Samples the screen in fixed time intervals.
public class ScreenSampler implements Sampler {
    private final int track;
    private long sequenceNumber;
    /// The AWT Robot which we use for capturing the screen.
    private final Robot robot;
    private final Rectangle captureArea;
    private final Rational interval;
    private final Rational initialDelay;
    private final Format screenFormat;

    public Format getFormat() {
        return screenFormat;
    }

    @Override
    public int getTrack() {
        return track;
    }

    public ScreenSampler(Rectangle captureArea, GraphicsDevice captureDevice, Format screenFormat, int track, Rational interval, Rational initialDelay) throws IOException {
        this.captureArea = captureArea;
        this.track = track;
        this.interval = interval;
        this.initialDelay = initialDelay;
        this.screenFormat = screenFormat.prepend(VideoFormatKeys.MediaTimeScale, 600L);
        try {
            this.robot = new Robot(captureDevice);
            // create a screen capture to warm up the robot
            robot.createScreenCapture(captureArea);
        } catch (AWTException e) {
            throw new IOException(e);
        }
    }


    @Override
    public void close() {

    }

    @Override
    public Buffer sample() {
        Buffer buf = new Buffer();
        buf.timeStamp = new Rational(System.nanoTime(), 1_000_000_000);
        buf.setFlag(BufferFlag.RELATIVE_TIME);
        buf.track = track;
        buf.sequenceNumber = sequenceNumber++;
        buf.format = screenFormat;
        buf.sampleDuration = interval;

        try {
            BufferedImage screenCapture = robot.createScreenCapture(captureArea);
            buf.format = fromImage(screenCapture);
            buf.data = screenCapture;
        } catch (Throwable e) {
            buf.exception = e;
            buf.setFlag(BufferFlag.DISCARD);
        }
        return buf;
    }

    @Override
    public Rational getInterval() {
        return interval;
    }

    @Override
    public Rational getInitialDelay() {
        return initialDelay;
    }


    public static Format fromImage(BufferedImage img) {
        return new Format(
                MediaTypeKey, FormatKeys.MediaType.VIDEO,
                DataClassKey, BufferedImage.class,
                EncodingKey, VideoFormatKeys.ENCODING_BUFFERED_IMAGE,
                WidthKey, img.getWidth(),
                HeightKey, img.getHeight());
    }
}
