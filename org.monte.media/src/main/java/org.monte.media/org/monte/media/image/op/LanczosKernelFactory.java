/*
 * @(#)LanczosKernelFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import java.util.DoubleSummaryStatistics;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.sin;

/// Computes a one dimensional Lanczos kernel that is normalized to 1.
public class LanczosKernelFactory implements SeparableKernelFactory {
    /// Compute a one dimensional kernel that is normalized to 1.
    ///
    /// @param radius the support size of the Lanczos function
    /// @return a kernel that contains 2 times sigma elements
    public float[] createKernel(float radius) {
        // Determine the size 'support' of the kernel
        int size = (int) ceil(radius * 2);// radius times 2
        if ((size & 1) == 0) size += 1;//make sure the array length is odd
        float[] data = new float[size];

        // Determine function parameters
        float mu = (size - 1) * 0.5f;

        // Fill the kernel
        DoubleSummaryStatistics sum = new DoubleSummaryStatistics();
        for (int i = 0; i < size; i++) {
            float value = lanczos(radius, i - mu);
            data[i] = value;
            sum.accept(value);
        }

        return data;
    }

    /// Compute the Lanczos function for a given x value.
    /// ```
    /// L(x) = 1                                  : x == 0
    ///       a*sin(PI*x)*sin(PI*x/a)/(PI^2*x^2)  : -a <= x and x < a and x != 0
    ///       0                                   : otherwise
    /// ```
    ///
    /// @param a the support size of the Lanczos function
    /// @param x the x value
    /// @return the y value
    private static float lanczos(float a, float x) {
        if (x == 0.0f) return 1.0f;
        if (abs(x) > a) return 0.0f;
        return (float) (a * sin(PI * x) * sin(PI * x / a) / (PI * PI * x * x));
    }
}
