/*
 * @(#)ParametricXyzColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Matrix3;

import java.awt.color.ColorSpace;

/// An XYZ color space with a linear transformation matrix from/to XYZ D50.
public class ParametricXyzColorSpace extends AbstractNamedColorSpace {
    private final static SrgbColorSpace SRGB_COLOR_SPACE = new SrgbColorSpace();

    private final Matrix3 toXyzMatrix;
    private final Matrix3 fromXyzMatrix;

    private final String name;

    public ParametricXyzColorSpace(String name, Matrix3 toXyzMatrix, Matrix3 fromXyzMatrix) {
        super(ColorSpace.TYPE_XYZ, 3);
        this.toXyzMatrix = toXyzMatrix;
        this.fromXyzMatrix = fromXyzMatrix;
        this.name = name;
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return toXyzMatrix.mul(colorvalue, xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromXyzMatrix.mul(xyz, colorvalue);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromXyzMatrix.mul(SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return SRGB_COLOR_SPACE.fromCIEXYZ(toXyzMatrix.mul(colorvalue, rgb), rgb);
    }
}
