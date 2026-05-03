/*
 * @(#)ParametricToneCurve.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

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
public record GammaToneCurve(float gamma, float a, float b, float c, float d) implements ToneCurve {
    public GammaToneCurve(float gamma) {
        this(gamma, 1, 0, 1, 0);
    }

    public GammaToneCurve(float gamma, float a, float b, float c, float d) {
        this.gamma = gamma;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    //FIXME CHECKME
    public ParametricToneCurve toParametricToneCurve() {
        return new ParametricToneCurve(gamma, 1 / a, b * a, 1 / c, d);
    }
}
