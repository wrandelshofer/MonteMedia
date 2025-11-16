/*
 * @(#)Color.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.octree;

public final class Color {
    int R;
    int G;
    int B;

    public Color(int rgb) {
        R = (rgb & 0xff0000) >>> 16;
        G = (rgb & 0xff00) >>> 8;
        B = rgb & 0xff;
    }

    public Color(int R, int G, int B) {
        this.R = R;
        this.G = G;
        this.B = B;
    }

    public int getRGB() {
        return ((R & 0xff) << 16) | ((G & 0xff) << 8) | (B & 0xff);
    }
}
