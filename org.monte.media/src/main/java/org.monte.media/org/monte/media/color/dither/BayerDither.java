/*
 * @(#)BayerDither.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

/**
 * Bayer ordered dithering.
 *
 * <p>
 * References:
 * <dl>
 *     <dt>Wikipedia (2025). Ordered dithering.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Ordered_dithering">wikipedia.com</a></dd>
 * </dl>
 */
public class BayerDither implements Dither {
    @SuppressWarnings("PointlessArithmeticExpression")
    private static float[][] DATA_4 = {
            {(float) (0 / 7.5 - 1), (float) (8 / 7.5 - 1), (float) (2 / 7.5 - 1), (float) (10 / 7.5 - 1)},
            {(float) (12 / 7.5 - 1), (float) (4 / 7.5 - 1), (float) (14 / 7.5 - 1), (float) (6 / 7.5 - 1)},
            {(float) (3 / 7.5 - 1), (float) (11 / 7.5 - 1), (float) (1 / 7.5 - 1), (float) (9 / 7.5 - 1)},
            {(float) (15 / 7.5 - 1), (float) (7 / 7.5 - 1), (float) (13 / 7.5 - 1), (float) (5 / 7.5 - 1)},
    };
    @SuppressWarnings("PointlessArithmeticExpression")
    private static float[][] DATA_8 = {
            {(float) (0 / 31.5 - 1), (float) (32 / 31.5 - 1), (float) (8 / 31.5 - 1), (float) (40 / 31.5 - 1), (float) (2 / 31.5 - 1), (float) (34 / 31.5 - 1), (float) (10 / 31.5 - 1), (float) (42 / 31.5 - 1)},
            {(float) (48 / 31.5 - 1), (float) (16 / 31.5 - 1), (float) (56 / 31.5 - 1), (float) (24 / 31.5 - 1), (float) (50 / 31.5 - 1), (float) (18 / 31.5 - 1), (float) (58 / 31.5 - 1), (float) (26 / 31.5 - 1)},
            {(float) (12 / 31.5 - 1), (float) (44 / 31.5 - 1), (float) (4 / 31.5 - 1), (float) (36 / 31.5 - 1), (float) (14 / 31.5 - 1), (float) (46 / 31.5 - 1), (float) (6 / 31.5 - 1), (float) (38 / 31.5 - 1)},
            {(float) (60 / 31.5 - 1), (float) (28 / 31.5 - 1), (float) (52 / 31.5 - 1), (float) (20 / 31.5 - 1), (float) (62 / 31.5 - 1), (float) (30 / 31.5 - 1), (float) (54 / 31.5 - 1), (float) (22 / 31.5 - 1)},
            {(float) (3 / 31.5 - 1), (float) (35 / 31.5 - 1), (float) (11 / 31.5 - 1), (float) (43 / 31.5 - 1), (float) (1 / 31.5 - 1), (float) (33 / 31.5 - 1), (float) (9 / 31.5 - 1), (float) (41 / 31.5 - 1)},
            {(float) (51 / 31.5 - 1), (float) (19 / 31.5 - 1), (float) (59 / 31.5 - 1), (float) (27 / 31.5 - 1), (float) (49 / 31.5 - 1), (float) (17 / 31.5 - 1), (float) (57 / 31.5 - 1), (float) (25 / 31.5 - 1)},
            {(float) (15 / 31.5 - 1), (float) (47 / 31.5 - 1), (float) (7 / 31.5 - 1), (float) (39 / 31.5 - 1), (float) (13 / 31.5 - 1), (float) (45 / 31.5 - 1), (float) (5 / 31.5 - 1), (float) (37 / 31.5 - 1)},
            {(float) (63 / 31.5 - 1), (float) (31 / 31.5 - 1), (float) (55 / 31.5 - 1), (float) (23 / 31.5 - 1), (float) (61 / 31.5 - 1), (float) (29 / 31.5 - 1), (float) (53 / 31.5 - 1), (float) (21 / 31.5 - 1)},
    };
    private final float[][] data;

    /**
     * Creates a new {@link BayerDither}.
     *
     * @param size   The size of the bayer matrix: 4 or 8.
     * @param spread the multiplication factor applied to the matrix values. With spread=1 the values are in the range [-1,+1].
     */
    public BayerDither(int size, float spread) {
        if (size != 4 && size != 8)
            throw new IllegalArgumentException("illegal size=" + size);
        this.data = new float[size][size];
        float[][] D = size == 4 ? DATA_4 : DATA_8;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = D[i][j] * spread;
            }
        }
    }

    @Override
    public float get(int x, int y) {
        return data[y & 3][x & 3];
    }

    public float[][] getData() {
        var d = new float[data.length][0];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].clone();
        }
        return d;
    }
}
