/*
 * @(#)GammaToneMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

/// Parametric tone curve mapper.
/// ```
/// f(x) = { (a * x + b)^gamma : when x >= d
///        { c * x             : when x < d
/// ```
/// ```
/// f(y) = { (y^(1/gamma) - b) / a : when y >= (a * d + b)^gamma
///        { y / c                 : when y < c * d
/// ```
public final class ParametricToneMapper implements ToneMapper {
    private final float gamma;
    private final float a;
    private final float b;
    private final float c;
    private final float d;
    private final float adbgamma;
    private final float invgamma;
    private final float invc;
    private final float inva;

    /// Creates a new instance.
    ///
    /// @param c the gamma tone curve
    public ParametricToneMapper(ParametricToneCurve c) {
        this(c.gamma(), c.a(), c.b(), c.c(), c.d());
    }

    /// Creates a new instance.
    ///
    /// @param gamma the gamma value
    /// @param a     the `a` value
    /// @param b     the `b` value
    /// @param c     the `c` value
    /// @param d     the `d` value
    public ParametricToneMapper(float gamma, float a, float b, float c, float d) {
        this.gamma = gamma;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.adbgamma = (float) Math.pow(a * d + b, gamma);
        this.invgamma = 1f / gamma;
        this.invc = 1f / c;
        this.inva = 1f / a;
    }

    @Override
    public float linearToCurved(int component, float y) {
        float sign = Math.signum(y);
        float abs = Math.abs(y);
        return (abs >= adbgamma)
                ? sign * (float) (Math.pow(abs, invgamma) - b) * inva
                : y * invc;
    }

    @Override
    public float curvedToLinear(int component, float x) {
        float sign = Math.signum(x);
        float abs = Math.abs(x);
        return (abs >= d)
                ? sign * (float) Math.pow(a * abs + b, gamma)
                : c * x;
    }

    public ParametricToneCurve getParameters() {
        return new ParametricToneCurve(gamma, a, b, c, d);
    }

}
