/*
 * @(#)NoDither.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

public class NoDither implements Dither {
    @Override
    public float get(int x, int y) {
        return 0;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }
}
