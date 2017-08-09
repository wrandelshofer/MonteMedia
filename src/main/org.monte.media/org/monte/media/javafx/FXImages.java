/* @(#)FXImages
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.javafx;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

/**
 * FXImages.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class FXImages {

    /**
     * Converts an AWT image to Java FX.
     * <p>
     * This method performs better than SwingFXUtils on Java SE 8 if the
     * underlying Raster has a DataBufferInt.
     *
     * @param bimg A buffered image, must not be null.
     * @param wimg A writable image, can be null.
     * @return a JavaFX writable image
     */
    public static WritableImage toFXImage(BufferedImage bimg, WritableImage wimg) {
        final int w = bimg.getWidth();
        final int h = bimg.getHeight();

        if (wimg == null || wimg.getWidth() != w || wimg.getHeight() != h) {
            wimg = new WritableImage(bimg.getWidth(), bimg.getHeight());
        }

        // perform fast conversion if possible
        fast:
        if ((bimg.getSampleModel() instanceof SinglePixelPackedSampleModel)
                && bimg.getRaster().getDataBuffer() instanceof DataBufferInt) {
            int[] p = ((DataBufferInt) bimg.getRaster().getDataBuffer()).getData();
            if (p.length != w * h) {
                break fast; // can't do it the fast way, because we do not know if there is an offset and/or a scanline stride
            }

            switch (bimg.getType()) {
                case BufferedImage.TYPE_INT_RGB:
                    // set Alpha value to 0xff on all pixels (make pixels opaque)
                    int[] o = new int[p.length];
                    for (int i = 0; i < o.length; i++) {
                        o[i] = p[i] | 0xff000000;
                    }
                    wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(),
                            o, 0, bimg.getWidth());
                    return wimg;
                case BufferedImage.TYPE_INT_ARGB:
                    wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(),
                            p, 0, bimg.getWidth());
                    return wimg;
                case BufferedImage.TYPE_INT_ARGB_PRE:
                    wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbPreInstance(),
                            p, 0, bimg.getWidth());
                    return wimg;
            }
        }

        // do it the slow way
        return SwingFXUtils.toFXImage(bimg, wimg);
    }

    /**
     * Prevent instance creation.
     */
    private FXImages() {
    }
}
