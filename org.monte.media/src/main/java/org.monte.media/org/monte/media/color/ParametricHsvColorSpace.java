/*
 * @(#)ParametricHsvColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.awt.color.ColorSpace;
import java.io.Serial;

/**
 * A parametric HSV color space computed from an RGB color space.
 * <p>
 * Components:
 * <dl>
 *     <dt>hue</dt><dd>0 to 360 degrees</dd>
 *     <dt>saturation</dt><dd>0 to 1 percentage</dd>
 *     <dt>value</dt><dd>0 to 1 percentage</dd>
 * </dl>
 * <p>
 * A color with maximal {@code value} is analogous to shining a white light on a colored object.
 * It is white only if {@code saturation} is also 0%.
 * <p>
 * A color with minimal {@code value} is analogous to shining no light on a colored object
 * It is black, no matter the {@code hue}, no matter the {@code saturation}.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. 7. HSL Colors: hsl() and hsla() functions.</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#the-hsl-notation">w3.org</a></dd>
 * </dl>
 *
 */
public class ParametricHsvColorSpace extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 1L;
    private final NamedColorSpace rgbColorSpace;
    private final String name;

    /**
     * Creates a new instance that is based on the provided RGB color space.
     *
     * @param name          the name of the created color space
     * @param rgbColorSpace the base RGB color space
     * @throws IllegalArgumentException if {@code rgbColorSpace} has not type {@link ColorSpace#TYPE_RGB}.
     */
    public ParametricHsvColorSpace(String name, NamedColorSpace rgbColorSpace) {
        super(TYPE_HSV, 3);
        assert (rgbColorSpace.getType() == TYPE_RGB);
        this.name = name;
        this.rgbColorSpace = rgbColorSpace;
    }

    /**
     * XYZ to LCH.
     *
     * @param lch CIEXYZ color value.
     * @return LCH color value.
     */
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lch) {
        return rgbToHsv(rgbColorSpace.fromCIEXYZ(xyz, lch), lch);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] lch) {
        return rgbToHsv(rgbColorSpace.fromRGB(rgb, lch), lch);
    }

    @Override
    public float getMaxValue(int component) {
        return component == 0 ? 360f : 1f;
    }

    @Override
    public String getName() {
        return name;
    }

    protected float[] hsvToRgb(float[] hsv, float[] rgb) {
        float hue = ((hsv[0] % 360f) + 360f) % 360f / 360f;
        float saturation = hsv[1];
        float value = hsv[2];

        float r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = value;
        } else {
            float h = (float) (hue - Math.floor(hue)) * 6;
            float f = h - (float) Math.floor(h);
            float p = value * (1f - saturation);
            float q = value * (1f - saturation * f);
            float t = value * (1f - (saturation * (1f - f)));
            switch ((int) h) {
                case 0:
                    r = value;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = value;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = value;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = value;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = value;
                    break;
                case 5:
                    r = value;
                    g = p;
                    b = q;
                    break;
            }
        }

        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        return rgb;
    }

    protected float[] rgbToHsv(float[] rgb, float[] hsv) {
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];
        float hue, saturation, value;
        float cmax = Math.max(r, g);
        if (b > cmax) {
            cmax = b;
        }
        float cmin = Math.min(r, g);
        if (b < cmin) {
            cmin = b;
        }

        value = cmax;
        if (cmax != 0) {
            saturation = (cmax - cmin) / cmax;
        } else {
            saturation = 0;
        }

        if (saturation == 0) {
            hue = 0;
        } else {
            float redc = (cmax - r) / (cmax - cmin);
            float greenc = (cmax - g) / (cmax - cmin);
            float bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax) {
                hue = bluec - greenc;
            } else if (g == cmax) {
                hue = 2f + redc - bluec;
            } else {
                hue = 4f + greenc - redc;
            }
            hue = hue / 6f;
            if (hue < 0) {
                hue = hue + 1f;
            }
        }
        hsv[0] = hue * 360f;
        hsv[1] = saturation;
        hsv[2] = value;
        return hsv;
    }

    /**
     * LCH to XYZ.
     *
     * @param colorvalue LCH color value.
     * @return CIEXYZ color value.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return rgbColorSpace.toCIEXYZ(hsvToRgb(colorvalue, xyz), xyz);
    }

    @Override
    public float[] toRGB(float[] lch, float[] rgb) {
        return rgbColorSpace.toRGB(hsvToRgb(lch, rgb), rgb);
    }

}
