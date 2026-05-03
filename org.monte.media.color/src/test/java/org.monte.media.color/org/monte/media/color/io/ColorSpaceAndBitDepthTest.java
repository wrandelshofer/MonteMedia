/*
 * @(#)BitDepthTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.io;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.color.RecBT2020ColorSpace;
import org.monte.media.color.util.TestImageFactory;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.monte.media.image.Images.toImageWithColorModel_usingColorConvertOp;

@Disabled
public class ColorSpaceAndBitDepthTest {


    @ParameterizedTest
    @ValueSource(ints = {/*1, 2, 3, 4, 5, 6,*/ 7, 8/*, 9, 10*/})
    public void shouldGetExpectedRgbForSrgbImage(int bitDepth) throws InterruptedException {
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage img = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);

        showImage("sRGB cube in sRGB space, " + bitDepth + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);
    }

    // FAIL: This test passes, but the displayed image is incorrect!
    @ParameterizedTest
    @ValueSource(ints = {/*1, 2, 3, 4, 5, 6,*/ 7, 8/*, 9, 10*/})
    public void shouldGetExpectedRgbForRec2020ImageUsingColorConvert(int bitDepth) throws InterruptedException {

        // Draw an RGB color in sRGB space
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage src = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);
        DirectColorModel scm = (DirectColorModel) src.getColorModel();

        // Convert the image to REC2020
        ICC_ColorSpace rec2020Space = new ICC_ColorSpace(TestImageFactory.createRec2020Profile());
        DirectColorModel cm = new DirectColorModel(rec2020Space, scm.getPixelSize(), scm.getRedMask(), scm.getGreenMask(), scm.getBlueMask(), scm.getAlphaMask(),
                scm.isAlphaPremultiplied(), scm.getTransferType());
        BufferedImage img = toImageWithColorModel_usingColorConvertOp(src, cm);
        assertEquals(rec2020Space, img.getColorModel().getColorSpace());

        showImage("sRGB cube in REC2020 space CONVERT, " + img.getColorModel().getComponentSize(0) + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel & 0xff_fffcfc), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel & 0xff_fcfffc), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel & 0xff_fcfcff), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);

        BufferedImage drawnImg = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
        Graphics2D graphics = drawnImg.createGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();
        assertEquals(rec2020Space, drawnImg.getColorModel().getColorSpace());
        assertImageEquals(img, drawnImg);
    }

    // FAIL: This test passes, but the displayed image is incorrect!
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    public void shouldGetExpectedRgbForCustomRec2020ImageUsingColorConvert(int bitDepth) throws InterruptedException {

        // Draw an RGB color in sRGB space
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage src = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);
        DirectColorModel scm = (DirectColorModel) src.getColorModel();

        // Convert the image to REC2020
        ColorSpace rec2020Space = RecBT2020ColorSpace.getInstance().toColorSpace();
        DirectColorModel cm = new DirectColorModel(rec2020Space, scm.getPixelSize(), scm.getRedMask(), scm.getGreenMask(), scm.getBlueMask(), scm.getAlphaMask(),
                scm.isAlphaPremultiplied(), scm.getTransferType());
        BufferedImage img = toImageWithColorModel_usingColorConvertOp(src, cm);
        assertEquals(rec2020Space, img.getColorModel().getColorSpace());

        showImage("sRGB cube in Custom REC2020 space CONVERT, " + img.getColorModel().getComponentSize(0) + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel & 0xff_fffcfc), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel & 0xff_fcfffc), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel & 0xff_fcfcff), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);

        BufferedImage drawnImg = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
        Graphics2D graphics = drawnImg.createGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();
        assertEquals(rec2020Space, drawnImg.getColorModel().getColorSpace());
        assertImageEquals(img, drawnImg);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    public void shouldGetExpectedRgbForRec2020ImageUsingDraw(int bitDepth) throws InterruptedException {

        // Draw an RGB color in sRGB space
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage src = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);
        DirectColorModel scm = (DirectColorModel) src.getColorModel();

        // Convert the image to REC2020
        ICC_ColorSpace rec2020Space = new ICC_ColorSpace(TestImageFactory.createRec2020Profile());
        DirectColorModel cm = new DirectColorModel(rec2020Space, scm.getPixelSize(), scm.getRedMask(), scm.getGreenMask(), scm.getBlueMask(), scm.getAlphaMask(),
                scm.isAlphaPremultiplied(), scm.getTransferType());
        BufferedImage img = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
        Graphics2D g = img.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        assertEquals(rec2020Space, img.getColorModel().getColorSpace());

        showImage("sRGB cube in REC2020 space DRAW, " + img.getColorModel().getComponentSize(0) + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel & 0xff_fffcfc), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel & 0xff_fcfffc), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel & 0xff_fcfcff), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    public void shouldGetExpectedRgbForCustomRec2020ImageUsingDraw(int bitDepth) throws InterruptedException {

        // Draw an RGB color in sRGB space
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage src = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);
        DirectColorModel scm = (DirectColorModel) src.getColorModel();

        // Convert the image to REC2020
        ColorSpace rec2020Space = RecBT2020ColorSpace.getInstance().toColorSpace();
        DirectColorModel cm = new DirectColorModel(rec2020Space, scm.getPixelSize(), scm.getRedMask(), scm.getGreenMask(), scm.getBlueMask(), scm.getAlphaMask(),
                scm.isAlphaPremultiplied(), scm.getTransferType());
        BufferedImage img = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
        Graphics2D g = img.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        assertEquals(rec2020Space, img.getColorModel().getColorSpace());

        showImage("sRGB cube in Custom REC2020 space DRAW, " + img.getColorModel().getComponentSize(0) + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel & 0xff_fffcfc), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel & 0xff_fcfffc), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel & 0xff_fcfcff), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);
    }

    @ParameterizedTest
    @ValueSource(ints = {/*1, 2, 3, 4, 5, 6,*/ 7, 8/* 9, 10*/})
    public void shouldGetExpectedRgbForRec2020ImagConvertedBackToSrgb(int bitDepth) throws InterruptedException {

        // Draw an RGB color in sRGB space
        ColorSpace srgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BufferedImage src = TestImageFactory.createRgbCubeFaces(srgbSpace, bitDepth);

        // Convert the image to REC2020
        DirectColorModel scm = (DirectColorModel) src.getColorModel();

        // Convert the image to REC2020
        ICC_ColorSpace rec2020Space = new ICC_ColorSpace(TestImageFactory.createRec2020Profile());
        DirectColorModel cm = new DirectColorModel(rec2020Space, scm.getPixelSize(), scm.getRedMask(), scm.getGreenMask(), scm.getBlueMask(), scm.getAlphaMask(),
                scm.isAlphaPremultiplied(), scm.getTransferType());
        BufferedImage intermediate = toImageWithColorModel_usingColorConvertOp(src, cm);

        // Convert the image back to sRGB
        BufferedImage img = toImageWithColorModel_usingColorConvertOp(intermediate, src.getColorModel());

        showImage("sRGB cube in sRGB space with intermediate REC2020, " + img.getColorModel().getComponentSize(0) + " bit", img);

        int size = 1 << bitDepth;
        int bluePixel = img.getRGB(0, 0);
        int greenPixel = img.getRGB(size, 0);
        int redPixel = img.getRGB(size * 2, 0);
        assertEquals(Integer.toHexString(0xff_ff0000), Integer.toHexString(redPixel & 0xff_fffcfc), "red");
        assertEquals(Integer.toHexString(0xff_00ff00), Integer.toHexString(greenPixel & 0xff_fcfffc), "green");
        assertEquals(Integer.toHexString(0xff_0000ff), Integer.toHexString(bluePixel & 0xff_fcfcff), "blue");

        System.out.println("BitDepth: " + img.getColorModel().getComponentSize(0) + " img: " + img);
    }

    private static void assertImageEquals(BufferedImage expected, BufferedImage actual) {
        for (int b = 0; b < expected.getRaster().getNumBands(); b++) {
            int[] exp = expected.getData().getSamples(0, 0, expected.getWidth(), expected.getHeight(), 0, (int[]) null);
            int[] act = actual.getData().getSamples(0, 0, expected.getWidth(), expected.getHeight(), 0, (int[]) null);
            int mismatch = Arrays.mismatch(exp, 0, exp.length, act, 0, exp.length);
            assertEquals(-1, mismatch, "bank=" + b);
            //if (mismatch != -1) {
            //    IO.println("mismatch: " + mismatch + " in bank=" + b);
            //}
        }
    }

    private static void showImage(String title, BufferedImage img) {
        //showImageFX(title, img);
        showImageAWT(title, img);
    }

    private static void showImageFX(String title, BufferedImage img) {

        CompletableFuture<WritableImage> imageFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                WritableImage fxImage = SwingFXUtils.toFXImage(img, null);
                imageFuture.complete(fxImage);
            } catch (Throwable t) {
                imageFuture.completeExceptionally(t);
                t.printStackTrace();
            }
        });
        Platform.runLater(() -> {
            try {
                ImageView imageView = new ImageView(imageFuture.get());
                Stage stage = new Stage();
                imageView.setSmooth(false);
                imageView.setFitWidth(256 * 3);
                imageView.setFitHeight(256 * 2);
                BorderPane pane = new BorderPane(imageView);
                stage.setScene(new Scene(pane));
                stage.setWidth(256 * 3);
                stage.setHeight(256 * 2);
                stage.setTitle(title);
                stage.show();

            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private static void showImageAWT(String title, BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            Icon icon;

            icon = new Icon() {

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.drawImage(img, x, y, 256 * 3, 256 * 2, null);
                }

                @Override
                public int getIconWidth() {
                    return 256 * 3;
                }

                @Override
                public int getIconHeight() {
                    return 256 * 2;
                }
            };

            var label = new JLabel(icon);
            var panel = new JPanel(new BorderLayout());
            panel.add(label, BorderLayout.CENTER);
            frame.setContentPane(panel);
            frame.pack();
            frame.setTitle(title);
            frame.setVisible(true);
        });
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
        }
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(120));
        ;
    }
}
