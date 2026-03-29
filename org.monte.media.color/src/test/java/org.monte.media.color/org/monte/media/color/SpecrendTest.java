/*
 * @(#)SpecrendTest.java
 *
 * Copyright (c) 2011 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and
 * contributors of the JHotDraw project ("the copyright holders").
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * the copyright holders. For details see accompanying license terms.
 */
package org.monte.media.color;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * {@code SpecrendTest}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-25 Created.
 */
public class SpecrendTest implements Runnable {

    @Override
    public void run() {
        int w = 256;
        int h = 40;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        double[] xyz = new double[3];
        double[] rgb = new double[3];
        float kMin = 1000;
        float kMax = 10000;
        for (int x = 0; x < w; x++) {
            double k = x * (kMax - kMin) / (w - 1) + kMin;
            Specrend.bb_to_xyz(k, xyz);
            Specrend.xyz_to_rgb(Specrend.SMPTEsystem, xyz, rgb);
            boolean isApprox = Specrend.constrain_rgb(rgb);
            Specrend.norm_rgb(rgb);
            int rgbp = (((int) (rgb[0] * 255) & 0xff) << 16)//
                    | (((int) (rgb[1] * 255) & 0xff) << 8)//
                    | (((int) (rgb[2] * 255) & 0xff) << 0);
            // System.out.println(k + " rgb:" + Arrays.toString(rgb) + " " + Integer.toHexString(rgbp));
            int xy = x;
            for (int y = 0; y < h; y++) {
                img.setRGB(x, y, rgbp);
                xy += h;
            }
        }

        JFrame f = new JFrame("Black body spectrum");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel l = new JLabel(new ImageIcon(img));
        f.add(l);
        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new SpecrendTest());
    }
}
