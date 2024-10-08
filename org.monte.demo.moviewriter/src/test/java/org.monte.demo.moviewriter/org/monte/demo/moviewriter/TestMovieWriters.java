/*
 * @(#)TestMovieWriters.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviewriter;

import org.junit.jupiter.api.Test;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieReader;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.VideoFormatKeys.PixelFormat;
import org.monte.media.avi.AVIReader;
import org.monte.media.avi.AVIWriter;
import org.monte.media.color.Colors;
import org.monte.media.math.Rational;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
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
 * Demonstrates the use of {@link AVIReader} and {@link AVIWriter}.
 *
 * @author Werner Randelshofer
 */
public class TestMovieWriters {

    private final static boolean INTERACTIVE = false;
    private static final int CLOCK_START_TIME = 8 * 3600 + 25 * 60;

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

    @Test
    public void testMovieWriter() throws Exception {


        try {
            var m = new TestMovieWriters();
            m.test(new File("moviewriterdemo-png.avi"), new Format(EncodingKey, ENCODING_AVI_PNG, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-h264-motion0.avi"), new Format(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), true);
            m.test(new File("moviewriterdemo-h264-motion0.mp4"), new Format(EncodingKey, ENCODING_AVC1, DepthKey, 24, QualityKey, 0.75f, MotionSearchRangeKey, 0), true);
            m.test(new File("moviewriterdemo-h264-motion0.mov"), new Format(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 0), false);
            m.test(new File("moviewriterdemo-h264-motion16.mp4"), new Format(EncodingKey, ENCODING_AVC1, DepthKey, 24, QualityKey, 0.75f, MotionSearchRangeKey, 16), true);
            m.test(new File("moviewriterdemo-h264-motion16.mov"), new Format(EncodingKey, ENCODING_AVC1, DepthKey, 24, MotionSearchRangeKey, 16), false);
            m.test(new File("moviewriterdemo-jpg-q0.75.avi"), new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 0.75f), true);
            m.test(new File("moviewriterdemo-jpg-q0.75.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_JPEG, DepthKey, 24, QualityKey, 0.75f), true);
            m.test(new File("moviewriterdemo-png.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_PNG, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-png.zip"), new Format(EncodingKey, ENCODING_AVI_PNG, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-raw24.avi"), new Format(EncodingKey, ENCODING_AVI_DIB, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-raw24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 24), false);
            m.test(new File("moviewriterdemo-raw8.avi"), new Format(EncodingKey, ENCODING_AVI_DIB, DepthKey, 8), true);
            m.test(new File("moviewriterdemo-raw8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_RAW, DepthKey, 8), false);
            m.test(new File("moviewriterdemo-raw8gray.avi"), new Format(EncodingKey, ENCODING_AVI_DIB, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY), true);
            m.test(new File("moviewriterdemo-rle16.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 16), false);
            m.test(new File("moviewriterdemo-rle24.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 24), false);
            m.test(new File("moviewriterdemo-rle8.avi"), new Format(EncodingKey, ENCODING_AVI_RLE8, DepthKey, 8), true);
            m.test(new File("moviewriterdemo-rle8.mov"), new Format(EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 8), false);
            m.test(new File("moviewriterdemo-rle8gray.avi"), new Format(EncodingKey, ENCODING_AVI_RLE8, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY), false);
            m.test(new File("moviewriterdemo-tscc16.avi"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16), true);
            m.test(new File("moviewriterdemo-tscc16.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 16), true);
            m.test(new File("moviewriterdemo-tscc24.avi"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-tscc24.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24), true);
            m.test(new File("moviewriterdemo-tscc8.avi"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8), true);
            m.test(new File("moviewriterdemo-tscc8.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8), true);
            m.test(new File("moviewriterdemo-tscc8gray.avi"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY), true);
            m.test(new File("moviewriterdemo-tscc8gray.mov"), new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 8, PixelFormatKey, PixelFormat.GRAY), true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void test(File file, Format format, boolean tryToReadTheFile) throws Exception {
        testWriting(file, format);
        if (tryToReadTheFile) {
            testReading(file);
        }
    }

    private static void testReading(File file) throws IOException, ExecutionException, InterruptedException {
        System.out.println("Reading " + file.getAbsolutePath());
        long startTime = System.nanoTime();
        MovieReader in = null;
        int count = 0;

        try {
            // Create the reader
            in = Registry.getInstance().getReader(file);

            // Look for the first video track
            int track = 0;
            while (track < in.getTrackCount()
                    && in.getFormat(track).get(MediaTypeKey) != MediaType.VIDEO) {
                track++;
            }

            // Read images from the track and compare with expected image
            Buffer inbuf = new Buffer();
            Buffer outbuf = new Buffer();
            BufferedImage actualImage = null;
            BufferedImage expectedImage = createImage(in.getFormat(track));
            Graphics2D g = expectedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackgroundImage(g);
            Codec codec = Registry.getInstance().getCodec(in.getFormat(track), new Format(EncodingKey, ENCODING_BUFFERED_IMAGE));

            if (codec == null) {
                System.out.println("  Could not find a codec");
            }
            Loop:
            while (true) {
                in.read(track, inbuf);
                if (inbuf.isFlag(BufferFlag.END_OF_MEDIA)) {
                    break;
                }
                count++;
                if (codec != null) {
                    drawAnimationFrame(g, inbuf.timeStamp.doubleValue() + CLOCK_START_TIME,
                            (int) inbuf.sequenceNumber, in.getSampleCount(track));
                    codec.process(inbuf, outbuf);
                    actualImage = (BufferedImage) outbuf.data;
                    BufferedImage diffImage = compareImages(expectedImage, actualImage);
                    if (diffImage != null) {
                        System.out.println("  Image " + inbuf.sequenceNumber + " does not match the expected image.");
                        var finalActualImage = actualImage;
                        if (INTERACTIVE) {
                            CompletableFuture<Void> future = new CompletableFuture<>();
                            SwingUtilities.invokeLater(() -> {
                                JFrame f = new JFrame();
                                f.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        f.dispose();
                                        future.complete(null);
                                    }
                                });
                                f.setTitle(file.getName());
                                ImageComparisonForm form = new ImageComparisonForm();
                                f.setContentPane(form);
                                form.setFile(file);
                                form.setFrame((int) outbuf.sequenceNumber);
                                form.setExpectedImage(expectedImage);
                                form.setActualImage(finalActualImage);
                                form.setDiffImage(diffImage);
                                form.setOnClose(() -> {
                                    f.dispose();
                                    future.complete(null);
                                });
                                f.setSize(640, 480);
                                f.setVisible(true);
                            });
                            future.get();
                        }
                        break Loop;
                    }

                }
            }
        } catch (IOException e) {
            System.out.println("  Reading failed " + e.getMessage());
            //throw e;
        } finally {
            // Close the reader
            if (in != null) {
                in.close();
            }
        }
        System.out.println("  " + (System.nanoTime() - startTime) / 1e9 + " seconds for " + count + " frames");
    }

    /**
     * Compares two images.
     * <p>
     * Returns the following results:
     * <dl>
     *     <dt>-1</dt><dd>The images are equal</dd>
     *     <dt>-2</dt><dd>Comparison failed</dd>
     *     <dt>≥ 0</dt><dd>Index of the first mismatching pixel</dd>
     * </dl>
     *
     * @param expectedImage expected image
     * @param actualImage   actual image
     * @return diffImage or null if images are equal
     */
    private static BufferedImage compareImages(BufferedImage expectedImage, BufferedImage actualImage) {
        if (expectedImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            return compareImagesInt(expectedImage, actualImage);
        } else if (expectedImage.getRaster().getDataBuffer() instanceof DataBufferByte) {
            return compareImagesByte(expectedImage, actualImage);
        } else if (expectedImage.getRaster().getDataBuffer() instanceof DataBufferShort) {
            return compareImagesShort(expectedImage, actualImage);
        } else {
            throw new UnsupportedOperationException("unsupported data buffer " + expectedImage.getRaster().getDataBuffer().getClass());
        }
    }

    private static BufferedImage compareImagesByte(BufferedImage expectedImage, BufferedImage actualImage) {
        return null;
    }

    private static BufferedImage compareImagesShort(BufferedImage expectedImage, BufferedImage actualImage) {
        return null;
    }

    private static BufferedImage compareImagesInt(BufferedImage expectedImage, BufferedImage actualImage) {
        // convert actual image to same format as expected image
        BufferedImage convertedImage = new BufferedImage(actualImage.getWidth(), actualImage.getHeight(), expectedImage.getType());
        Graphics2D g = convertedImage.createGraphics();
        g.drawImage(actualImage, 0, 0, null);
        g.dispose();

        BufferedImage diffImage = new BufferedImage(actualImage.getWidth(), actualImage.getHeight(), expectedImage.getType());
        int[] dDat = ((DataBufferInt) diffImage.getRaster().getDataBuffer()).getData();
        int[] eDat = ((DataBufferInt) expectedImage.getRaster().getDataBuffer()).getData();
        int[] aDat = ((DataBufferInt) convertedImage.getRaster().getDataBuffer()).getData();

        // Erase alpha bits in expected image and actual image
        for (int i = 0; i < eDat.length; i++) {
            eDat[i] = eDat[i] & 0xffffff;
            aDat[i] = aDat[i] & 0xffffff;
        }

        // Search for mismatches
        int mismatch = Arrays.mismatch(eDat, 0, eDat.length, aDat, 0, aDat.length);
        boolean hasDiff = mismatch != -1;
        if (hasDiff) {
            System.out.println("  index=" + mismatch + " expected:" + Integer.toHexString(eDat[mismatch]) + " actual: " + Integer.toHexString(aDat[mismatch]));
        }
        while (mismatch != -1) {
            dDat[mismatch] = 0xffff0000;
            int result = Arrays.mismatch(eDat, mismatch + 1, eDat.length, aDat, mismatch + 1, aDat.length);
            mismatch = result == -1 ? -1 : mismatch + 1 + result;
        }
        return hasDiff ? diffImage : null;
    }

    private static void testWriting(File file, Format format) throws IOException {
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
        int n = 3;
        try {
            // Create the writer
            out = Registry.getInstance().getWriter(file);

            // Add a track to the writer
            out.addTrack(format);

            // Draw the animation
            for (int i = 0; i < n; i++) {
                double t = frameRate.divide(i).doubleValue() + CLOCK_START_TIME;
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
        var backgroundImage = ImageIO.read(TestMovieWriters.class.getResource("BackgroundImage.png"));
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }
    }
}
