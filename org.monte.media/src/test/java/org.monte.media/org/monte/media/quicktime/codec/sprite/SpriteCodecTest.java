/*
 * @(#)SpriteCodecTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

import org.junit.jupiter.api.Test;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.AffineTransform;
import org.monte.media.math.Rational;
import org.monte.media.util.HexDump;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.ENCODING_JAVA_SPRITE;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.ENCODING_QUICKTIME_SPRITE;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.SpriteImageEncodingKey;

public class SpriteCodecTest {
    private boolean verbose = false;
    private boolean showFrame = true;

    @Test

    public void shouldEncodeSprite() throws IOException {
        SpriteCodec codec = new SpriteCodec();
        codec.setInputFormat(new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_JAVA,
                EncodingKey, ENCODING_JAVA_SPRITE, DataClassKey, SpriteSample.class));
        Format outputFormat = codec.setOutputFormat(
                new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_QUICKTIME,
                        EncodingKey, ENCODING_QUICKTIME_SPRITE, DataClassKey, byte[].class,
                        SpriteImageEncodingKey, ENCODING_QUICKTIME_PNG));

        // encode a key frame
        SpriteSample spriteSample = new SpriteSample();
        spriteSample.images.add(ImageIO.read(SpriteCodecTest.class.getResource("Cursor.black-32bit.png")));
        spriteSample.images.add(ImageIO.read(SpriteCodecTest.class.getResource("Cursor.black.pressed-32bit.png")));
        spriteSample.sprites.put(1, new Sprite(1, 1, true, 1, AffineTransform.IDENTITY));
        var in = new Buffer();
        in.data = spriteSample;
        var out = new Buffer();
        if (verbose) System.out.println("KeyFrame");
        doEncodeSprite(spriteSample, codec, in, out);
        assertEquals(3719, out.length);
        assertEquals(outputFormat, out.format);

        // encode a delta frame
        spriteSample.sprites.put(1, new Sprite(1, 1, true, 1, AffineTransform.translate(20, 30)));
        in.data = spriteSample;
        if (verbose) System.out.println("DeltaFrame");
        doEncodeSprite(spriteSample, codec, in, out);
        assertEquals(96, out.length);

        // encode another delta frame
        spriteSample.sprites.put(1, new Sprite(1, 2, true, 1, AffineTransform.translate(20, 30)));
        in.data = spriteSample;
        if (verbose) System.out.println("DeltaFrame");
        doEncodeSprite(spriteSample, codec, in, out);
        assertEquals(62, out.length);
    }

    private void doEncodeSprite(SpriteSample spriteSample, SpriteCodec codec, Buffer in, Buffer out) {
        var status = codec.process(in, out);
        assertEquals(Codec.CODEC_OK, status, "codec must not have failed");
        if (verbose) {
            HexDump hexDump = new HexDump();
            String actual = hexDump.formatHex((byte[]) out.data, out.offset, out.length);
            System.out.println(actual);
        }

    }

    @Test

    public void shouldWriteAndReadVideoWithSpriteTrack32Bit() throws IOException, InterruptedException, InvocationTargetException {
        int width = 640;
        int height = 480;
        Format fileFormat = new Format(
                MimeTypeKey, MIME_QUICKTIME,
                KeyFrameIntervalKey, 30,
                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 32,
                WidthKey, width, HeightKey, height);
        Format spriteTrackFormat = new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.SPRITE,
                DataClassKey, SpriteSample.class, WidthKey, width, HeightKey, height,
                KeyFrameIntervalKey, 15);
        File file = new File("SpriteCodeTest-MovingCursor-Sprites-32Bit.mov");
        String cursorBlackResource = "Cursor.black-32bit.png";
        BufferedImage cursorBlack = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackResource));
        String cursorBlackPressedResource = "Cursor.black.pressed-32bit.png";
        BufferedImage cursorBlackPressed = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackPressedResource));
        assertNotNull(cursorBlack, cursorBlackResource);
        assertNotNull(cursorBlackPressed, cursorBlackPressedResource);

        doWrite(file, fileFormat, spriteTrackFormat, cursorBlack, cursorBlackPressed);
        doRead(file, fileFormat, spriteTrackFormat, showFrame);
    }

    @Test

    public void shouldWriteAndReadVideoWithSpriteTrack24Bit() throws IOException, InterruptedException, InvocationTargetException {
        int width = 640;
        int height = 480;
        Format fileFormat = new Format(
                KeyFrameIntervalKey, 30,
                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DepthKey, 24,
                WidthKey, width, HeightKey, height);
        Format spriteTrackFormat = new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.SPRITE,
                DataClassKey, SpriteSample.class, WidthKey, width, HeightKey, height,
                KeyFrameIntervalKey, 15);
        File file = new File("SpriteCodeTest-MovingCursor-Sprites-24Bit.mov");
        String cursorBlackResource = "Cursor.black-24bit.png";
        BufferedImage cursorBlack = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackResource));
        String cursorBlackPressedResource = "Cursor.black.pressed-24bit.png";
        BufferedImage cursorBlackPressed = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackPressedResource));
        assertNotNull(cursorBlack, cursorBlackResource);
        assertNotNull(cursorBlackPressed, cursorBlackPressedResource);

        doWrite(file, fileFormat, spriteTrackFormat, cursorBlack, cursorBlackPressed);
        doRead(file, fileFormat, spriteTrackFormat, showFrame);
    }

    @Test

    public void shouldWriteAndReadVideoWithSpriteTrack16Bit() throws IOException, InterruptedException, InvocationTargetException {
        int width = 640;
        int height = 480;
        Format fileFormat = new Format(
                KeyFrameIntervalKey, 30,
                DepthKey, 16,
                WidthKey, width, HeightKey, height);
        Format spriteTrackFormat = new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.SPRITE,
                DataClassKey, SpriteSample.class, WidthKey, width, HeightKey, height,
                KeyFrameIntervalKey, 15);
        File file = new File("SpriteCodeTest-MovingCursor-Sprites-16Bit.mov");
        String cursorBlackResource = "Cursor.black-16bit.png";
        BufferedImage cursorBlack = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackResource));
        String cursorBlackPressedResource = "Cursor.black.pressed-16bit.png";
        BufferedImage cursorBlackPressed = ImageIO.read(SpriteCodecTest.class.getResource(cursorBlackPressedResource));
        assertNotNull(cursorBlack, cursorBlackResource);
        assertNotNull(cursorBlackPressed, cursorBlackPressedResource);

        doWrite(file, fileFormat, spriteTrackFormat, cursorBlack, cursorBlackPressed);
        doRead(file, fileFormat, spriteTrackFormat, false);
    }

    @Test
    public void shouldReadFileWithSpriteTrack() throws IOException, InterruptedException, InvocationTargetException {
        int width = 240;
        int height = 160;
        Format fileFormat = new Format(
                KeyFrameIntervalKey, 30,
                DepthKey, 24,
                WidthKey, width, HeightKey, height);
        Format spriteTrackFormat = new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.SPRITE,
                DataClassKey, SpriteSample.class, WidthKey, width, HeightKey, height,
                KeyFrameIntervalKey, 15);
        File file = new File("data/QuickTimeLogo-Sprites.MOV");
        doRead(file, fileFormat, spriteTrackFormat, false);
    }

    private static class SpritePanel extends JPanel {
        private SpriteSample spriteSample;

        public SpritePanel() {
            setLayout(null);
        }

        public SpriteSample getSpriteSample() {
            return spriteSample;
        }

        public void setSpriteSample(SpriteSample spriteSample) {
            this.spriteSample = spriteSample;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (spriteSample == null) return;
            for (Sprite spr : spriteSample.sprites.values()) {
                if (spr.visible() && spr.imageId() >= 1 && spr.imageId() <= spriteSample.images.size()) {
                    BufferedImage img = spriteSample.images.get(spr.imageId() - 1);
                    System.out.println(spr + "\n  " + img);
                    Point2D.Double p = spr.transform().transform(0, 0, Point2D.Double::new);
                    System.out.println("  coords: " + p);
                    g.drawImage(img, (int) p.x, (int) p.y, null);
                }
            }
        }

    }

    private void doRead(File file, Format fileFormat, Format spriteTrackFormat, boolean showFrame) throws IOException, InterruptedException, InvocationTargetException {
        var panel = new SpritePanel();
        var frameLabel = new JLabel("Frame: 0");
        var nextButton = new JButton("Next");
        BlockingQueue<String> buttonQueue = new LinkedBlockingQueue<>();
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonQueue.add("Next");
            }
        });
        if (showFrame) {
            SwingUtilities.invokeAndWait(() -> {
                GraphicsConfiguration config = null;
                for (GraphicsDevice screenDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    for (GraphicsConfiguration configuration : screenDevice.getConfigurations()) {
                        if (configuration.getColorModel().getColorSpace().isCS_sRGB()) {
                            config = configuration;
                            break;
                        }
                    }

                }

                JFrame frame = new JFrame(config);
                var borderPanel = new JPanel();
                borderPanel.setLayout(new BorderLayout());
                borderPanel.add(panel, BorderLayout.CENTER);
                var flowPanel = new JPanel();
                borderPanel.add(flowPanel, BorderLayout.SOUTH);
                flowPanel.add(nextButton);
                flowPanel.add(frameLabel);
                frame.getContentPane().add(borderPanel);
                panel.setPreferredSize(new Dimension(fileFormat.get(WidthKey), fileFormat.get(HeightKey)));
                frame.setSize(borderPanel.getPreferredSize());
                System.out.println("borderPanel size " + panel.getPreferredSize());
                frame.setVisible(true);
            });
        }

        try (MovieReader in = Registry.getInstance().getReader(file)) {
            int spriteTrack = -1;
            for (int i = 0, n = in.getTrackCount(); i < n; i++) {
                Format actualFormat = in.getFormat(i);
                if (FormatKeys.MediaType.SPRITE.equals(actualFormat.get(MediaTypeKey))) {
                    spriteTrack = i;
                    break;
                }
            }
            assertNotEquals(-1, spriteTrack, "must have at least one sprite track");
            SpriteCodec codec = new SpriteCodec();
            codec.setInputFormat(in.getFormat(spriteTrack));
            codec.setOutputFormat(spriteTrackFormat);

            Buffer inBuf = new Buffer();
            Buffer outBuf = new Buffer();
            int count = 0;
            while (true) {
                in.read(spriteTrack, inBuf);
                assertNull(inBuf.exception, "inBuf must not have an exception");
                if (inBuf.isFlag(BufferFlag.END_OF_MEDIA)) break;
                if (inBuf.isFlag(BufferFlag.DISCARD)) continue;
                codec.process(inBuf, outBuf);
                if (outBuf.exception != null) {
                    outBuf.exception.printStackTrace();
                }
                assertNull(outBuf.exception, "outBuf must not have an exception");
                if (outBuf.isFlag(BufferFlag.END_OF_MEDIA)) break;
                if (outBuf.isFlag(BufferFlag.DISCARD)) continue;
                count++;
                assertInstanceOf(SpriteSample.class, outBuf.data, "must have decoded the sample into a Sprite");

                if (outBuf.data instanceof SpriteSample spr) {
                    int finalCount = count;
                    if (showFrame) {
                        SwingUtilities.invokeAndWait(() -> {
                            panel.setSpriteSample(spr);
                            frameLabel.setText("Frame: " + finalCount);
                        });
                        buttonQueue.poll(10, TimeUnit.SECONDS);
                    }
                }
            }
            assertEquals(in.getSampleCount(spriteTrack), count, "must have all samples");
        }
    }

    private void doWrite(File file, Format fileFormat, Format spriteTrackFormat, BufferedImage cursorImage, BufferedImage cursorPressedImage) throws IOException {
        int width = fileFormat.get((WidthKey));
        int height = fileFormat.get((HeightKey));
        try (MovieWriter out = Registry.getInstance().getWriter(fileFormat, file)) {
            int spriteTrack = out.addTrack(spriteTrackFormat);

            SpriteCodec codec = new SpriteCodec();
            Format inputFormat = new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_JAVA_SPRITE, DataClassKey, SpriteSample.class);
            codec.setInputFormat(inputFormat);
            codec.setOutputFormat(
                    new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_QUICKTIME,
                            EncodingKey, ENCODING_QUICKTIME_SPRITE, DataClassKey, byte[].class,
                            KeyFrameIntervalKey, 15));
            out.setCodec(spriteTrack, codec);

            // create the sprite
            int cx = width / 2;
            int cy = height / 2;
            int radius = Math.min(cx, cy) - 100;
            SpriteSample spriteSample = new SpriteSample();
            spriteSample.images.add(cursorImage);
            spriteSample.images.add(cursorPressedImage);
            spriteSample.sprites.put(1, new Sprite(1, 1, true, 1, AffineTransform.translate(cx + radius, cy)));
            var buf = new Buffer();
            buf.format = inputFormat;
            buf.data = spriteSample;
            buf.sampleDuration = Rational.valueOf(1, 30);

            // move the sprite in a circle around the screen
            for (int i = 0; i < 30; i++) {
                double x = cx + Math.cos(Math.PI * 2 * i / 30.0) * radius;
                double y = cy + Math.sin(Math.PI * 2 * i / 30.0) * radius;
                spriteSample.sprites.put(1, new Sprite(1, 1, true, 1, AffineTransform.translate(x, y)));
                out.write(spriteTrack, buf);
            }

            // toggle to image 2 and back to image 1 to simulate a mouse click
            spriteSample.sprites.put(1, new Sprite(1, 2, true, 1, AffineTransform.translate(cx + radius, cy)));
            for (int i = 0; i < 5; i++) {
                out.write(spriteTrack, buf);
            }
            spriteSample.sprites.put(1, new Sprite(1, 1, true, 1, AffineTransform.translate(cx + radius, cy)));
            for (int i = 0; i < 5; i++) {
                out.write(spriteTrack, buf);
            }
        }
    }
}
