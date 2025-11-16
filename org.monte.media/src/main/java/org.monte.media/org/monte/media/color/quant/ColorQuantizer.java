/*
 * @(#)ColorQuantizer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.quant;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 * Given a set of images, estimates a common color palette.
 */
public interface ColorQuantizer {
    /**
     * Adds an image for processing.
     *
     * @param image an image
     * @throws IllegalStateException if {@link #computeColorPalette} has already been called.
     */
    void addImage(BufferedImage image);

    /**
     * Creates a color palette.
     *
     * @return color palette
     */
    IndexColorModel computeColorPalette();

    /**
     * Returns the index of the representative color in the palette.
     *
     * @param rgb a color
     * @return the representative color in the palette
     */
    int quant(int rgb);
}
