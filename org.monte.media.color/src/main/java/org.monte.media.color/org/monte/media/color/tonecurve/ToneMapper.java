/*
 * @(#)ToneCurveMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

/// Interface for tone reproduction curve mappers.
/// Tone curve mappers map between a (curved) device space `x`
/// from/to a linear space `y`.
public interface ToneMapper {
    /// Maps from the linear space to the (curved) device space
    ///
    /// @param component the component index
    /// @param y         a value in the linear space
    /// @return a value in the (curved) device space
    float fromLinear(int component, float y);

    /// Maps from the (curved) device space into the linear space.
    ///
    /// @param component the component index
    /// @param x         a value in the (curved) device space
    /// @return a value in the curved device space
    float toLinear(int component, float x);

    /// Maps from the linear space to the (curved) device space
    ///
    /// @param y a value in the linear space
    /// @param x a value in the (curved) device space
    /// @return a value in the (curved) device space
    default float[] fromLinear(float[] y, float[] x) {
        for (int i = 0; i < y.length; i++) {
            x[i] = fromLinear(i, y[i]);
        }
        return x;
    }

    /// Maps from the (curved) device space into the linear space.
    ///
    /// @param x a value in the (curved) device space
    /// @param y a value in the linear space
    /// @return a value in the curved device space
    default float[] toLinear(float[] x, float[] y) {
        for (int i = 0; i < x.length; i++) {
            y[i] = toLinear(i, x[i]);
        }
        return y;
    }
}
