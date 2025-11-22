/*
 * @(#)MouseSampler.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.codec.video.AffineTransform;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.codec.sprite.Sprite;
import org.monte.media.quicktime.codec.sprite.SpriteFormatKeys;
import org.monte.media.quicktime.codec.sprite.SpriteSample;

import java.awt.AWTEvent;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

/// Samples the mouse.
public class MouseSampler implements Sampler {
    private final Rational interval;
    private final Rational initialDelay;
    private final int track;
    private final Rectangle captureArea;
    private long sequenceNumber;
    private volatile boolean mousePressed;
    private final Format screenFormat;
    private volatile boolean mouseWasPressed;
    private final BufferedImage cursorImage;
    private final BufferedImage cursorPressedImage;
    private final Point cursorOffset;
    private final GraphicsDevice captureDevice;

    private final AWTEventListener awtEventListener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                mousePressed = true;
                mouseWasPressed = true;
            } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                mousePressed = false;
            }
        }
    };

    @Override
    public int getTrack() {
        return track;
    }


    public Format getFormat() {
        return screenFormat;
    }

    public MouseSampler(Rectangle captureArea, GraphicsDevice captureDevice, Format screenFormat, int track, Rational interval,
                        BufferedImage cursorImage, BufferedImage cursorPressedImage, Point cursorOffset, Rational initialDelay) throws IOException {
        this.interval = interval;
        this.captureArea = captureArea;
        this.screenFormat = screenFormat.prepend(VideoFormatKeys.MediaTimeScale, 600L);
        this.track = track;
        this.cursorImage = cursorImage;
        this.cursorPressedImage = cursorPressedImage;
        this.captureDevice = captureDevice;
        this.cursorOffset = cursorOffset;
        this.initialDelay = initialDelay;
        Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void close() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
    }

    @Override
    public Buffer sample() {
        Buffer buf = new Buffer();
        buf.timeStamp = new Rational(System.nanoTime(), 1_000_000_000);
        buf.setFlag(BufferFlag.RELATIVE_TIME);
        buf.track = track;
        buf.sampleCount = 1;
        buf.sampleDuration = interval;
        buf.sequenceNumber = sequenceNumber++;

        PointerInfo info = MouseInfo.getPointerInfo();
        buf.format = screenFormat.prepend(VideoFormatKeys.EncodingKey, SpriteFormatKeys.ENCODING_JAVA_SPRITE);

        /*
        BufferedImage img = new BufferedImage(captureArea.width, captureArea.height, cursorImage.getType());
        Graphics g = img.getGraphics();
        g.drawImage(mouseWasPressed ? cursorPressedImage : cursorImage, info.getLocation().x + cursorOffset.x, info.getLocation().y + cursorOffset.y, null);
        g.dispose();

        buf.data = img;
        */
        var ss = new SpriteSample();
        ss.images.add(cursorImage);
        ss.images.add(cursorPressedImage);
        ss.sprites.put(0, new Sprite(0, mousePressed ? 0 : 1, true, 0,
                AffineTransform.translate(info.getLocation().x - captureArea.x - cursorOffset.x,
                        info.getLocation().y - captureArea.y - cursorOffset.y)));
        buf.data = ss;

        mouseWasPressed = mousePressed;
        return buf;
    }

    @Override
    public Rational getInitialDelay() {
        return initialDelay;
    }

    @Override
    public Rational getInterval() {
        return interval;
    }
}
