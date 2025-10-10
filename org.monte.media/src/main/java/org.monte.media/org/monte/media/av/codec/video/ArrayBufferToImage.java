/*
 * @(#)ArrayBufferToImage.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;

public class ArrayBufferToImage {


    /**
     * Copies a buffered image.
     */
    public BufferedImage copyImage(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public ColorModel getColorModel(Buffer buf) {
        if (buf.header instanceof ColorModel) {
            return (ColorModel) buf.header;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            return image.getColorModel();
        }
        return buf.format.get(PaletteKey);
    }
}
