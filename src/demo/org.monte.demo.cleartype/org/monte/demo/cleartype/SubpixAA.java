/* @(#)SubpixAA.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.demo.cleartype;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;

/**
 * Scales an image down and applies Subpixel antialiasing to it.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-12-11 Created.
 */
public class SubpixAA {

    public final static Object HBGR;
    public final static Object VBGR;
    public final static Object HRGB;
    public final static Object VRGB;

    static {
        Object hbgr, vbgr, hrgb, vrgb;
        try {
            vbgr = RenderingHints.class.getField("VALUE_TEXT_ANTIALIAS_LCD_VBGR").get(null);
            vrgb = RenderingHints.class.getField("VALUE_TEXT_ANTIALIAS_LCD_VRGB").get(null);
            hbgr = RenderingHints.class.getField("VALUE_TEXT_ANTIALIAS_LCD_HBGR").get(null);
            hrgb = RenderingHints.class.getField("VALUE_TEXT_ANTIALIAS_LCD_HRGB").get(null);
        } catch (Exception ex) {
            hrgb = "HRGB";
            hbgr = "HBGR";
            vrgb = "VRGB";
            vbgr = "VBGR";
        }
        HBGR = hbgr;
        HRGB = hrgb;
        VBGR = vrgb;
        VRGB = vbgr;
    }
    /** Source image buffer. */
    private BufferedImage sBuf;
    /** Destination image buffer. */
    private BufferedImage dBuf;

