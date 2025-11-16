/*
 * @(#)IndexColorModelFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.codec;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

public class IndexColorModelFactory {
    /**
     * Creates an ordered palette with cbrt(numColors) RGB colors,
     * and the remaining colors filled with a grayscale.
     *
     * @param size the palette size
     * @return a new instance
     */
    public IndexColorModel createOrderedPalette(int size) {
        size = Math.max(2, size);
        int[] cmap = new int[size];

        // Compute a color cube
        int cdim = (int) Math.cbrt(size);
        if (cdim > 1) {
            for (int r = 0; r < cdim; r++) {
                for (int g = 0; g < cdim; g++) {
                    for (int b = 0; b < cdim; b++) {
                        cmap[r * cdim * cdim + g * cdim + b] =
                                ((255 * r / (cdim - 1)) << 16) | ((255 * g / (cdim - 1)) << 8) | ((255 * b / (cdim - 1)));
                    }
                }
            }
        }

        // Compute a grayscale
        int gdim = size - cdim * cdim * cdim + 2;
        int index = cdim * cdim * cdim;
        if (gdim > 1) {
            for (int gr = 1; gr < gdim - 1; gr++) {
                int gray = 255 * gr / (gdim - 1);
                cmap[index++] = (gray << 16) | (gray << 8) | gray;
            }
        }

        int bits = 32 - Integer.numberOfLeadingZeros(size - 1);
        return new IndexColorModel(bits, size, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
    }
}
