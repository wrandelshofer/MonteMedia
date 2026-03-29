/*
 * @(#)SRGBToneCurveMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.trc;


/// Gamma tone curve mapper.
/// ```
/// f(x) = { ((x + b) / a)^gamma : when x >= d
///        { x / c               : when x < d
/// ```
/// ```
/// f(y) = { (a * y^(1/gamma) - b : when y >= ((d + b)/ a)^gamma
///        { c * y                : when y < c * d
/// ```
public final class GammaToneMapper implements ToneMapper {
    private final float gamma;
    private final float a;
    private final float b;
    private final float c;
    private final float d;
    private final float dbagamma;
    private final float invgamma;
    private final float invc;

    /// Creates a new instance.
    ///
    /// @param c the gamma tone curve
    public GammaToneMapper(GammaToneCurve c) {
        this(c.gamma(), c.a(), c.b(), c.c(), c.d());
    }

    /// Creates a new instance.
    ///
    /// @param gamma the gamma value
    /// @param a     the a value
    /// @param b     the b value
    /// @param c     the c value
    /// @param d     the d value
    public GammaToneMapper(float gamma, float a, float b, float c, float d) {
        this.gamma = gamma;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.dbagamma = (float) Math.pow((d + b) / a, gamma);
        this.invgamma = 1f / gamma;
        this.invc = 1f / c;
    }

    @Override
    public float fromLinear(float y) {
        float sign = Math.signum(y);
        float abs = Math.abs(y);
        if (abs > dbagamma) {
            return sign * (a * (float) Math.pow(abs, invgamma) - b);
        }
        return c * y;
    }

    @Override
    public float toLinear(float x) {
        float sign = Math.signum(x);
        float abs = Math.abs(x);
        if (abs < d) {
            return x * invc;
        }
        return sign * (float) (Math.pow((abs + b) / a, gamma));
    }

    public GammaToneCurve getParameters() {
        return new GammaToneCurve(gamma, a, b, c, d);
    }
}
