/*
 * @(#)BlueNoiseDither.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import static java.lang.Math.abs;

/// 64x64 blue noise dither matrix.
///
/// References:
///
/// Bart Wronski (2020). “Optimizing” blue noise dithering – backpropagation through Fourier transform and sorting.
/// : [bartwronski.com](https://bartwronski.com/2020/04/26/optimizing-blue-noise-dithering-backpropagation-through-fourier-transform-and-sorting/)
///
public class BlueNoiseDither implements Dither {
    private final static float[][][] DATA_ALL = {BlueNoiseData256.DATA0, BlueNoiseData256.DATA1, BlueNoiseData256.DATA2, BlueNoiseData256.DATA3};
    private final float[][] data;
    private final int mask;

    /// Creates a new [BayerDither].
    ///
    /// @param spread the multiplication factor applied to the matrix values. With spread=1 the values are in the range [-1,+1].
    public BlueNoiseDither(float spread) {
        this(0, spread);
    }

    /// Creates a new [BayerDither].
    ///
    /// @param channel The color channel in the range [0,3] (for RGB+alpha, of for Lab+alpha)
    /// @param spread  the multiplication factor applied to the matrix values. With spread=1 the values are in the range [-1,+1].
    public BlueNoiseDither(int channel, float spread) {
        float[][] DATA = DATA_ALL[abs(channel) % 4];
        if (spread == 1) this.data = DATA;
        else {
            this.data = new float[DATA.length][DATA[0].length];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    data[i][j] = DATA[i][j] * spread;
                }
            }
        }
        mask = data.length - 1;
    }

    @Override
    public float get(int x, int y) {
        return data[y & mask][x & mask];
    }

    public float[][] getData() {
        var d = new float[data.length][0];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].clone();
        }
        return d;
    }
}
