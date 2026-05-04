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

/// 128x128 blue noise dither matrix. Provides 4 data arrays for color models with up to 4 components.
///
/// References:
///
/// Bart Wronski (2020). “Optimizing” blue noise dithering – backpropagation through Fourier transform and sorting.
/// : [bartwronski.com](https://bartwronski.com/2020/04/26/optimizing-blue-noise-dithering-backpropagation-through-fourier-transform-and-sorting/)
///
class BlueNoiseData128 {
    final static float[][] DATA0 = new float[128][128];
    final static float[][] DATA1 = new float[128][128];
    final static float[][] DATA2 = new float[128][128];
    final static float[][] DATA3 = new float[128][128];

    static {
        loadData(BlueNoiseData128.class.getResourceAsStream("BlueNoiseData0_128.hex"), DATA0);
        loadData(BlueNoiseData128.class.getResourceAsStream("BlueNoiseData1_128.hex"), DATA1);
        loadData(BlueNoiseData128.class.getResourceAsStream("BlueNoiseData2_128.hex"), DATA2);
        loadData(BlueNoiseData128.class.getResourceAsStream("BlueNoiseData3_128.hex"), DATA3);
    }

    static void loadData(InputStream in, float[][] data) {
        try (InputStreamReader r = new InputStreamReader(in)) {
            var tt = new StreamPosTokenizer(r);
            tt.resetSyntax();
            tt.slashStarComments(true);
            tt.whitespaceChars(0, ' ');
            tt.wordChars('0', '9');
            tt.wordChars('a', 'z');
            int index = 0;
            while (tt.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tt.ttype) {
                    case StreamTokenizer.TT_WORD -> {
                        float value = Float.intBitsToFloat(Integer.parseUnsignedInt(tt.sval, 16));
                        data[index >> 7][index & 127] = Math.clamp(value, -1f, 1f);
                        index++;
                    }
                    default -> {
                        throw new IOException("Invalid data format at lineno=" + tt.lineno() + " tt.ttype=" + tt.ttype + (tt.ttype > 0 ? "  tt.val='" + (char) tt.ttype + "'" : " tt.sval=" + tt.sval));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
