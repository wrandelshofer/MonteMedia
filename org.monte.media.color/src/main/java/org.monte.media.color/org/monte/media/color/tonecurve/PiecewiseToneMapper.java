/*
 * @(#)PiecewiseToneMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;


import java.util.Arrays;

public class PiecewiseToneMapper implements ToneMapper {
    private final float[] values;

    public PiecewiseToneMapper(float[] values) {
        this.values = values.clone();
    }

    public PiecewiseToneMapper(char[] values) {
        this.values = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = values[i] * (1f / 0xffff);
        }
    }

    @Override
    public float fromLinear(int component, float y) {
        y = Math.clamp(y, 0.0f, 1.0f);
        float pos = y * values.length;
        int index = (int) pos;
        float frac = pos - index;
        return values[index] * (1 - frac) + values[index + 1] * (1 - frac);
    }

    @Override
    public float toLinear(int component, float x) {
        int result = Arrays.binarySearch(values, x);
        if (result < 0) {
            int index = ~result;
            if (index < 0) {
                return values[0];
            } else if (index >= values.length) {
                return values[values.length - 1];
            }
            float segmentRange = values[index + 1] - values[index];
            // Avoid division by zero for flat segments
            float frac = (segmentRange == 0) ? 0 : (x - values[index]) / segmentRange;
            return (index + frac) / (values.length - 1);
        }
        return values[result];
    }
}
