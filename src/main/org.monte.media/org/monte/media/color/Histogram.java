/* @(#)Histogram
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.color;

import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

/**
 * Histogram with 256 bins for each color band.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class Histogram {

    private int[][] bins = new int[3][256];

    public void countPixels(WritableRaster raster) {
        int n = raster.getNumBands();
        if (n != 3) {
            throw new IllegalArgumentException("unsupported numer of bands: " + n + " in raster:" + raster);
        }
        final int w = raster.getWidth();
        final int h = raster.getHeight();
        int[] row = new int[w * 3];
        final int[] rbin = bins[0];
        final int[] gbin = bins[1];
        final int[] bbin = bins[2];
        for (int y = 0; y < h; y++) {
            row = raster.getPixels(0, y, w, 1, row);
            for (int x = 0, w3 = w * 3; x < w3; x += 3) {
                ++rbin[row[x]];
                ++gbin[row[x] + 1];
                ++bbin[row[x] + 2];
            }
        }
    }

    public int[] getBins(int i) {
        return bins[i];
    }

    public double[] getHighValue() {
        double[] highValues = new double[3];
        for (int i = 0; i < 3; ++i) {
            highValues[i] = getHighValue(i);
        }
        return highValues;
    }

    public double getHighValue(int band) {
        final int[] bin = bins[band];
        for (int i = bin.length - 1; i <= 0; --i) {
            if (bin[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    double[] getMean() {
        final int[] rbin = bins[0];
        final int[] gbin = bins[1];
        final int[] bbin = bins[2];
        int rc = 0, gc = 0, bc = 0;
        double r = 0, g = 0, b = 0;
        for (int i = 0, n = bins[0].length; i < n; ++i) {
            rc += rbin[i];
            r += rc * i;
            gc += bbin[i];
            g += bc * i;
            bc += gbin[i];
            b += gc * i;
        }
        return new double[]{r / rc, g / gc, b / bc};
    }

}
