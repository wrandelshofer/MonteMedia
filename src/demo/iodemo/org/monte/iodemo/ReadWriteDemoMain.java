/**
 * @(#)Main.java
 * Copyright © 2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.iodemo;

import java.util.ArrayList;
import org.monte.media.movie.MovieReader;
import org.monte.media.movie.MovieWriter;
import java.util.HashSet;
import org.monte.media.player.Codec;
import org.monte.media.player.Buffer;
import org.monte.media.math.Rational;
import org.monte.media.player.Format;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.Random;
import org.monte.media.player.BufferFlag;
import org.monte.media.player.Registry;
import org.monte.media.image.Images;
import static org.monte.media.player.VideoFormatKeys.*;

/**
 * Demonstrates the use of {@link MovieReader} and {@link MovieWriter}. <p> This
 * class creates a video file named montemedia-...codec....avi for each
 * installed Codec, and writes simple animation frames into the file. Then, it
 * attempts to read the frames back from the video file.
 *
 * @author Werner Randelshofer
 * @version $Id: ReadWriteDemoMain.java 364 2016-11-09 19:54:25Z werner $
 */
public class ReadWriteDemoMain {

    /**
     * Main function.
     *
     * @param args the command line arguments (ignored)
     */
    public static void main(String[] args) {
        try {
            HashSet<String> usedFilenames = new HashSet<String>();

            // Test all available AVI video formats
            for (Codec c : Registry.getInstance().getEncoders(new Format(MimeTypeKey, MIME_AVI, MediaTypeKey, MediaType.VIDEO))) {
                for (Format f : c.getOutputFormats(new Format(DataClassKey, BufferedImage.class))) {
                    if (f.get(MimeTypeKey) == MIME_AVI) {
                        String filename = "montemedia-" + f.get(EncodingKey).trim() + f.get(DepthKey) + ".avi";
                        String filename2 = "montemedia-" + f.get(EncodingKey).trim() + f.get(DepthKey) + "(2).avi";
                        if (usedFilenames.add(filename)) {
                            test(new File(filename), new File(filename2), f);
                        }
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void test(File file, File file2, Format format) throws IOException {
        testWriting(file, format);
        try {
            testReading(file);
            BufferedImage[] frames = readMovie(file);
            writeMovie(file2, frames);
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
        }
    }

    private static void testWriting(File file, Format format) throws IOException {
        System.out.println("Writing " + file);

        // Make the format more specific
        format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
                FrameRateKey, new Rational(30, 1),//
                WidthKey, 320, //
                HeightKey, 160);

        // Create a buffered image for this format
        BufferedImage img = createImage(format);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        MovieWriter out = null;
        try {
            // Create the writer
            out = Registry.getInstance().getWriter(format, file);

            // Add a track to the writer
            out.addTrack(format);

            // initialize the animation
            Random rnd = new Random(0); // use seed 0 to get reproducable output
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, img.getWidth(), img.getHeight());

            Buffer buf = new Buffer();
            buf.format = new Format(DataClassKey, BufferedImage.class);
            buf.sampleDuration = format.get(FrameRateKey).inverse();
            buf.data = img;
            for (int i = 0; i < 100; i++) {
                // Create an animation frame
                g.setColor(new Color(rnd.nextInt()));
                g.fillOval(rnd.nextInt(img.getWidth() - 30), rnd.nextInt(img.getHeight() - 30), 30, 30);

                // write it to the writer
                out.write(0, buf);
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

    private static void writeMovie(File file, BufferedImage[] frames) throws IOException {
        MovieWriter out = Registry.getInstance().getWriter(file);

        Format format = new Format(MediaTypeKey, MediaType.VIDEO, //
                EncodingKey, ENCODING_AVI_MJPG,
                FrameRateKey, new Rational(30, 1),//
                WidthKey, frames[0].getWidth(), //
                HeightKey, frames[0].getHeight(),//
                DepthKey, 24);

        int track = out.addTrack(format);

        try {
            Buffer buf = new Buffer();
            buf.format = new Format(DataClassKey, BufferedImage.class);
            buf.sampleDuration = format.get(FrameRateKey).inverse();
            for (int i = 0; i < frames.length; i++) {
                buf.data = frames[i];
                out.write(track, buf);
            }
        } finally {
            out.close();
        }
    }

    private static BufferedImage[] readMovie(File file) throws IOException {
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();

        MovieReader in = Registry.getInstance().getReader(file);
        try {

            Format format = new Format(DataClassKey, BufferedImage.class);

            int track = in.findTrack(0, new Format(MediaTypeKey, MediaType.VIDEO));
            if (track == -1) {
                throw new IOException("Movie has no video track");
            }
            Codec codec = Registry.getInstance().getCodec(in.getFormat(track), format);
            if (codec == null) {
                throw new IOException("Can not decode video track.");
            }

            Buffer inBuf = new Buffer();
            Buffer outBuf = new Buffer();
            do {
                in.read(track, inBuf);
                codec.process(inBuf, outBuf);
                if (!outBuf.isFlag(BufferFlag.DISCARD)) {
                    frames.add(Images.cloneImage((BufferedImage) outBuf.data));
                }

            } while (!inBuf.isFlag(BufferFlag.END_OF_MEDIA));
        } finally {
            in.close();
        }

        return frames.toArray(new BufferedImage[frames.size()]);
    }

    private static void testReading(File file) throws IOException {
        System.out.println("Reading " + file);
        MovieReader in = null;

        try {
            // Create the reader
            in = Registry.getInstance().getReader(file);

            // Look for the first video track
            int track = 0;
            while (track < in.getTrackCount()
                    && in.getFormat(track).get(MediaTypeKey) != MediaType.VIDEO) {
                track++;
            }

            // Read images from the track
            Buffer inputBuffer = new Buffer();
            Buffer codecBuffer = new Buffer();
            Format imageFormat = new Format(DataClassKey, BufferedImage.class);
            Codec codec = null;
            BufferedImage img = null;
            do {
                in.read(track, inputBuffer);
                Buffer imageBuffer = null;
                if (inputBuffer.format.matches(imageFormat)) {
                    imageBuffer = inputBuffer;
                } else {
                    if (codec == null) {
                        codec = Registry.getInstance().getCodec(inputBuffer.format, imageFormat);
                    }
                    if (codec == null) {
                        throw new UnsupportedOperationException("No Codec for " + inputBuffer.format);
                    }
                    codec.process(inputBuffer, codecBuffer);
                    imageBuffer = codecBuffer;
                }

                //...to do: do something with the image...

            } while (img != null);

        } finally {
            // Close the rader
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Creates a buffered image of the specified depth with a random color palette.
     */
    private static BufferedImage createImage(Format format) {
        int depth = format.get(DepthKey);
        int width = format.get(WidthKey);
        int height = format.get(HeightKey);

        Random rnd = new Random(0); // use seed 0 to get reproducable output
        BufferedImage img;
        switch (depth) {
            case 24:
            default: {
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                break;
            }
            case 8: {
                byte[] red = new byte[256];
                byte[] green = new byte[256];
                byte[] blue = new byte[256];
                for (int i = 0; i < 255; i++) {
                    red[i] = (byte) rnd.nextInt(256);
                    green[i] = (byte) rnd.nextInt(256);
                    blue[i] = (byte) rnd.nextInt(256);
                }
                rnd.setSeed(0); // set back to 0 for reproducable output
                IndexColorModel palette = new IndexColorModel(8, 256, red, green, blue);
                img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);
                break;
            }
            case 4: {
                byte[] red = new byte[16];
                byte[] green = new byte[16];
                byte[] blue = new byte[16];
                for (int i = 0; i < 15; i++) {
                    red[i] = (byte) rnd.nextInt(16);
                    green[i] = (byte) rnd.nextInt(16);
                    blue[i] = (byte) rnd.nextInt(16);
                }
                rnd.setSeed(0); // set back to 0 for reproducable output
                IndexColorModel palette = new IndexColorModel(4, 16, red, green, blue);
                img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);
                break;
            }
        }
        return img;
    }
}
