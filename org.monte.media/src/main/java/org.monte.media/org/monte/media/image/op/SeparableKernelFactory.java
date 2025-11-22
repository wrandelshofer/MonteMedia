/*
 * @(#)SeparableKernelFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

/// Factory for one dimensional separable kernels that are normalized to 1.
public interface SeparableKernelFactory {
    /// Compute a one dimensional kernel that is normalized to 1.
    ///
    /// @param radius the support size of the kernel
    /// @return the kernel
    float[] createKernel(float radius);
}
