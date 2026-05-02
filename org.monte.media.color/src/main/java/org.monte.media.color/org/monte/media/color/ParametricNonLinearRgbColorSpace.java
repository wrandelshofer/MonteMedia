/*
 * @(#)ParametricNonLinearRgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.ToneMapper;

import java.awt.color.ColorSpace;

/// Adds a non-linear transfer function to a linear `RGB` color space.
public class ParametricNonLinearRgbColorSpace extends AbstractNamedColorSpace {
    private final NamedColorSpace linearCS;
    private final String name;
    private final ToneMapper toneMapper;

    public ParametricNonLinearRgbColorSpace(String name, NamedColorSpace linearCS,
                                            ToneMapper toneMapper) {
        super(ColorSpace.TYPE_RGB, 3);
        this.linearCS = linearCS;
        this.name = name;
        this.toneMapper = toneMapper;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromLinear(linearCS.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    protected float[] fromLinear(float[] linear, float[] curved) {
        return toneMapper.linearToCurved(linear, curved);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromLinear(linearCS.fromRGB(rgb, colorvalue), colorvalue);
    }


    public NamedColorSpace getLinearColorSpace() {
        return linearCS;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return linearCS.toCIEXYZ(toLinear(colorvalue, xyz), xyz);
    }

    protected float[] toLinear(float[] curved, float[] linear) {
        return toneMapper.curvedToLinear(curved, linear);
    }

    public ToneMapper getToneMapper() {
        return toneMapper;
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
