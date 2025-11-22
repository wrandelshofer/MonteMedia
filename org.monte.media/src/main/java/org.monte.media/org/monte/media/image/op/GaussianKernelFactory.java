/*
 * @(#)GaussianKernelFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import java.util.DoubleSummaryStatistics;

/// Computes a one dimensional Gaussian kernel that is normalized to 1.
public class GaussianKernelFactory implements SeparableKernelFactory {

    /// Compute a one dimensional kernel that is normalized to 1.
    ///
    /// @param sigma the sigma of the Gaussian function
    /// @return a kernel of size ⌈sigma * 6⌉
    public float[] createKernel(float sigma) {
        // Determine the size of the kernel
        int size = (int) Math.ceil(sigma * 6);
        if ((size & 1) == 0) size += 1;//make sure the array length is odd
        float[] data = new float[size];

        // Determine function parameters
        float mu = (size - 1) * 0.5f;

        // Fill the kernel
        DoubleSummaryStatistics sum = new DoubleSummaryStatistics();
        for (int i = 0; i < size; i++) {
            float value = gaussian(sigma, i - mu);
            data[i] = value;
            sum.accept(value);
        }

        // Normalize the kernel
        data[size / 2] += (float) (1d - sum.getSum());

        return data;
    }

    /// Compute the Gaussian function for a given x value.
    ///
    /// @param sigma the sigma of the gaussian
    /// @param x     the x value
    /// @return the y value
    private static float gaussian(float sigma, float x) {
        float sigmaP2 = sigma * sigma;
        return (float) (1f / (Math.sqrt(2 * Math.PI * sigmaP2)) * Math.exp(x * x / (-2 * sigmaP2)));
    }
}
