/*
 * @(#)ParametricScaledColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.io.Serial;

/// A parametric color space with scaled components computed from an RGB color space
/// with component values in the range `[0, 1]`.
public class ParametricScaledColorSpace extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 1L;
    private final NamedColorSpace labColorSpace;
    private final String name;
    private final float scale;
    private final float inverseScale;
    private final int equivalentBuiltInColorSpace;

    public int getEquivalentBuiltInColorSpace() {
        return equivalentBuiltInColorSpace;
    }

    public ParametricScaledColorSpace(String name, float scale, NamedColorSpace rgbColorSpace, int equivalentBuiltInColorSpace) {
        super(TYPE_RGB, 3);
        this.equivalentBuiltInColorSpace = equivalentBuiltInColorSpace;
        assert (rgbColorSpace.getType() == TYPE_RGB);
        this.name = name;
        this.labColorSpace = rgbColorSpace;
        this.scale = scale;
        this.inverseScale = 1 / scale;
    }

    /// XYZ to LCH.
    ///
    /// @param lch CIEXYZ color value.
    /// @return LCH color value.
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lch) {
        return rgbToScaled(labColorSpace.fromCIEXYZ(xyz, lch), lch);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] lch) {
        return rgbToScaled(labColorSpace.fromRGB(rgb, lch), lch);
    }

    @Override
    public float getMaxValue(int component) {
        return scale;
    }

    @Override
    public String getName() {
        return name;
    }

    protected float[] scaledToRgb(float[] scaled, float[] rgb) {
        rgb[0] = scaled[0] * inverseScale;
        rgb[1] = scaled[1] * inverseScale;
        rgb[2] = scaled[2] * inverseScale;
        return rgb;
    }

    protected float[] rgbToScaled(float[] rgb, float[] scaled) {
        scaled[0] = rgb[0] * scale;
        scaled[1] = rgb[1] * scale;
        scaled[2] = rgb[2] * scale;
        return rgb;
    }

    /// LCH to XYZ.
    ///
    /// @param colorvalue LCH color value.
    /// @return CIEXYZ color value.
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return labColorSpace.toCIEXYZ(scaledToRgb(colorvalue, xyz), xyz);
    }

    @Override
    public float[] toRGB(float[] lch, float[] rgb) {
        return labColorSpace.toRGB(scaledToRgb(lch, rgb), rgb);
    }

}
