/*
 * @(#)ParametricToneCurve.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.trc;

/// Holds the parameters of a [ParametricToneMapper].
///
/// ```
/// f(x) = { ( a * x + b )^gamma : when x >= d
///        { (c * x)             : when x < d
/// ```
///
/// These parameters can be used instead of [GammaToneCurve]
/// we have:
/// ```
/// ParametricToneCurve gamma = GammaToneCurve gamma
///                         a = 1 / a
///                         b = b / a
///                         c = 1 / c
///                         d = d
/// ```
/// The sRGB parameters for this tone curve are:
/// ```
/// GammaToneCurve(2.4f, 1/1.055f, 0.055f/1.055f, 1/12.92f, 0.04045f)
/// ```
///
/// @param gamma the `gamma` value
/// @param a     the `a` value
/// @param b     the `b` value
/// @param c     the `c` value
/// @param d     the `d` value
public record ParametricToneCurve(float gamma, float a, float b, float c, float d) {
}
