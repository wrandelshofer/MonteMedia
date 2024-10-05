/*
 * @(#)RgbImageBuilder.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Helper for creating test images.
 */
public class RgbImageBuilder {
    private int width;
    private int height;
    public final static int WHITE = 0xffffff;
    public final static int BLACK = 0x000000;
    public final static int RED = 0xff0000;
    public final static int GREEN = 0x00ff00;
    public final static int BLUE = 0x0000ff;

    public RgbImageBuilder(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public BufferedImage createBlack() {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage createWhite() {
        BufferedImage img = createBlack();
        return fillRectangle(img, 0, 0, img.getWidth(), img.getHeight(), WHITE);
    }

    public BufferedImage fillRectangle(BufferedImage img, int x, int y, int w, int h, int color) {
        int imgW = img.getWidth();
        DataBufferInt buf = (DataBufferInt) img.getRaster().getDataBuffer();
        int[] data = buf.getData();
        for (int i = 0; i < h; i++) {
            int lineIndex = (y + i) * imgW;
            Arrays.fill(data, lineIndex + x, lineIndex + x + w, color);

        }
        return img;
    }
}
