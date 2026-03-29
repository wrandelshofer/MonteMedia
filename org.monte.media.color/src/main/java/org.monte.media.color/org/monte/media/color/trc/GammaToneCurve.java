/*
 * @(#)ParametricToneCurve.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.trc;

/// Holds the parameters of a [ParametricToneMapper].
///
/// ```
/// f(x) = { ((x + b) / a)^gamma : when x >= d
///        { (x / c)             : when x < d
/// ```
/// The sRGB parameters for this tone curve are:
/// ```
/// GammaToneCurve(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f)
/// ```
///
/// @param gamma the `gamma` value
/// @param a     the `a` value
/// @param b     the `b` value
/// @param c     the `c` value
/// @param d     the `d` value
public record GammaToneCurve(float gamma, float a, float b, float c, float d) {
}
