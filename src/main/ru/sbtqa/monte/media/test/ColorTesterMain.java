/* @(#)ColorTesterMain.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.test;

import ru.sbtqa.monte.media.VideoFormatKeys;
import ru.sbtqa.monte.media.avi.AVIWriter;
import ru.sbtqa.monte.media.color.Colors;
import ru.sbtqa.monte.media.quicktime.QuickTimeWriter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 * {@code ColorTesterMain}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-03-15 Created.
 */
public class ColorTesterMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
        byte[] data = ((DataBufferByte) img1.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img1, 0, 0, 256, 256, 0, 0, 16, 16, null);
        g.dispose();
        {
            QuickTimeWriter w = new QuickTimeWriter(new File(System.getProperty("user.home") + File.separator + "Movies" + File.separator + "ColorTest.mov"));
            w.addVideoTrack(VideoFormatKeys.ENCODING_QUICKTIME_RAW, VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_RAW, 600, img.getWidth(), img.getHeight(), 8, 0);
            w.write(0, img, 600);
            w.close();
        }
        AVIWriter w = new AVIWriter(new File(System.getProperty("user.home") + File.separator + "Movies" + File.separator + "ColorTest.avi"));
        w.addVideoTrack(VideoFormatKeys.ENCODING_AVI_DIB, 600, 1, img.getWidth(), img.getHeight(), 8, 0);
        w.write(0, img, 600);
        w.close();
    }
}
