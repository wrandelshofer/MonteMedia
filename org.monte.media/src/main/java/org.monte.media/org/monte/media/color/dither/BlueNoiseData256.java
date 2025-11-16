/*
 * @(#)BlueNoiseData256.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import org.monte.media.io.StreamPosTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * 256x256 blue noise dither matrix. Provides 4 data arrays for color models with up to 4 components.
 * <p>
 * References:
 * <dl>
 *     <dt>Bart Wronski (2020). “Optimizing” blue noise dithering – backpropagation through Fourier transform and sorting.</dt>
 *     <dd><a href="https://bartwronski.com/2020/04/26/optimizing-blue-noise-dithering-backpropagation-through-fourier-transform-and-sorting/">bartwronski.com</a></dd>
 * </dl>
 */
class BlueNoiseData256 {
    final static float[][] DATA0 = new float[256][256];
    final static float[][] DATA1 = new float[256][256];
    final static float[][] DATA2 = new float[256][256];
    final static float[][] DATA3 = new float[256][256];

    static {
        loadData(BlueNoiseData256.class.getResourceAsStream("BlueNoiseData0_256.txt"), DATA0);
        loadData(BlueNoiseData256.class.getResourceAsStream("BlueNoiseData1_256.txt"), DATA1);
        loadData(BlueNoiseData256.class.getResourceAsStream("BlueNoiseData2_256.txt"), DATA2);
        loadData(BlueNoiseData256.class.getResourceAsStream("BlueNoiseData3_256.txt"), DATA3);
    }

    private static void loadData(InputStream in, float[][] data) {
        try (InputStreamReader r = new InputStreamReader(in)) {
            var tt = new StreamPosTokenizer(r);
            tt.parseExponents();
            int index = 0;
            while (tt.nextToken() != StreamTokenizer.TT_EOF) {
                if (tt.ttype == StreamTokenizer.TT_NUMBER) {
                    data[index >> 8][index & 255] = Math.clamp((float) tt.nval, -1f, 1f);
                    index++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
