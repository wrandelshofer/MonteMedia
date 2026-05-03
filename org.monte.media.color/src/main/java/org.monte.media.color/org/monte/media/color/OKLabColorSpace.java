/*
 * @(#)OKLabColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

import org.monte.media.color.tonecurve.GammaToneMapper;
import org.monte.media.color.tonecurve.ToneMapper;
import org.monte.media.math.Matrix3Double;

import java.awt.color.ColorSpace;
import java.io.Serial;

/// The OK Lab Color Space.
///
/// There lightness `L` axis is a number in the range `[0,1]`. Where 0 is a deep black and 1 is a diffuse white.
///
/// The `a` and `b` axes convey hue; positive values along the `a` axis are a purplish red while
/// negative values are the complementary color, a green.
/// Similarly, positive values along the `b` axis are yellow and negative are the complementary blue/violet.
///
/// The `a` and `b` values are unbounded. CSS associates the range of ±0.4 to
/// the range of -100% to +100%.
///
/// The distance that can be calculated between two colors, is
/// directly proportional to the difference between the two colors as perceived
/// by the human eye.
///
/// Given a color in XYZ coordinates, with a D65 white point and white as Y=1,
/// OK Lab coordinates can be computed like this:
///
/// First the XYZ coordinates are converted to an approximate cone responses:
/// ```
///    [l]          [X]
///    [m] = M_1 ×  [Y]
///    [s]          [Z]
/// ```
/// A non-linearity  is applied
/// ```
///    [l' ]    [∛l]
///    [m' ] =  [∛m]
///    [s' ]    [∛s]
/// ```
/// Finally, this is transformed into the LabLab-coordinates:
/// ```
///    [L]          [l' ]
///    [a] = M_2 ×  [m' ]
///    [b]          [s' ]
/// ```
/// With the following values for M1M1 and M2M2:
/// ```
/// M1= [+0.8189330101+0.3618667424 −0.1288597137]
///     [+0.0329845436+0.9293118715+0.0361456387]
///     [+0.0482003018+0.2643662691+0.6338517070]
///
///
/// M2 = [+0.2104542553+0.7936177850 −0.0040720468]
///      [+1.9779984951 −2.4285922050+0.4505937099]
///      [+0.0259040371+0.7827717662 −0.8086757660]
/// ```
/// Note that we have to use a different matrix for M1 when we compute from/to XYZ with a D50 white point.
///
/// References:
/// <dl>
///     <dt>Börn Ottosson, A perceptual color space for image processing, Converting from linear sRGB to Oklab.
///     [MIT License](https://github.com/bottosson/bottosson.github.io/blob/3d3f17644d7f346e1ce1ca08eb8b01782eea97af/misc/colorpicker/License.txt)</dt>
///     <dd>[github.io](https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab)</dd>
/// </dl>
@SuppressWarnings("UnnecessaryLocalVariable")
public class OKLabColorSpace extends AbstractNamedColorSpace {
    public static OKLabColorSpace getInstance() {
        class Holder {
            private static final OKLabColorSpace INSTANCE = new OKLabColorSpace();
        }
        return Holder.INSTANCE;
    }

    @Override
    public int getEquivalentBuiltInColorSpace() {
        return -1;
    }

    /// Concatenation of RGB_to_XYZ_D65 matrix and the M1 matrix.
    ///
    /// This matrix computes lms directly from linear sRGB values.
    /// ```
    ///    [X]                     [R]
    ///    [Y] = RGB_to_XYZ_D65 ×  [G]
    ///    [Z]                     [B]
    /// ```
    /// ```
    ///    [l]          [X]
    ///    [m] = M_1 ×  [Y]
    ///    [s]          [Z]
    /// ```
    private static final Matrix3Double M1_RGB = new Matrix3Double(
            0.4122214708, +0.5363325363, +0.0514459929,
            0.2119034982, +0.6806995451, +0.1073969566,
            0.0883024619, +0.2817188376, +0.6299787005
    );
    /// The M1 matrix.
    private static final Matrix3Double M1 = new Matrix3Double(
            +0.8189330101, +0.3618667424, -0.1288597137,
            +0.0329845436, +0.9293118715, +0.0361456387,
            +0.0482003018, +0.2643662691, +0.6338517070
    );
    /// The inverse of the M1 matrix.
    private static final Matrix3Double M1_INV = M1.inv();
    /// The M2 matrix.
    private static final Matrix3Double M2 = new Matrix3Double(
            0.2104542553, 0.7936177850, -0.0040720468,
            1.9779984951, -2.4285922050, +0.4505937099,
            0.0259040371, +0.7827717662, -0.8086757660
    );
    /// The inverse of the M2 matrix.
    private static final Matrix3Double M2_INV = new Matrix3Double(
            1, +0.3963377774, +0.2158037573,
            1, -0.1055613458, -0.0638541728,
            1, -0.0894841775, -1.2914855480
    );
    /// Concatenation of M<sup>-1</sup> and RGB<sup>-1</sup> matrix.
    ///
    /// This matrix computes sRGB values directly from lms values.
    /// ```
    ///    [X]            [l]
    ///    [Y] = M_1^-1 ×  [m]
    ///    [Z]            [s]
    /// ```
    /// ```
    ///    [R]                        [X]
    ///    [G] = RGB_to_XYZ_D65^-1 ×  [Y]
    ///    [B]                        [Z]
    /// ```
    private static final Matrix3Double RGB_INV_M1_INV = new Matrix3Double(
            4.0767416621, -3.3077115913, +0.2309699292,
            -1.2684380046, +2.6097574011, -0.3413193965,
            -0.0041960863, -0.7034186147, +1.7076147010
    );
    private static final NamedColorSpace linearSrgb = LinearSrgbColorSpace.getInstance();
    @Serial
    private static final long serialVersionUID = 1L;
    private final ToneMapper toneMapper = new GammaToneMapper(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f);

    public OKLabColorSpace() {
        super(ColorSpace.TYPE_Lab, 3);

    }

    @Override
    public float[] toCIEXYZ(float[] lab, float[] xyz) {
        var lms = M2_INV.mul(xyz, lab);
        lms[0] = lms[0] * lms[0] * lms[0];
        lms[1] = lms[1] * lms[1] * lms[1];
        lms[2] = lms[2] * lms[2] * lms[2];
        xyz = M1_INV.mul(lms, lab);
        xyz = ParametricLinearRgbColorSpace.FROM_D65_TO_D50.mul(xyz, xyz);
        return xyz;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lab) {
        xyz = ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ.mul(xyz, xyz);
        var lms = M1.mul(xyz, lab);
        lms[0] = (float) Math.cbrt(lms[0]);
        lms[1] = (float) Math.cbrt(lms[1]);
        lms[2] = (float) Math.cbrt(lms[2]);
        lab = M2.mul(lms, lab);
        return lab;
    }

    protected float[] fromLinear(float[] linear, float[] curved) {
        return toneMapper.linearToCurved(linear, curved);
    }

    public float[] fromLinearRGB(float[] rgb, float[] lab) {
        // Convert from linear sRGB to approximate cone responses
        float[] lms = M1_RGB.mul(rgb, lab);

        // Apply non-linearity
        float[] lms_ = lms;
        lms_[0] = (float) Math.cbrt(lms[0]);
        lms_[1] = (float) Math.cbrt(lms[1]);
        lms_[2] = (float) Math.cbrt(lms[2]);

        // Convert transformed cone responses to lab
        return M2.mul(lms_, lab);
    }

    @Override
    public float[] fromRGB(float[] srgbvalue, float[] colorvalue) {
        return fromLinearRGB(toLinear(srgbvalue, colorvalue), colorvalue);
    }

    @Override
    public float getMaxValue(int component) {
        return switch (component) {
            case 0 -> 1f;
            case 1, 2 -> 0.4f;
            default -> throw new IllegalArgumentException("component=" + component);
        };
    }

    @Override
    public float getMinValue(int component) {
        return switch (component) {
            case 0 -> 0f;
            case 1, 2 -> -0.4f;
            default -> throw new IllegalArgumentException("component=" + component);
        };
    }

    @Override
    public String getName() {
        return "OKLAB";
    }


    protected float[] toLinear(float[] curved, float[] linear) {
        return toneMapper.curvedToLinear(curved, linear);
    }

    protected float[] toLinearRGB(float[] lab, float[] rgb) {
        // Convert from lab to transformed cone responses
        float[] lms_ = M2_INV.mul(lab, rgb);

        // Unapply non-linearity
        float[] lms = lms_;
        float l_ = lms_[0];
        float m_ = lms_[1];
        float s_ = lms_[2];
        lms[0] = l_ * l_ * l_;
        lms[1] = m_ * m_ * m_;
        lms[2] = s_ * s_ * s_;

        // Convert from approximate cone responses to linear sRGB
        return RGB_INV_M1_INV.mul(lms, rgb);


    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return fromLinear(toLinearRGB(colorvalue, rgb), rgb);
    }
}
