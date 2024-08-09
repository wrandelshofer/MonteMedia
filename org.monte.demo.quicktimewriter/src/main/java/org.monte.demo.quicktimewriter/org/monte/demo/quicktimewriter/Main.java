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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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
            test(new File("quicktimedemo-jpg.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_JPEG, DepthKey, 24, QualityKey, 1f));
            test(new File("quicktimedemo-jpg-q0.5.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_JPEG, DepthKey, 24, QualityKey, 0.5f));
            test(new File("quicktimedemo-png.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_PNG, DepthKey, 24));
            test(new File("quicktimedemo-raw24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 24));
            test(new File("quicktimedemo-raw8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 8));
            test(new File("quicktimedemo-anim8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 8));
            test(new File("quicktimedemo-tscc8.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8));
            test(new File("quicktimedemo-tscc8gray.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8, PixelFormatKey, VideoFormatKeys.PixelFormat.GRAY));
            test(new File("quicktimedemo-tscc16.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16));
            test(new File("quicktimedemo-tscc24.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24));
            test(new File("quicktimedemo-rle8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 8));
            test(new File("quicktimedemo-rle16.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 16));
            test(new File("quicktimedemo-rle24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 24));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void test(File file, Format format) throws IOException {
        testWriting(file, format);
        try {
            testReading(file);
        } catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void testWriting(File file, Format format) throws IOException {
        System.out.println("Writing " + file.getAbsolutePath());

        // Make the format more specific
        format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
                FrameRateKey, new Rational(30, 1),//
                WidthKey, 640, //
                HeightKey, 480);

        // Create a buffered image for this format
        BufferedImage img = createImage(format);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        QuickTimeWriter out = null;
        try {
            // Create the writer
            out = new QuickTimeWriter(file);

            // Add a track to the writer
            out.addTrack(format);
            out.setVideoColorTable(0, img.getColorModel());

            // Draw the animation
            for (int i = 0, n = 61; i < n; i++) {
                double t = (double) i / n - 1;
                drawAnimationFrame(img, g, t);

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

    private static void drawAnimationFrame(BufferedImage img, Graphics2D g, double t) {
        int rhour = Math.min(img.getWidth(), img.getHeight()) / 6;
        int rminute = Math.min(img.getWidth(), img.getHeight()) / 4;
        int cx = img.getWidth() / 2;
        int cy = img.getHeight() / 2;

        double tminute = t;
        double thour = tminute / 60.0;
        Stroke sfine = new BasicStroke(1.0f);
        Stroke shour = new BasicStroke(7.0f);
        Stroke sminute = new BasicStroke(5.0f);
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, img.getWidth(), img.getHeight());

        // draw color-changing dot
        g.setColor(Color.getHSBColor((float) t, 0.8f, 0.6f));
        Ellipse2D ellipse = new Ellipse2D.Double(cx - 10, cy + rhour - 10, 20, 20);
        g.fill(ellipse);

        // draw color strip
        float[] fractions = new float[12];
        Color[] colors = new Color[fractions.length];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = (float) i / (fractions.length - 1);
            colors[i] = Color.getHSBColor(fractions[i], 0.8f, 0.6f);
        }
        g.setPaint(new LinearGradientPaint(cx - rminute, cy + rminute, cx + rminute, cy + rminute,
                fractions,
                colors));
        Rectangle2D rectangle = new Rectangle2D.Double(cx - rminute, cy + rhour + 10, rminute * 2, 20);
        g.fill(rectangle);


        // draw clock hour hand
        Line2D.Double lhour = new Line2D.Double(cx, cy, cx + Math.sin(thour * Math.PI * 2) * rhour, cy - Math.cos(thour * Math.PI * 2) * rhour);
        g.setColor(Color.BLUE);
        g.setStroke(shour);
        g.draw(lhour);

        // draw clock minute hand
        g.setColor(Color.RED);
        Line2D.Double lminute = new Line2D.Double(cx, cy, cx + Math.sin(tminute * Math.PI * 2) * rminute, cy - Math.cos(tminute * Math.PI * 2) * rminute);
        g.setStroke(sminute);
        g.draw(lminute);
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
