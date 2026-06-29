/*
 * @(#)ImageSplitterPanel.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.util;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static java.lang.Math.abs;

public class ImageSplitterJPanel extends JPanel {
    private BufferedImage img1;
    private BufferedImage img2;
    private int splitX = 150; // Initial position of the divider

    public ImageSplitterJPanel() {
        this(null, null);
    }

    public BufferedImage getImg1() {
        return img1;
    }

    public void setImg1(BufferedImage img1) {
        this.img1 = img1;
    }

    public BufferedImage getImg2() {
        return img2;
    }

    public void setImg2(BufferedImage img2) {
        this.img2 = img2;
    }

    public ImageSplitterJPanel(BufferedImage img1, BufferedImage img2) {
        this.img1 = img1;
        this.img2 = img2;

        // Optional: Mouse listener to move the bar with the mouse
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                splitX = e.getX();
                repaint();
            }

            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (abs(e.getX() - splitX) < 3) {
                    setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    setCursor(null);
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        if (img1 == null) {
            return new Dimension(320, 240);
        }
        return new Dimension(img1.getWidth(), img1.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw first image (left side) clipped to splitX
        int width = getWidth();
        int height = getHeight();
        if (img1 != null) {
            g2d.drawImage(img1, 0, 0, splitX, height, 0, 0, splitX, height, null);
        }
        // Draw second image (right side) from splitX onwards
        if (img2 != null) {
            g2d.drawImage(img2, splitX, 0, width, height,
                    splitX, 0, width, height, null);
        }

        // Reset clip and draw the divider line
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(splitX, 0, splitX, height);
    }
}