    /*
    private static void setup(Graphics gr) {
    Graphics2D g = (Graphics2D) gr;
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static void setup2(Graphics gr) {
    Graphics2D g = (Graphics2D) gr;
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    public static void main(String[] args) {
    System.setProperty("apple.awt.graphics.UseQuartz", "false");
    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    Map<RenderingHints.Key, Object> map = (Map<RenderingHints.Key, Object>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
    System.out.println(map);

    SwingUtilities.invokeLater(new Runnable() {

    public void run() {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(430, 340);
    f.setLocation(0, 180);
    f.setTitle("SubpixAA");
    JPanel p = new JPanel() {
    
    public void update(Graphics g) {
    paint(g);
    }

    public void paint(Graphics gr) {
    Graphics2D g = (Graphics2D) gr;
    setup(g);
    Font f = new Font("Times New Roman", Font.PLAIN, 18);

    g.setFont(f);
    g.setColor(Color.white);
    g.fillRect(0, 0, size().width, size().height);
    g.setColor(Color.black);
    int y = 5;
    g.drawString("Sample Text String", 5, y + getFontMetrics(f).getAscent());
    y += getFontMetrics(f).getHeight();
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight(), RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight(), RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight() * 2, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight() * 2, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
    drawClearType(g, "Sample Text String", 5, y += getFontMetrics(f).getHeight() * 3, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
    }

    public void drawClearType(Graphics g, String s, int x, int y, Object method) {
    Font f = g.getFont();
    FontMetrics fm = getFontMetrics(f);
    int sw = fm.stringWidth(s);
    int sh = fm.getHeight();
    Dimension dim = getSourceDimension(sw, sh, method);
    int width = dim.width;
    int height = dim.height;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D grph = img.createGraphics();
    setup2(grph);
    grph.setColor(Color.white);
    grph.fillRect(0, 0, width, height);
    grph.setColor(Color.black);
    grph.setFont(f.deriveFont(AffineTransform.getScaleInstance(dim.width / sw, dim.height / sh)));
    grph.drawString(s, 0, grph.getFontMetrics().getMaxAscent());
    grph.dispose();
    SubpixAA aa = new SubpixAA();
    aa.drawAA(g, img, x, y - sh, sw, sh, method, this);
    g.drawImage(img, x, y, this);
    }
    };
    f.setContentPane(p);
    f.setVisible(true);
    }
    });


    }*/
    /** Renders an image with subpixel antialiasing.
     *
     * This method uses caching to improve the performance of subsequent calls.
     *
     * The image must be larger than the destination width and height.
     */
    public void drawAA(Graphics gr, BufferedImage img, int x, int y, int width, int height, ImageObserver observer) {
        Graphics2D g = (Graphics2D) gr;
        drawAA(gr, img, x, y, width, height, g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING), observer);
    }

    /** Renders an image with subpixel antialiasing.
     *
     * This method uses caching to improve the performance of subsequent calls.
     *
     * The image must be larger than the destination width and height.
     */
    public void drawAA(Graphics gr, BufferedImage img, int x, int y, int width, int height, Object method, ImageObserver observer) {
        // If the image dimension matches width and height, draw it as is.
        if (img.getWidth() == width && img.getHeight() == height) {
            gr.drawImage(img, x, y, observer);
            return;
        }

        Graphics2D g = (Graphics2D) gr;
        // Rescale the image if it does not have the right size for the
        // subpixel antialiasing method.
        Dimension sdim = getSourceDimension(width, height, method);
        if (img.getWidth() != sdim.width || img.getHeight() != sdim.height) {
            if (sBuf == null || sBuf.getWidth() != sdim.width || sBuf.getHeight() != sdim.height) {
                sBuf = new BufferedImage(sdim.width, sdim.height, BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D sg = sBuf.createGraphics();
            setupRendering(sg);
            sg.drawImage(img, 0, 0, sdim.width, sdim.height, null);
            //System.out.println("rescale " + img.getWidth() + "," + img.getHeight() + " to " + sBuf.getWidth() + "," + sBuf.getHeight());
            sg.dispose();
            img = sBuf;
        }
        if (dBuf == null || dBuf.getWidth() != width || dBuf.getHeight() != height) {
            dBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        aa(img, dBuf, method);

        g.drawImage(dBuf, x, y, observer);
    }

    /** Returns the dimensions needed of the source image for the desired
     * destination image size.
     *
     * @param width The desired width of the destination image.
     * @param height The desired height of the destination image.
     * @param method The Antialiasing method to be used must be one of
     * {@code RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB}, {@code ..._HBGR},
     */
    public static Dimension getSourceDimension(int width, int height, Object method) {
        Dimension dim;
        if (method == HRGB ||//
                method == HBGR) {
            dim = new Dimension(width * 3, height);
        } else if (method == VRGB ||//
                method == VBGR) {
            dim = new Dimension(width, height * 3);
        } else {
            dim = new Dimension(width, height);
        }
        return dim;
    }

    /** Scales down an image using the specified antialiasing method.
     * <p>
     * For methods {@code HBGR} and {@code HRGB}, the image width is scaled down
     * by factor 3.
     * For methods {@code VBGR} and {@code VRGB}, the image height is scaled down
     * by factor 3.
     *
     * @param src The source image.
     * @param dst The destination image.
     * @param m The method.
     */
    public static void aa(BufferedImage src, BufferedImage dst, Object m) {
        if (m == HRGB) {
            aaHRGB(src, dst);
        } else if (m == HBGR) {
            aaHBGR(src, dst);
        } else if (m == VRGB) {
            aaVRGB(src, dst);
        } else if (m == VBGR) {
            aaVBGR(src, dst);
        } else {
            Graphics2D g = dst.createGraphics();
            setupRendering(g);
            g.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), null);
            g.dispose();
        }
    }

    /**
     * Set up the graphics transform to match the clip region
     * to the image size.
     */
    private static void setupRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        //getToolkit().
    }

    /** Scales down an image using HRGB antia-aliasing.
     * <p>
     * Source and destination image must be of type BufferedImage.TYPE_RGB
     * and have a data buffer of type DataBufferInt.
     * <p>
     * The height of source and destination image must be the same.
     * The width of the source image must be 3 times the width of the
     * destination image.
     * <p>
     * Intensity weights:
     * <pre>
     *
     *            1
     *            |
     *       +----+----+
     *       |    |    |
     *      1/3  1/3  1/3
     *            |
     *  +----+----+----+----+
     *  |    |    |    |    |
     * 1/9  2/9  3/9  2/9  1/9
     * </pre>
     *
     * @param src The source image.
     * @param dst The destination image.
     */
    public static void aaHRGB(BufferedImage src, BufferedImage dst) {
        WritableRaster sras = src.getRaster();
        WritableRaster dras = dst.getRaster();
        int[] s = ((DataBufferInt) sras.getDataBuffer()).getData();
        int[] d = ((DataBufferInt) dras.getDataBuffer()).getData();
        int sw = src.getWidth();
        int sh = src.getHeight();
        int dw = dst.getWidth();
        int dh = dst.getHeight();

        // weights:left-left,left,center,right,right-right,total
        //final int wll = 0, wl = 0, wc = 1, wr = 0, wrr = 0, tw = wll + wl + wc + wr + wrr;
        //final int wll = 0, wl = 1, wc = 1, wr = 1, wrr = 0, tw = wll+wl+wc+wr+wrr;
        final int wll = 1, wl = 2, wc = 3, wr = 2, wrr = 1, tw = wll + wl + wc + wr + wrr;

        for (int y = 0; y < dh; y++) {
            int sxy = y * sw;
            int dxy = y * dw;
            int dxyeol = dxy + dw - 1;


            // Process the first pixel of the line.
            d[dxy] =//
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * (wll + wl + wc) +//
                    (s[sxy + 1] & 0xff0000) * wr +//
                    (s[sxy + 2] & 0xff0000) * wrr//
                    ) / tw & 0xff0000) |
                    //
                    // Green component:
                    ((//
                    (s[sxy + 0] & 0x00ff00) * (wll + wl) + //
                    (s[sxy + 1] & 0x00ff00) * wc + //
                    (s[sxy + 2] & 0x00ff00) * wr + //
                    (s[sxy + 3] & 0x00ff00) * wrr//
                    ) / tw & 0x00ff00) |
                    //
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * wll + //
                    (s[sxy + 1] & 0x0000ff) * wl + //
                    (s[sxy + 2] & 0x0000ff) * wc + //
                    (s[sxy + 3] & 0x0000ff) * wr +//
                    (s[sxy + 4] & 0x0000ff) * wrr//
                    ) / tw);
            sxy += 3;
            dxy++;

            // Process the pixels in the middle of the line.
            for (; dxy < dxyeol; sxy += 3, dxy++) {
                d[dxy] =//
                        // Red component:
                        ((//
                        (s[sxy - 2] & 0xff0000) * wll + //
                        (s[sxy - 1] & 0xff0000) * wl + //
                        (s[sxy + 0] & 0xff0000) * wc + //
                        (s[sxy + 1] & 0xff0000) * wr +//
                        (s[sxy + 2] & 0xff0000) * wrr//
                        ) / tw & 0xff0000) |
                        //
                        // Green component:
                        ((//
                        (s[sxy - 1] & 0x00ff00) * wll +//
                        (s[sxy - 0] & 0x00ff00) * wl + //
                        (s[sxy + 1] & 0x00ff00) * wc + //
                        (s[sxy + 2] & 0x00ff00) * wr + //
                        (s[sxy + 3] & 0x00ff00) * wrr//
                        ) / tw & 0x00ff00) |
                        //
                        // Blue component:
                        ((//
                        (s[sxy + 0] & 0x0000ff) * wll + //
                        (s[sxy + 1] & 0x0000ff) * wl + //
                        (s[sxy + 2] & 0x0000ff) * wc + //
                        (s[sxy + 3] & 0x0000ff) * wr +//
                        (s[sxy + 4] & 0x0000ff) * wrr//
                        ) / tw);
                /*
                if (x == 2) {
                System.out.println("y:" + y + " sxy:" + sxy + " :" + Integer.toHexString(s[sxy]).substring(2) + " " + Integer.toHexString(s[sxy + 1]).substring(2) + " " + Integer.toHexString(s[sxy + 2]).substring(2) + ":" + Integer.toHexString(0xff00000 | d[dxy]).substring(2));
                }*/
            }
            // Process the last pixel on the line
            d[dxy] =//
                    // Red component:
                    ((//
                    (s[sxy - 2] & 0xff0000) * wll + //
                    (s[sxy - 1] & 0xff0000) * wl + //
                    (s[sxy + 0] & 0xff0000) * wc + //
                    (s[sxy + 1] & 0xff0000) * wr +//
                    (s[sxy + 2] & 0xff0000) * wrr//
                    ) / tw & 0xff0000) |
                    //
                    // Green component:
                    ((//
                    (s[sxy - 1] & 0x00ff00) * wll +//
                    (s[sxy + 0] & 0x00ff00) * wl + //
                    (s[sxy + 1] & 0x00ff00) * wc + //
                    (s[sxy + 2] & 0x00ff00) * (wr + wrr) //
                    ) / tw & 0x00ff00) |
                    //
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * wll + //
                    (s[sxy + 1] & 0x0000ff) * wl + //
                    (s[sxy + 2] & 0x0000ff) * (wc + wr + wrr) //
                    ) / tw);
        }
    }

    /** Scales down an image using HBGR antia-aliasing.
     * <p>
     * Source and destination image must be of type BufferedImage.TYPE_RGB
     * and have a data buffer of type DataBufferInt.
     * <p>
     * The height of source and destination image must be the same.
     * The width of the source image must be 3 times the width of the
     * destination image.
     * <p>
     * Intensity weights:
     * <pre>
     *
     *            1
     *            |
     *       +----+----+
     *       |    |    |
     *      1/3  1/3  1/3
     *            |
     *  +----+----+----+----+
     *  |    |    |    |    |
     * 1/9  2/9  3/9  2/9  1/9
     * </pre>
     *
     * @param src The source image.
     * @param dst The destination image.
     */
    public static void aaHBGR(BufferedImage src, BufferedImage dst) {
        WritableRaster sras = src.getRaster();
        WritableRaster dras = dst.getRaster();
        int[] s = ((DataBufferInt) sras.getDataBuffer()).getData();
        int[] d = ((DataBufferInt) dras.getDataBuffer()).getData();
        int sw = src.getWidth();
        int sh = src.getHeight();
        int dw = dst.getWidth();
        int dh = dst.getHeight();

        // weights:left-left,left,center,right,right-right,total
        //final int wll = 0, wl = 0, wc = 1, wr = 0, wrr = 0, tw = wll + wl + wc + wr + wrr;
        final int wll = 1, wl = 2, wc = 3, wr = 2, wrr = 1, tw = wll + wl + wc + wr + wrr;

        for (int y = 0; y < dh; y++) {
            int sxy = y * sw;
            int dxy = y * dw;
            int dxyeol = dxy + dw - 1;


            // Process the first pixel of the line.
            d[dxy] =//
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * (wll + wl + wc) +//
                    (s[sxy + 1] & 0x0000ff) * wr +//
                    (s[sxy + 2] & 0x0000ff) * wrr//
                    ) / tw) |
                    //
                    // Green component:
                    ((//
                    (s[sxy + 0] & 0x00ff00) * (wll + wl) + //
                    (s[sxy + 1] & 0x00ff00) * wc + //
                    (s[sxy + 2] & 0x00ff00) * wr + //
                    (s[sxy + 3] & 0x00ff00) * wrr//
                    ) / tw & 0x00ff00) |
                    //
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * wll + //
                    (s[sxy + 1] & 0xff0000) * wl + //
                    (s[sxy + 2] & 0xff0000) * wc + //
                    (s[sxy + 3] & 0xff0000) * wr +//
                    (s[sxy + 4] & 0xff0000) * wrr//
                    ) / tw & 0xff0000);
            sxy += 3;
            dxy++;

            // Process the pixels in the middle of the line.
            for (; dxy < dxyeol; sxy += 3, dxy++) {
                d[dxy] =//
                        // Blue component:
                        ((//
                        (s[sxy - 2] & 0x0000ff) * wll + //
                        (s[sxy - 1] & 0x0000ff) * wl + //
                        (s[sxy + 0] & 0x0000ff) * wc + //
                        (s[sxy + 1] & 0x0000ff) * wr +//
                        (s[sxy + 2] & 0x0000ff) * wrr//
                        ) / tw) |
                        //
                        // Green component:
                        ((//
                        (s[sxy - 1] & 0x00ff00) * wll +//
                        (s[sxy + 0] & 0x00ff00) * wl + //
                        (s[sxy + 1] & 0x00ff00) * wc + //
                        (s[sxy + 2] & 0x00ff00) * wr + //
                        (s[sxy + 3] & 0x00ff00) * wrr//
                        ) / tw & 0x00ff00) |
                        //
                        // Red component:
                        ((//
                        (s[sxy + 0] & 0xff0000) * wll + //
                        (s[sxy + 1] & 0xff0000) * wl + //
                        (s[sxy + 2] & 0xff0000) * wc + //
                        (s[sxy + 3] & 0xff0000) * wr +//
                        (s[sxy + 4] & 0xff0000) * wrr//
                        ) / tw & 0xff0000);
                /*
                if (x == 2) {
                System.out.println("y:" + y + " sxy:" + sxy + " :" + Integer.toHexString(s[sxy]).substring(2) + " " + Integer.toHexString(s[sxy + 1]).substring(2) + " " + Integer.toHexString(s[sxy + 2]).substring(2) + ":" + Integer.toHexString(0xff00000 | d[dxy]).substring(2));
                }*/
            }
            // Process the last pixel on the line
            d[dxy] =//
                    // Blue component:
                    ((//
                    (s[sxy - 2] & 0x0000ff) * wll + //
                    (s[sxy - 1] & 0x0000ff) * wl + //
                    (s[sxy + 0] & 0x0000ff) * wc + //
                    (s[sxy + 1] & 0x0000ff) * wr +//
                    (s[sxy + 2] & 0x0000ff) * wrr//
                    ) / tw) |
                    //
                    // Green component:
                    ((//
                    (s[sxy - 1] & 0x00ff00) * wll +//
                    (s[sxy + 0] & 0x00ff00) * wl + //
                    (s[sxy + 1] & 0x00ff00) * wc + //
                    (s[sxy + 2] & 0x00ff00) * (wr + wrr) //
                    ) / tw & 0x00ff00) |
                    //
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * wll + //
                    (s[sxy + 1] & 0xff0000) * wl + //
                    (s[sxy + 2] & 0xff0000) * (wc + wr + wrr) //
                    ) / tw & 0xff0000);
        }
    }

    /** Scales down an image using VRGB antia-aliasing.
     * <p>
     * Source and destination image must be of type BufferedImage.TYPE_RGB
     * and have a data buffer of type DataBufferInt.
     * <p>
     * The width of source and destination image must be the same.
     * The height of the source image must be 3 times the height of the
     * destination image.
     * <p>
     * Intensity weights:
     * <pre>
     *
     *            1
     *            |
     *       +----+----+
     *       |    |    |
     *      1/3  1/3  1/3
     *            |
     *  +----+----+----+----+
     *  |    |    |    |    |
     * 1/9  2/9  3/9  2/9  1/9
     * </pre>
     *
     * @param src The source image.
     * @param dst The destination image.
     */
    public static void aaVRGB(BufferedImage src, BufferedImage dst) {
        WritableRaster sras = src.getRaster();
        WritableRaster dras = dst.getRaster();
        int[] s = ((DataBufferInt) sras.getDataBuffer()).getData();
        int[] d = ((DataBufferInt) dras.getDataBuffer()).getData();
        int sw = src.getWidth();
        int sh = src.getHeight();
        int dw = dst.getWidth();
        int dh = dst.getHeight();

        // weights:left-left,left,center,right,right-right,total
        //final int wll = 0, wl = 0, wc = 1, wr = 0, wrr = 0, tw = wll + wl + wc + wr + wrr;
        //final int wll = 0, wl = 1, wc = 1, wr = 1, wrr = 0, tw = wll+wl+wc+wr+wrr;
        final int wll = 1, wl = 2, wc = 3, wr = 2, wrr = 1, tw = wll + wl + wc + wr + wrr;

        for (int x = 0; x < dw; x++) {
            int sxy = x;
            int dxy = x;
            int dxyeol = dxy + dw * (dh - 1);


            // Process the first pixel of the line.
            d[dxy] =//
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * (wll + wl + wc) +//
                    (s[sxy + sw] & 0xff0000) * wr +//
                    (s[sxy + sw * 2] & 0xff0000) * wrr//
                    ) / tw & 0xff0000) |
                    //
                    // Green component:
                    ((//
                    (s[sxy + 0] & 0x00ff00) * (wll + wl) + //
                    (s[sxy + sw] & 0x00ff00) * wc + //
                    (s[sxy + sw * 2] & 0x00ff00) * wr + //
                    (s[sxy + sw * 3] & 0x00ff00) * wrr//
                    ) / tw & 0x00ff00) |
                    //
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * wll + //
                    (s[sxy + sw] & 0x0000ff) * wl + //
                    (s[sxy + sw * 2] & 0x0000ff) * wc + //
                    (s[sxy + sw * 3] & 0x0000ff) * wr +//
                    (s[sxy + sw * 4] & 0x0000ff) * wrr//
                    ) / tw);
            sxy += 3 * sw;
            dxy += dw;

            // Process the pixels in the middle of the line.
            for (; dxy < dxyeol; sxy += 3 * sw, dxy += dw) {
                d[dxy] =//
                        // Red component:
                        ((//
                        (s[sxy - sw * 2] & 0xff0000) * wll + //
                        (s[sxy - sw] & 0xff0000) * wl + //
                        (s[sxy + 0] & 0xff0000) * wc + //
                        (s[sxy + sw] & 0xff0000) * wr +//
                        (s[sxy + sw * 2] & 0xff0000) * wrr//
                        ) / tw & 0xff0000) |
                        //
                        // Green component:
                        ((//
                        (s[sxy - sw] & 0x00ff00) * wll +//
                        (s[sxy - 0] & 0x00ff00) * wl + //
                        (s[sxy + sw] & 0x00ff00) * wc + //
                        (s[sxy + sw * 2] & 0x00ff00) * wr + //
                        (s[sxy + sw * 3] & 0x00ff00) * wrr//
                        ) / tw & 0x00ff00) |
                        //
                        // Blue component:
                        ((//
                        (s[sxy + 0] & 0x0000ff) * wll + //
                        (s[sxy + sw] & 0x0000ff) * wl + //
                        (s[sxy + sw * 2] & 0x0000ff) * wc + //
                        (s[sxy + sw * 3] & 0x0000ff) * wr +//
                        (s[sxy + sw * 4] & 0x0000ff) * wrr//
                        ) / tw);
                /*
                if (dxy <= dw*2) {
                System.out.println("x:" + x + " sxy:" + sxy + " :" + Integer.toHexString(s[sxy]).substring(2) + " " + Integer.toHexString(s[sxy + 1]).substring(2) + " " + Integer.toHexString(s[sxy + 2]).substring(2) + ":" + Integer.toHexString(0xff00000 | d[dxy]).substring(2));
                }*/
            }
            // Process the last pixel on the line
            d[dxy] =//
                    // Red component:
                    ((//
                    (s[sxy - sw * 2] & 0xff0000) * wll + //
                    (s[sxy - sw] & 0xff0000) * wl + //
                    (s[sxy + 0] & 0xff0000) * wc + //
                    (s[sxy + sw] & 0xff0000) * wr +//
                    (s[sxy + sw * 2] & 0xff0000) * wrr//
                    ) / tw & 0xff0000) |
                    //
                    // Green component:
                    ((//
                    (s[sxy - sw] & 0x00ff00) * wll +//
                    (s[sxy + 0] & 0x00ff00) * wl + //
                    (s[sxy + sw] & 0x00ff00) * wc + //
                    (s[sxy + sw * 2] & 0x00ff00) * (wr + wrr) //
                    ) / tw & 0x00ff00) |
                    //
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * wll + //
                    (s[sxy + sw] & 0x0000ff) * wl + //
                    (s[sxy + sw * 2] & 0x0000ff) * (wc + wr + wrr) //
                    ) / tw);
        }
    }

    /** Scales down an image using VBGR antia-aliasing.
     * <p>
     * Source and destination image must be of type BufferedImage.TYPE_RGB
     * and have a data buffer of type DataBufferInt.
     * <p>
     * The width of source and destination image must be the same.
     * The height of the source image must be 3 times the height of the
     * destination image.
     * <p>
     * Intensity weights:
     * <pre>
     *
     *            1
     *            |
     *       +----+----+
     *       |    |    |
     *      1/3  1/3  1/3
     *            |
     *  +----+----+----+----+
     *  |    |    |    |    |
     * 1/9  2/9  3/9  2/9  1/9
     * </pre>
     *
     * @param src The source image.
     * @param dst The destination image.
     */
    public static void aaVBGR(BufferedImage src, BufferedImage dst) {
        WritableRaster sras = src.getRaster();
        WritableRaster dras = dst.getRaster();
        int[] s = ((DataBufferInt) sras.getDataBuffer()).getData();
        int[] d = ((DataBufferInt) dras.getDataBuffer()).getData();
        int sw = src.getWidth();
        int sh = src.getHeight();
        int dw = dst.getWidth();
        int dh = dst.getHeight();

        // weights:left-left,left,center,right,right-right,total
        //final int wll = 0, wl = 0, wc = 1, wr = 0, wrr = 0, tw = wll + wl + wc + wr + wrr;
        //final int wll = 0, wl = 1, wc = 1, wr = 1, wrr = 0, tw = wll+wl+wc+wr+wrr;
        final int wll = 1, wl = 2, wc = 3, wr = 2, wrr = 1, tw = wll + wl + wc + wr + wrr;

        for (int x = 0; x < dw; x++) {
            int sxy = x;
            int dxy = x;
            int dxyeol = dxy + dw * (dh - 1);


            // Process the first pixel of the line.
            d[dxy] =//
                    // Blue component:
                    ((//
                    (s[sxy + 0] & 0x0000ff) * (wll + wl + wc) +//
                    (s[sxy + sw] & 0x0000ff) * wr +//
                    (s[sxy + sw * 2] & 0x0000ff) * wrr//
                    ) / tw) |
                    //
                    // Green component:
                    ((//
                    (s[sxy + 0] & 0x00ff00) * (wll + wl) + //
                    (s[sxy + sw] & 0x00ff00) * wc + //
                    (s[sxy + sw * 2] & 0x00ff00) * wr + //
                    (s[sxy + sw * 3] & 0x00ff00) * wrr//
                    ) / tw & 0x00ff00) |
                    //
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * wll + //
                    (s[sxy + sw] & 0xff0000) * wl + //
                    (s[sxy + sw * 2] & 0xff0000) * wc + //
                    (s[sxy + sw * 3] & 0xff0000) * wr +//
                    (s[sxy + sw * 4] & 0xff0000) * wrr//
                    ) / tw & 0xff0000);
            sxy += 3 * sw;
            dxy += dw;

            // Process the pixels in the middle of the line.
            for (; dxy < dxyeol; sxy += 3 * sw, dxy += dw) {
                d[dxy] =//
                        // Blue component:
                        ((//
                        (s[sxy - sw * 2] & 0x0000ff) * wll + //
                        (s[sxy - sw] & 0x0000ff) * wl + //
                        (s[sxy + 0] & 0x0000ff) * wc + //
                        (s[sxy + sw] & 0x0000ff) * wr +//
                        (s[sxy + sw * 2] & 0x0000ff) * wrr//
                        ) / tw) |
                        //
                        // Green component:
                        ((//
                        (s[sxy - sw] & 0x00ff00) * wll +//
                        (s[sxy - 0] & 0x00ff00) * wl + //
                        (s[sxy + sw] & 0x00ff00) * wc + //
                        (s[sxy + sw * 2] & 0x00ff00) * wr + //
                        (s[sxy + sw * 3] & 0x00ff00) * wrr//
                        ) / tw & 0x00ff00) |
                        //
                        // Red component:
                        ((//
                        (s[sxy + 0] & 0xff0000) * wll + //
                        (s[sxy + sw] & 0xff0000) * wl + //
                        (s[sxy + sw * 2] & 0xff0000) * wc + //
                        (s[sxy + sw * 3] & 0xff0000) * wr +//
                        (s[sxy + sw * 4] & 0xff0000) * wrr//
                        ) / tw & 0xff0000);
                /*
                if (dxy <= dw*2) {
                System.out.println("x:" + x + " sxy:" + sxy + " :" + Integer.toHexString(s[sxy]).substring(2) + " " + Integer.toHexString(s[sxy + 1]).substring(2) + " " + Integer.toHexString(s[sxy + 2]).substring(2) + ":" + Integer.toHexString(0xff00000 | d[dxy]).substring(2));
                }*/
            }
            // Process the last pixel on the line
            d[dxy] =//
                    // Blue component:
                    ((//
                    (s[sxy - sw * 2] & 0x0000ff) * wll + //
                    (s[sxy - sw] & 0x0000ff) * wl + //
                    (s[sxy + 0] & 0x0000ff) * wc + //
                    (s[sxy + sw] & 0x0000ff) * wr +//
                    (s[sxy + sw * 2] & 0x0000ff) * wrr//
                    ) / tw) |
                    //
                    // Green component:
                    ((//
                    (s[sxy - sw] & 0x00ff00) * wll +//
                    (s[sxy + 0] & 0x00ff00) * wl + //
                    (s[sxy + sw] & 0x00ff00) * wc + //
                    (s[sxy + sw * 2] & 0x00ff00) * (wr + wrr) //
                    ) / tw & 0x00ff00) |
                    //
                    // Red component:
                    ((//
                    (s[sxy + 0] & 0xff0000) * wll + //
                    (s[sxy + sw] & 0xff0000) * wl + //
                    (s[sxy + sw * 2] & 0xff0000) * (wc + wr + wrr) //
                    ) / tw & 0xff0000);
        }
    }
}
