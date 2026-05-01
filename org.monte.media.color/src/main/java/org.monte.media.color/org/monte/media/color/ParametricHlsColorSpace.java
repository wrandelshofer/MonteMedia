/*
 * @(#)ParametricHlsColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.io.Serial;

/// A parametric HLS color space computed from an RGB color space.
///
/// Components:
/// <dl>
///     <dt>hue</dt><dd>0 to 360 degrees</dd>
///     <dt>lightness</dt><dd>0 to 1 percentage</dd>
///     <dt>saturation</dt><dd>0 to 1 percentage</dd>
/// </dl>
///
/// A color with maximal `lightness` is pure white.
///
/// A color with minimal `lightness` is pure black.
///
/// References:
///
/// CSS Color Module Level 4. 7. HSL Colors: hsl() and hsla() functions.
/// : [w3.org](https://www.w3.org/TR/css-color-4/#the-hsl-notation)
///
public class ParametricHlsColorSpace extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 1L;
    private final NamedColorSpace rgbColorSpace;
    private final String name;

    /// Creates a new instance that is based on the provided RGB color space.
    ///
    /// @param name          the name of the created color space
    /// @param rgbColorSpace the base RGB color space
    /// @throws IllegalArgumentException if `rgbColorSpace` has not type [#TYPE_RGB].
    public ParametricHlsColorSpace(String name, NamedColorSpace rgbColorSpace) {
        super(TYPE_HLS, 3);
        assert (rgbColorSpace.getType() == TYPE_RGB);
        this.name = name;
        this.rgbColorSpace = rgbColorSpace;
    }

    public NamedColorSpace getRgbColorSpace() {
        return rgbColorSpace;
    }

    /// XYZ to LCH.
    ///
    /// @param lch CIEXYZ color value.
    /// @return LCH color value.
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lch) {
        return rgbToHls(rgbColorSpace.fromCIEXYZ(xyz, lch), lch);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] lch) {
        return rgbToHls(rgbColorSpace.fromRGB(rgb, lch), lch);
    }

    @Override
    public float getMaxValue(int component) {
        return component == 0 ? 360f : 1f;
    }

    @Override
    public String getName() {
        return name;
    }

    protected float[] hlsToRgb(float[] hls, float[] rgb) {
        double hue = hls[0] / 360.0;
        double saturation = hls[2];
        double lightness = hls[1];

        // compute p and q from saturation and lightness
        double q;
        if (lightness < 0.5) {
            q = lightness * (1 + saturation);
        } else {
            q = lightness + saturation - (lightness * saturation);
        }
        double p = 2 * lightness - q;

        // normalize hue to -1..+1

        double hk = hue - Math.floor(hue);

        // compute red, green and blue
        double red = hk + 1 / 3.0;
        double green = hk;
        double blue = hk - 1 / 3.0;

        // normalize rgb values
        if (red < 0) {
            red = red + 1;
        } else if (red > 1) {
            red = red - 1;
        }

        if (green < 0) {
            green = green + 1;
        } else if (green > 1) {
            green = green - 1;
        }

        if (blue < 0) {
            blue = blue + 1;
        } else if (blue > 1) {
            blue = blue - 1;
        }


        // adjust rgb values
        if (red < 1 / 6d) {
            red = p + ((q - p) * 6 * red);
        } else if (red < 0.5d) {
            red = q;
        } else if (red < 2d / 3d) {
            red = p + ((q - p) * 6 * (2d / 3d - red));
        } else {
            red = p;
        }

        if (green < 1d / 6d) {
            green = p + ((q - p) * 6 * green);
        } else if (green < 0.5d) {
            green = q;
        } else if (green < 2d / 3d) {
            green = p + ((q - p) * 6 * (2d / 3d - green));
        } else {
            green = p;
        }

        if (blue < 1d / 6d) {
            blue = p + ((q - p) * 6 * blue);
        } else if (blue < 0.5d) {
            blue = q;
        } else if (blue < 2d / 3d) {
            blue = p + ((q - p) * 6 * (2d / 3d - blue));
        } else {
            blue = p;
        }

        rgb[0] = (float) Math.clamp(red, 0d, 1d);
        rgb[1] = (float) Math.clamp(green, 0d, 1d);
        rgb[2] = (float) Math.clamp(blue, 0d, 1d);
        return rgb;
    }

    protected float[] rgbToHls(float[] rgb, float[] hls) {
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];

        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);

        double hue;
        double saturation;
        double luminance;

        if (max == min) {
            hue = 0;
        } else if (max == r) {
            hue = 60f * (g - b) / (max - min);
            if (g < b) {
                hue += 360f;
            }
        } else if (max == g) {
            hue = 60f * (b - r) / (max - min) + 120f;
        } else /*if (max == b)*/ {
            hue = 60f * (r - g) / (max - min) + 240f;
        }

        luminance = (max + min) / 2f;

        if (max == min) {
            saturation = 0;
        } else if (luminance <= 0.5f) {
            saturation = (max - min) / (max + min);
        } else /* if (lightness  > 0.5f)*/ {
            saturation = (max - min) / (2 - (max + min));
        }

        hls[0] = (float) hue;
        hls[2] = (float) saturation;
        hls[1] = (float) luminance;
        return hls;
    }

    /// LCH to XYZ.
    ///
    /// @param colorvalue LCH color value.
    /// @return CIEXYZ color value.
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return rgbColorSpace.toCIEXYZ(hlsToRgb(colorvalue, xyz), xyz);
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return rgbColorSpace.toRGB(hlsToRgb(colorvalue, rgb), rgb);
    }

}
