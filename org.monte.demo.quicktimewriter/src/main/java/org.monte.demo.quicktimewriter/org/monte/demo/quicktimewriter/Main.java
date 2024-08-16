/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.quicktimewriter;

import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.color.Colors;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.QuickTimeReader;
import org.monte.media.quicktime.QuickTimeWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PixelFormatKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Demonstrates the use of {@link QuickTimeReader} and {@link QuickTimeWriter}.
 *
 * @author Werner Randelshofer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("QuickTimeDemo " + Main.class.getPackage().getImplementationVersion());
        System.out.println("This is a demo of the Monte Media library.");
        System.out.println("Copyright © Werner Randelshofer. All Rights Reserved.");
        System.out.println("License: MIT License");
        System.out.println();

        try {
            test(new File("quicktimedemo-jpg-q0.75.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_JPEG, DepthKey, 24, QualityKey, 0.75f), true);
            test(new File("quicktimedemo-png.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_PNG, DepthKey, 24), true);
            test(new File("quicktimedemo-raw24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 24), false);
            test(new File("quicktimedemo-raw8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 8), false);
            test(new File("quicktimedemo-tscc8.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8), true);
            test(new File("quicktimedemo-tscc8gray.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8,
                    PixelFormatKey, VideoFormatKeys.PixelFormat.GRAY), true);
            test(new File("quicktimedemo-tscc16.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16), true);
            test(new File("quicktimedemo-tscc24.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24), true);
            test(new File("quicktimedemo-rle8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 8), false);
            test(new File("quicktimedemo-rle16.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 16), false);
            test(new File("quicktimedemo-rle24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 24), false);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void test(File file, Format format, boolean tryToReadTheFile) throws IOException {
        testWriting(file, format);
        if (tryToReadTheFile) {
            try {
                testReading(file);
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void testWriting(File file, Format format) throws IOException {
        System.out.println("Writing " + file.getAbsolutePath());

        // Make the format more specific
        Rational frameRate = new Rational(10, 1);
        format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
                FrameRateKey, frameRate,//
                WidthKey, 640, //
                HeightKey, 480);

        // Create a buffered image for this format
        BufferedImage img = createImage(format);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var backgroundImage = ImageIO.read(Main.class.getResource("BackgroundImage.png"));
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }

        QuickTimeWriter out = null;
        try {
            // Create the writer
            out = new QuickTimeWriter(file);

            // Add a track to the writer
            out.addTrack(format);
            out.setVideoColorTable(0, img.getColorModel());

            // Draw the animation
            for (int i = 0, n = frameRate.multiply(60).intValue(); i < n; i++) {
                double t = frameRate.divide(i).doubleValue() + 8 * 3600 + 25 * 60;
                drawAnimationFrame(img, g, t, i, n);

                // write image to the writer
                out.write(0, img, 1);
            }

        } finally {
            // Close the writer
            if (out != null) {
                out.close();
            }

            // Dispose the graphics object
            g.dispose();
        }
    }

    private static void drawAnimationFrame(BufferedImage img, Graphics2D g, double second, int frameIndex, int frameCount) {
        drawClock(g, 232, 240, 150, second);

        g.setPaint(Color.WHITE);
        g.fillRect(472, 10, 168, 110);
        g.setPaint(Color.BLACK);
        g.drawString("Frame " + (frameIndex + 1) + " of " + frameCount, 473, 24);
    }

    private static void drawClock(Graphics2D g, int cx, int cy, int radius, double seconds) {
        g.setPaint(Color.WHITE);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);


        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        drawClockHand(g, cx, cy, -10, radius / 2, new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), Color.BLACK, (hours) * (Math.PI * 2.0 / 12.0));
        drawClockHand(g, cx, cy, -10, radius - 20, new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), new Color(0x1a1a1a), (minutes) * (Math.PI * 2.0 / 60.0));
        drawClockHand(g, cx, cy, -64, radius - 1, new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL), Color.RED, (seconds) * (Math.PI * 2.0 / +60.0));
        drawClockHand(g, cx, cy, -64, radius - 1, new BasicStroke(20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 4f, new float[]{20f, radius * 2}, 0f), Color.RED, (seconds) * (Math.PI * 2.0 / +60.0));

        // Draw plug
        int plugRadius = 12;
        g.setPaint(Color.WHITE);
        g.fillOval(cx - plugRadius, cy - plugRadius, plugRadius * 2, plugRadius * 2);
        g.setStroke(new BasicStroke(10));
        g.setPaint(new Color(0x333333));
        g.drawOval(cx - plugRadius, cy - plugRadius, plugRadius * 2, plugRadius * 2);
    }

    private static void drawClockHand(Graphics2D g, int cx, int cy, int radius1, int radius2, Stroke stroke, Color color, double angle) {
        angle = angle % (Math.PI * 2);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        g.setPaint(color);
        g.setStroke(stroke);
        g.draw(new Line2D.Double(cx + radius1 * sin, cy - radius1 * cos, cx + radius2 * sin, cy - radius2 * cos));
    }


    private static void testReading(File file) throws IOException {
        System.out.println("Reading " + file.getAbsolutePath());


        try (QuickTimeReader in = new QuickTimeReader(file)) {

            // Look for the first video track
            int track = 0;
            while (track < in.getTrackCount()
                    && (in.getFormat(track) == null || in.getFormat(track).get(MediaTypeKey) != MediaType.VIDEO)) {
                System.out.println("Skipping track " + track + " with format " + in.getFormat(track));
                track++;
            }
            if (track >= in.getTrackCount()) {
                System.out.println("ERROR no video track found.");
                return;
            }
            // Read images from the track
            BufferedImage img = null;
            do {
                img = in.read(track, img);

                //...to do: do something with the image...
            } while (img != null);
        }
    }

    /**
     * Creates a buffered image of the specified format.
     */
    private static BufferedImage createImage(Format format) {
        int depth = format.get(DepthKey);
        int width = format.get(WidthKey);
        int height = format.get(HeightKey);
        VideoFormatKeys.PixelFormat pixelFormat = format.get(PixelFormatKey);

        BufferedImage img;
        switch (depth) {
            case 24:
            default: {
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                break;
            }
            case 8: {
                if (pixelFormat == VideoFormatKeys.PixelFormat.GRAY) {
                    img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                    break;
                } else {
                    IndexColorModel palette = Colors.createMacColors();
                    img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);
                    break;
                }
            }
        }
        return img;
    }
}
