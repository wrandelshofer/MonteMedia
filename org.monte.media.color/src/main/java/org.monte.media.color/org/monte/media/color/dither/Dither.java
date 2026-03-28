/*
 * @(#)Dither.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

public interface Dither {

    public float get(int x, int y);

    int getWidth();

    int getHeight();
}
