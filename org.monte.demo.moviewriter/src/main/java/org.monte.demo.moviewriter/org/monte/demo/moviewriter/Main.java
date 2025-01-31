/*
 * @(#)TestMovieWriters.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviewriter;

import org.monte.media.av.Codec;
import org.monte.media.av.CodecSpi;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.MovieWriterSpi;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.VideoFormatKeys.PixelFormat;
import org.monte.media.color.Colors;
import org.monte.media.jcodec.mp4.JCodecMP4WriterSpi;
import org.monte.media.math.Rational;
import org.monte.media.mp4.MP4WriterSpi;
import org.monte.media.mp4.codec.video.H264CodecSpi;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVC1;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_DIB;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_MJPG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_RLE8;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MotionSearchRangeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PixelFormatKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Demonstrates the use of {@link MovieWriter}.
 *
 * @author Werner Randelshofer
 */
public class Main {


    /**
     * Creates a buffered image of the specified format.
     */
    private static BufferedImage createImage(Format format) {
        int depth = format.get(DepthKey);
        int width = format.get(WidthKey);
        int height = format.get(HeightKey);
        PixelFormat pixelFormat = format.get(PixelFormatKey);

        BufferedImage img;
        switch (depth) {
            case 24:
            default: {
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                break;
            }
            case 8:
                if (pixelFormat == PixelFormat.GRAY) {
                    img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                    break;
                } else {
                    IndexColorModel palette = Colors.createMacColors();
                    img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);
                    break;
                }
        }
        return img;
    }

    private static void drawAnimationFrame(Graphics2D g, double second, int frameIndex, int frameCount) {
        drawClock(g, 232, 240, 150, second);

        g.setPaint(Color.WHITE);
        g.fillRect(472, 10, 168, 110);
        g.setPaint(Color.BLACK);
        g.drawString("Frame " + (frameIndex + 1) + " of " + frameCount, 473, 24);
    }

    private static void drawClock(Graphics2D g, int cx, int cy, int radius, double timeInSeconds) {
        g.setPaint(Color.WHITE);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        double timeInMinutes = timeInSeconds / 60.0;
        double timeInHours = timeInMinutes / 60.0;
        drawClockHand(g, cx, cy, -10, radius / 2, new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), Color.BLACK, (timeInHours) * (Math.PI * 2.0 / 12.0));
        drawClockHand(g, cx, cy, -10, radius - 20, new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), new Color(0x1a1a1a), (timeInMinutes) * (Math.PI * 2.0 / 60.0));
        drawClockHand(g, cx, cy, -64, radius - 1, new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL), Color.RED, (timeInSeconds) * (Math.PI * 2.0 / +60.0));
        drawClockHand(g, cx, cy, -64, -24, new BasicStroke(20, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL), Color.RED, (timeInSeconds) * (Math.PI * 2.0 / +60.0));
        // Draw plug
        int plugRadius = 12;
        g.setPaint(Color.WHITE);
        g.fillOval(cx - plugRadius, cy - plugRadius, plugRadius * 2, plugRadius * 2);
        g.setStroke(new BasicStroke(10));
        g.setPaint(new Color(0x333333));
        g.drawOval(cx - plugRadius, cy - plugRadius, plugRadius * 2, plugRadius * 2);
    }

    private static void drawClockHand(Graphics2D g, int cx, int cy, int radius1, int radius2, Stroke stroke, Color color, double theta) {
        AffineTransform tx = new AffineTransform();
        tx.setToRotation(theta % (Math.PI * 2), cx, cy);
        g.setTransform(tx);
        g.setColor(color);
        g.setStroke(stroke);
        g.drawLine(cx, cy - radius1, cx, cy - radius2);
        tx.setToIdentity();
        g.setTransform(tx);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("MovieWriterDemo " + Main.class.getPackage().getImplementationVersion());
        System.out.println("This is a demo of the Monte Media library.");
        System.out.println("Copyright © Werner Randelshofer. All Rights Reserved.");
        System.out.println("License: MIT License");
        System.out.println();

        try {
            var m = new Main();
            float quality = 0.75f;
            Format baseFormat = new Format(QualityKey, quality, KeyFrameIntervalKey, 60);
            List<TestData> list = new ArrayList<>();
            list.add(new TestData(new File("moviewriterdemo-rle8.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-rle24.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-rle16.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 16)));
            list.add(new TestData(new File("moviewriterdemo-rle8.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_RLE8, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-raw16.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 16)));
            list.add(new TestData(new File("moviewriterdemo-h264-motion0-jcodec.mp4"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), null, new JCodecMP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion16-jcodec.mp4"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 16), null, new JCodecMP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion0.mp4"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), new H264CodecSpi(), new MP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion0.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), new H264CodecSpi(), new MP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion0.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), new H264CodecSpi(), new MP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion16.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 16), new H264CodecSpi(), new MP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-h264-motion16.mp4"), baseFormat.prepend(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 16), new H264CodecSpi(), new MP4WriterSpi()));
            list.add(new TestData(new File("moviewriterdemo-jpg-q" + quality + ".avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-jpg-q" + quality + ".mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_JPEG, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-png.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_PNG, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-png.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_PNG, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-png.zip"), baseFormat.prepend(EncodingKey, ENCODING_AVI_PNG, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-raw24.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_DIB, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-raw24.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-raw8.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_DIB, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-raw8.mov"), baseFormat.prepend(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-raw8gray.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_DIB, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY)));
            list.add(new TestData(new File("moviewriterdemo-rle8gray.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_RLE8, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY)));
            list.add(new TestData(new File("moviewriterdemo-tscc16.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16)));
            list.add(new TestData(new File("moviewriterdemo-tscc16.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16)));
            list.add(new TestData(new File("moviewriterdemo-tscc24.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-tscc24.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24)));
            list.add(new TestData(new File("moviewriterdemo-tscc8.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-tscc8.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8)));
            list.add(new TestData(new File("moviewriterdemo-tscc8gray.avi"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY)));
            list.add(new TestData(new File("moviewriterdemo-tscc8gray.mov"), baseFormat.prepend(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY)));
            for (TestData data : list) {
                m.test(data.file, data.format, data.codecSpi, data.movieWriterSpi);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private record TestData(File file, Format format, CodecSpi codecSpi, MovieWriterSpi movieWriterSpi) {
        public TestData(File file, Format format) {
            this(file, format, null, null);
        }
    }

    private void test(File file, Format format, CodecSpi codecSpi, MovieWriterSpi movieWriterSpi) throws IOException {
        System.out.print("Writing " + file.getAbsolutePath());
        long startTime = System.nanoTime();

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
        drawBackgroundImage(g);

        MovieWriter out = null;
        int n = frameRate.multiply(60).intValue();
        try {
            // Create the writer
            out = movieWriterSpi == null ? Registry.getInstance().getWriter(file) : movieWriterSpi.create(file);

            // Add a track to the writer
            int trackIndex = out.addTrack(format);
            if (codecSpi != null) {
                Codec codec = codecSpi.create();
                Format actualInputFormat = codec.setInputFormat(format.prepend(
                        EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));
                codec.setOutputFormat(actualInputFormat.prepend(EncodingKey, format.get(EncodingKey), DataClassKey, byte[].class));
                out.setCodec(trackIndex, codec);
            }

            // Draw the animation
            for (int i = 0; i < n; i++) {
                double t = frameRate.divide(i).doubleValue() + 8 * 3600 + 25 * 60;
                drawAnimationFrame(g, t, i, n);

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
        System.out.println(", " + (int) ((n * 1e9) / (System.nanoTime() - startTime)) + " fps");
    }

    private static void drawBackgroundImage(Graphics2D g) throws IOException {
        var backgroundImage = ImageIO.read(Main.class.getResource("BackgroundImage.png"));
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }
    }
}
