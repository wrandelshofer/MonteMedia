/*
 * @(#)ParametricNonLinearRgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.FloatFunction;

import java.awt.color.ColorSpace;

/**
 * Adds a non-linear transfer function to a linear {@code RGB} color space.
 */
public class ParametricNonLinearRgbColorSpace extends AbstractNamedColorSpace {
    private final FloatFunction fromLinear;
    private final NamedColorSpace linearCS;
    private final String name;
    private final FloatFunction toLinear;

    public ParametricNonLinearRgbColorSpace(String name, NamedColorSpace linearCS,
                                            FloatFunction toLinear,
                                            FloatFunction fromLinear) {
        super(ColorSpace.TYPE_RGB, 3);
        this.linearCS = linearCS;
        this.name = name;
        this.toLinear = toLinear;
        this.fromLinear = fromLinear;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromLinear(linearCS.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    protected float[] fromLinear(float[] linear, float[] corrected) {
        corrected[0] = fromLinear.apply(linear[0]);
        corrected[1] = fromLinear.apply(linear[1]);
        corrected[2] = fromLinear.apply(linear[2]);
        return corrected;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromLinear(linearCS.fromRGB(rgb, colorvalue), colorvalue);
    }

    public FloatFunction getFromLinear() {
        return fromLinear;
    }

    public NamedColorSpace getLinearColorSpace() {
        return linearCS;
    }

    @Override
    public String getName() {
        return name;
    }

    public FloatFunction getToLinear() {
        return toLinear;
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return linearCS.toCIEXYZ(toLinear(colorvalue, xyz), xyz);
    }

    protected float[] toLinear(float[] corrected, float[] linear) {
        linear[0] = toLinear.apply(corrected[0]);
        linear[1] = toLinear.apply(corrected[1]);
        linear[2] = toLinear.apply(corrected[2]);
        return linear;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return linearCS.toRGB(toLinear(colorvalue, rgb), rgb);
    }

    @Override
    public float getMinValue(int component) {
        return linearCS.getMinValue(component);
    }

    @Override
    public float getMaxValue(int component) {
        return linearCS.getMaxValue(component);
    }
}
