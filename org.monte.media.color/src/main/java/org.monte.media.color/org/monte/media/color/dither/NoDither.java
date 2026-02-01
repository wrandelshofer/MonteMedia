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
}
