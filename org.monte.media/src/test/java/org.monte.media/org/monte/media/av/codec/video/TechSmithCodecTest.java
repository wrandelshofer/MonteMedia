/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.junit.jupiter.api.Test;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TechSmithCodecTest {
    @Test
    public void shouldEncodeDecode24BitKeyFrame() throws IOException {
        int width = 40, height = 30;
        int[] rgb24 = toRgb24(createFrame(width, height, 0, BufferedImage.TYPE_INT_RGB), true);
        TechSmithCodecCore codec = new TechSmithCodecCore();
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        codec.encodeKey24(encoded, rgb24, width, height, 0, width);
        byte[] encodedBytes = encoded.toByteArray();
        int[] actualPixels = new int[width * height];
        codec.decode24(encodedBytes, 0, encodedBytes.length, actualPixels, null, width, height, true);

        assertArrayEquals(rgb24, actualPixels);
    }

    @Test
    public void shouldEncodeDecode16BitKeyFrame() throws IOException {
        int width = 40, height = 30;
        BufferedImage frame24 = createFrame(width, height, 0, BufferedImage.TYPE_INT_RGB);
        BufferedImage frame16 = createFrame(width, height, 0, BufferedImage.TYPE_USHORT_555_RGB);
        int[] rgb24 = toRgb24(frame24, true);
        short[] rgb16 = toRgb16(frame16);
        TechSmithCodecCore codec = new TechSmithCodecCore();
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        codec.encodeKey16(encoded, rgb16, width, height, 0, width);
        byte[] encodedBytes = encoded.toByteArray();
        int[] actualPixels = new int[width * height];
        codec.decode16(encodedBytes, 0, encodedBytes.length, actualPixels, null, width, height, true);
        assertArrayEquals(rgb24, actualPixels);
    }

    @Test
    public void shouldEncodeDecode8BitKeyFrame() throws IOException {
        int width = 40, height = 30;
        BufferedImage frame24 = createFrame(width, height, 0, BufferedImage.TYPE_INT_RGB);
        BufferedImage frame8 = createFrame(width, height, 0, BufferedImage.TYPE_BYTE_INDEXED);
        int[] rgb24 = toRgb24(frame24, false);
        byte[] rgb8 = toRgb8(frame8);
        TechSmithCodecCore codec = new TechSmithCodecCore();
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        codec.encodeKey8(encoded, rgb8, width, height, 0, width);
        byte[] encodedBytes = encoded.toByteArray();
        int[] actualPixels = new int[width * height];
        int[] palette = new int[256];
        ((IndexColorModel) frame8.getColorModel()).getRGBs(palette);
        codec.setPalette(palette);
        codec.decode8(encodedBytes, 0, encodedBytes.length, actualPixels, null, width, height, true);
        assertArrayEquals(rgb24, actualPixels);
    }

    /**
     * Creates a video frame at the specified time.
     *
     * @param width  width of the frame
     * @param height height of the frame
     * @param time   time in range[0,1].
     * @param type
     * @return
     */
    private static BufferedImage createFrame(int width, int height, float time, int type) throws IOException {
        BufferedImage img = new BufferedImage(width, height, type);
        Graphics2D g = img.createGraphics();
        g.setBackground(Color.BLACK);
        int strokeWidth = 6;
        int strokeWidth2 = strokeWidth / 2;
        float h = (float) (0.5 * arcLength(width - strokeWidth, height - strokeWidth));
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 5f, new float[]{h, h}, (1 - time) * h));
        g.clearRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.drawOval(strokeWidth2, strokeWidth2, width - strokeWidth, height - strokeWidth);
        g.dispose();
        //ImageIO.write(img, "PNG", new File("frame" + time + ".png"));
        return img;
    }

    private static int[] toRgb24(BufferedImage img, boolean clearAlphaBits) {
        AbstractVideoCodec abstractVideoCodec = new AbstractVideoCodec(null, null) {
            @Override
            public int process(Buffer in, Buffer out) {
                return 0;
            }
        };
        Buffer buf = new Buffer();
        buf.data = img;
        int[] rgb24 = abstractVideoCodec.getRGB24(buf);
        // Clear Alpha bits:
        if (clearAlphaBits) {
            for (int i = 0; i < rgb24.length; i++) {
                rgb24[i] = rgb24[i] & 0xffffff;
            }
        }
        return rgb24;
    }

    private static short[] toRgb16(BufferedImage img) {
        Format format = new Format(VideoFormatKeys.WidthKey, img.getWidth(), VideoFormatKeys.HeightKey, img.getHeight());
        AbstractVideoCodec abstractVideoCodec = new AbstractVideoCodec(null, null) {
            {
                this.outputFormat = format;
            }

            @Override
            public int process(Buffer in, Buffer out) {
                return 0;
            }
        };
        Buffer buf = new Buffer();
        buf.format = format;
        buf.data = img;
        return abstractVideoCodec.getRGB16(buf);
    }

    private static byte[] toRgb8(BufferedImage img) {
        Format format = new Format(VideoFormatKeys.WidthKey, img.getWidth(), VideoFormatKeys.HeightKey, img.getHeight());
        AbstractVideoCodec abstractVideoCodec = new AbstractVideoCodec(null, null) {
            {
                this.outputFormat = format;
            }

            @Override
            public int process(Buffer in, Buffer out) {
                return 0;
            }
        };
        Buffer buf = new Buffer();
        buf.format = format;
        buf.data = img;
        return abstractVideoCodec.getIndexed8(buf);
    }

    /**
     * Computes the arc length of an oval.
     * <a href="https://www.mathematik.ch/anwendungenmath/ellipsenumfang/">www.mathematik.ch</a>
     *
     * @param width  width of the oval
     * @param height height of the oval
     * @return arc length
     */
    private static double arcLength(int width, int height) {
        double a = width / 2.0, b = height / 2.0;
        double lambda = (a - b) / (a + b);
        return (a + b) * Math.PI * (1 + (3 * lambda * lambda) / (10 + Math.sqrt(4 - 3 * lambda * lambda)));
    }
}