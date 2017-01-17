/* @(#)ClearType.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
 /*
 * Source taken from
 * http://blog.monstuff.com/archives/000022.html
 */
package ru.sbtqa.monte.media.image;

import java.awt.*;
import static java.awt.Color.black;
import static java.awt.Color.white;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.geom.AffineTransform.getScaleInstance;
import java.awt.image.*;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import static javax.swing.SwingUtilities.invokeLater;

public class ClearType {

    public static void main(String[] args) {
        invokeLater(
                new Runnable() {
            @Override
            public void run() {

                JFrame f = new JFrame();
                f.setDefaultCloseOperation(EXIT_ON_CLOSE);
                f.setSize(430, 180);
                f.setTitle("ClearType");
                JPanel p = new JPanel() {
                    private final static long serialVersionUID = 1L;

                    @Override
                    public void update(Graphics g) {
                        paint(g);
                    }

                    @Override
                    public void paint(Graphics g) {
                        Font f = new Font("Times New Roman", PLAIN, 18);
                        Graphics2D grph = (Graphics2D) g;
                        grph.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
                        g.setFont(f);
                        g.setColor(white);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(black);
                        g.drawString("Sample Text String", 5, getFontMetrics(f).getHeight());
                        drawClearType(g, "Sample Text String", 5, getFontMetrics(f).getHeight() * 3);
                    }

                    public void drawClearType(Graphics g, String s, int x, int y) {
                        Font f = g.getFont();
                        FontMetrics fm = getFontMetrics(f);
                        int width = fm.stringWidth(s) * 3;
                        int height = fm.getHeight();
                        BufferedImage img = (BufferedImage) createImage(width, height);
                        Graphics2D grph = img.createGraphics();
                        grph.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
                        grph.setColor(white);
                        grph.fillRect(0, 0, width, height);
                        grph.setColor(black);
                        grph.setFont(f.deriveFont(getScaleInstance(3, 1)));
                        grph.drawString(s, 0, height - fm.getMaxDescent());
                        int[] pixelsA = getPixelArray(img);
                        int[] pixels = new int[pixelsA.length];
                        int[] newpixels = new int[(width / 3) * height];
                        for (int i = 0; i < width * height; i++) {
                            newpixels[i / 3] = (0xff << 24);
                            if (i != width * height - 1) {
                                pixels[i + 1] = fuzz(pixels[i + 1], pixelsA[i]);
                            }
                            pixels[i] = fuzz(pixels[i], pixelsA[i]);
                            if (i != 0) {
                                pixels[i - 1] = fuzz(pixels[i - 1], pixelsA[i]);
                            }
                        }
                        for (int i = 0; i < width * height; i++) {
                            switch (i % 3) {
                                case 0:
                                    newpixels[i / 3] |= (pixels[i]) & 0xff0000;
                                    break;
                                case 1:
                                    newpixels[i / 3] |= (pixels[i]) & 0xff00;
                                    break;
                                case 2:
                                    newpixels[i / 3] |= (pixels[i]) & 0xff;
                                    break;
                            }
                        }
                        grph.dispose();
                        g.drawImage(imageFromPixels(newpixels, width / 3, height), x, y - height, width / 3, height, this);
                        g.drawImage(img, x, y, width, height, this);
                    }
                };
                f.setContentPane(p);
                f.setVisible(true);
            }
        });
    }

    /**
     * Adds one third of b to a
     */
    private static int fuzz(int a, int b) {
        int red = ((b >> 16) & 0xff) / 3 + ((a >> 16) & 0xff);
        int green = ((b >> 8) & 0xff) / 3 + ((a >> 8) & 0xff);
        int blue = (b & 0xff) / 3 + (a & 0xff);
        return (0xff << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int[] getPixelArray(Image img) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        int[] pixels = new int[width * height];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
        return pixels;
    }

    /**
     * Renders an image with clear type. Note the width of the image must be
     * divideable by 3.
     *
     * @param g TODO
     * @param observer TODO
     * @param img TODO
     * @param superY TODO
     * @param x TODO
     * @param y TODO
     */
    public static void drawClearType(Graphics g, Image img, int x, int y, int superY, ImageObserver observer) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        int[] pixelsA = getPixelArray(img);
        int[] pixels = new int[pixelsA.length];
        int[] newpixels = new int[(width / 3) * height];
        for (int i = 0; i < width * height; i++) {
            newpixels[i / 3] = (0xff << 24);
            if (i != width * height - 1) {
                pixels[i + 1] = fuzz(pixels[i + 1], pixelsA[i]);
            }
            pixels[i] = fuzz(pixels[i], pixelsA[i]);
            if (i != 0) {
                pixels[i - 1] = fuzz(pixels[i - 1], pixelsA[i]);
            }
        }
        for (int i = 0; i < width * height; i++) {
            switch (i % 3) {
                case 0:
                    newpixels[i / 3] |= (pixels[i]) & 0xff0000;
                    break;
                case 1:
                    newpixels[i / 3] |= (pixels[i]) & 0xff00;
                    break;
                case 2:
                    newpixels[i / 3] |= (pixels[i]) & 0xff;
                    break;
            }
        }
        g.drawImage(imageFromPixels(newpixels, width / 3, height), x, y, width / 3, height / superY, observer);
    }

    private static Image imageFromPixels(int[] pixels, int width, int height) {
        return getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
    }
}
