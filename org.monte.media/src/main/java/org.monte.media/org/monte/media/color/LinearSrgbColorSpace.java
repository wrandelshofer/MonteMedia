/*
 * @(#)LinearSrgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

/**
 * Linear {@code sRGB} Color Space.
 * <dl>
 *     <dt>Wikipedia: sRGB. Transformation</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/SRGB#Transformation">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class LinearSrgbColorSpace extends ParametricLinearRgbColorSpace {
    public final static LinearSrgbColorSpace INSTANCE = new LinearSrgbColorSpace();

    public LinearSrgbColorSpace() {
        super("sRGB Linear",
                new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ
        );
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return fromLinear(colorvalue, rgb);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return toLinear(rgb, colorvalue);
    }

    protected static float[] fromLinear(float[] linear, float[] corrected) {
        corrected[0] = LinearSrgbColorSpace.fromLinear(linear[0]);
        corrected[1] = LinearSrgbColorSpace.fromLinear(linear[1]);
        corrected[2] = LinearSrgbColorSpace.fromLinear(linear[2]);
        return corrected;
    }


    protected static float[] toLinear(float[] corrected, float[] linear) {
        linear[0] = LinearSrgbColorSpace.toLinear(corrected[0]);
        linear[1] = LinearSrgbColorSpace.toLinear(corrected[1]);
        linear[2] = LinearSrgbColorSpace.toLinear(corrected[2]);
        return linear;
    }

    /**
     * Inverse "Gamma" transfer function.
     * <p>
     * Convert an array of linear-light sRGB values in the range 0.0-1.0
     * to gamma corrected form.
     * <p>
     * Extended transfer function:
     * For negative values, linear portion extends on reflection
     * of axis, then uses reflected pow below that
     */
    public static float fromLinear(float linear) {
        float sign = Math.signum(linear);
        float abs = Math.abs(linear);
        if (abs > 0.0031308f) {
            return sign * (1.055f * (float) Math.pow(abs, 1 / 2.4d) - 0.055f);
        }
        return 12.92f * linear;
    }

    /**
     * "Gamma" transfer function.
     * <p>
     * Convert an array of sRGB values
     * where in-gamut values are in the range [0 - 1]
     * to linear light (un-companded) form.
     * <p>
     * Extended transfer function:
     * for negative values,  linear portion is extended on reflection of axis,
     * then reflected power function is used.
     */
    public static float toLinear(float nonlinear) {
        float sign = Math.signum(nonlinear);
        float abs = Math.abs(nonlinear);
        if (abs < 0.04045f) {
            return nonlinear / 12.92f;
        }
        return sign * (float) (Math.pow((abs + 0.055f) / 1.055f, 2.4));
    }
}
