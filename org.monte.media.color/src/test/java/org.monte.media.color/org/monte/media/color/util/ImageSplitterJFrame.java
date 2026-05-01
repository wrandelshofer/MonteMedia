/*
 * @(#)ImageSplitterFrame.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.util;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;

public class ImageSplitterJFrame {
    private JFrame frame;
    private ImageSplitterJPanel panel;
    private BufferedImage img1, img2;

    public ImageSplitterJFrame() {
    }

    public BufferedImage getImg1() {
        return img1;
    }

    public void setImg1(BufferedImage img1) {
        this.img1 = img1;
        updateView();
    }

    public BufferedImage getImg2() {
        return img2;
    }

    public void setImg2(BufferedImage img2) {
        this.img2 = img2;
        updateView();
    }

    private void updateView() {
        SwingUtilities.invokeLater(() -> {
            if (panel != null) {
                panel.setImg1(img1);
                panel.setImg2(img2);
            }
        });
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            if (frame == null) {
                frame = new JFrame();
                panel = new ImageSplitterJPanel();
                frame.setContentPane(panel);
            }
            panel.setImg1(img1);
            panel.setImg2(img2);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
