/*
 * @(#)PiecewiseToneMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;


import java.util.Arrays;

public class PiecewiseToneMapper implements ToneMapper {
    private final float[] ltc;
    private final float[] ctl;

    public PiecewiseToneMapper(float[] linearToCurved) {
        this.ltc = linearToCurved.clone();
        ctl = computeInverseMap(linearToCurved);
    }

    public PiecewiseToneMapper(char[] linearToCurved) {
        this.ltc = new float[linearToCurved.length];
        for (int i = 0; i < linearToCurved.length; i++) {
            this.ltc[i] = linearToCurved[i] * (1f / 0xffff);
        }
        ctl = computeInverseMap(this.ltc);
    }

    private float[] computeInverseMap(float[] a) {
        var b = new float[a.length];
        int n = a.length;
        float minVal = a[0];
        float maxVal = a[n - 1];

        double divisor = (n - 1);
        for (int i = 0; i < n; i++) {
            double y = minVal + (i / divisor * (maxVal - minVal));
            double x;
            int searchResult = Arrays.binarySearch(a, (float) y);
            if (searchResult < 0) {
                int index = ~searchResult;
                if (index >= n) {
                    x = a[n - 1];
                } else {
                    double weight = (y - a[index - 1]) / (a[index] - a[index - 1]);
                    x = (index - 1 + weight) / divisor;
                }
            } else {
                x = a[searchResult];
            }
            b[i] = (float) x;
        }
        return b;
    }

    @Override
    public float linearToCurved(int component, float y) {
        int n = ltc.length;
        float minVal = ltc[0];
        float maxVal = ltc[n - 1];
        y = Math.clamp(y, minVal, maxVal);
        float pos = (y - minVal) * (n - 1);
        int index = (int) pos;
        if (index >= n - 1) {
            return ltc[n - 1];
        }
        float frac = pos - index;
        return ltc[index] * (1 - frac) + ltc[index + 1] * (frac);
    }

    @Override
    public float curvedToLinear(int component, float x) {
        int n = ctl.length;
        float minVal = ctl[0];
        float maxVal = ctl[n - 1];
        x = Math.clamp(x, minVal, maxVal);
        float pos = (x - minVal) * (n - 1);
        int index = (int) pos;
        if (index >= n - 1) {
            return ctl[n - 1];
        }
        float frac = pos - index;
        return ctl[index] * (1 - frac) + ctl[index + 1] * (frac);
        /*
        int searchResult = Arrays.binarySearch(linearToCurved, x);
        if (searchResult < 0) {
            int index = ~searchResult;
            if (index < 0) {
                return linearToCurved[0];
            } else if (index >= linearToCurved.length) {
                return linearToCurved[linearToCurved.length - 1];
            }
            float segmentRange = linearToCurved[index + 1] - linearToCurved[index];
            // Avoid division by zero for flat segments
            float frac = (segmentRange == 0) ? 0 : (x - linearToCurved[index]) / segmentRange;
            return (index + frac) / (linearToCurved.length - 1);
        }
        return linearToCurved[searchResult];

         */
    }
}
