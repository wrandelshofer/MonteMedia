/* @(#)CodecTesterMain.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import ru.sbtqa.monte.media.VideoFormatKeys;
import ru.sbtqa.monte.media.avi.AVIWriter;
import ru.sbtqa.monte.media.color.Colors;

/**
 * Renders the following test animation sequence:
 * <p>
 * An all blue image. Each frame draws a new red pixel and overwrites
 * the previously drawn red pixel with white. The pixel is drawn on
 * the corners of a square.
 * <p>
 * The animation sequence is written into a movie file.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-03-14 Created.
 */
public class CodecTesterMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        int w = 400, h = 400;

        // Set up movie writer
        AVIWriter out = new AVIWriter(new File("Test RLE blue.avi"));
        out.addVideoTrack(VideoFormatKeys.ENCODING_AVI_DIB, 1, 25, w, h, 8, 25);
        out.setPalette(0, Colors.createMacColors());

        // Set up image
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
        Graphics2D g = img.createGraphics();
        g.setBackground(Color.BLUE);
        g.clearRect(0, 0, w, h);


        out.write(0, img, 1);

        try {
            Point[] p = {new Point(10, 10), new Point(390, 10), new Point(390, 390), new Point(10, 390)};
            for (int i = 0; i <= p.length; i++) {
                if (i > 0) {
                    g.setColor(Color.BLUE);
                    g.fillRect(p[i - 1].x, p[i - 1].y, 1, 1);
                }
                if (i < p.length) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(p[i].x, p[i].y, 1, 1);
                }
                out.write(0, img, 1);
            }

        } finally {
            out.close();
        }

        g.dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
