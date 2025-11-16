/*
 * @(#)Rec709ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// Rec. 709 Color Space.
///
/// The Rec. 709 color space is used for high definition television (HDTV).
/// It uses the same primaries as sRGB but uses a different gamma curve.
/// Rec. 709 is intended for use on a TV screen in a dark environment,
/// whereas sRGB is intended for use on a computer screen in a bright environment.
///
/// Chromatic coordinates:
///
/// | Color       | x      | y      |
/// |-------------|--------|--------|
/// | Red (R)     | 0.640  | 0.330  |
/// | Green (G)   | 0.300  | 0.600  |
/// | Blue (B)    | 0.150  | 0.060  |
/// | White (D65) | 0.3127 | 0.3290 |
///
/// Conceptual Gamma correction performed by the transfer function:
///
/// ```
/// gamma ~ 0.45
///```
///
/// Transfer function `fromLinear(L:float) -> V:float`
/// The transfer function converts linear RGB values (`ER`, `EG`, `EB`)
/// to gamma corrected RGB values (`EpR`, `EpG`, `EpB`).
/// This is the same function that is used for Rec. 2020.
///
/// ```
/// V = 1.099 * L^0.45 - 0.099  for 1     >= L >= 0.018
/// V = 4.500 * L               for 0.018 >  L >= 0
///```
///
/// Derivation of luminance signal `EpY`:
///
/// ```
/// EpY = 0.2126 * EpR + 0.7152 * EpG + 0.0722 * EpR
///```
///
/// Derivation of color difference signal (analogue coding):
///
/// ```
/// EpCB = ( EpB - EpY )/ 1.8556
///      = ( -0.2126 * EpR - 0.7152 * EpG + 0.9278 EpB )/ 1.8556
///
/// EpCR = ( EpR - EpY )/ 1.5748
///      = ( -0.7874 * EpR - 0.7152 * EpG + 0.0722 EpB )/ 1.5748
///```
///
/// Quantization of RGB luminance and color difference signals:
///
/// To avoid confusion between 8-bit and 10-bit representations, the eight most-significant bits are
/// considered to be an integer part while the two additional bits, if present, are considered to be fractional
/// parts.
///
/// ```
/// DpR  = INT((219 * EpR  +  16) * 2^(n-8))
/// DpG  = INT((219 * EpG  +  16) * 2^(n-8))
/// DpB  = INT((219 * EpB  +  16) * 2^(n-8))
///
/// DpY  = INT((219 * EpY  + 16) * 2^(n-8))
/// DpCB = INT((224 * EpCB + 128) * 2^(n-8))
/// DpCR = INT((224 * EpCR + 128) * 2^(n-8))
///```
///
/// ITU. Recommendation BT.709 (06/2025)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.709/en)
///
/// CSS Color Module Level 4. Sample code for Color Conversions
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#color-conversion-code)
///
/// Wikipedia: Rec. 709
/// : [wikipedia](https://en.wikipedia.org/wiki/Rec._709)
public class Rec709ColorSpace extends ParametricNonLinearRgbColorSpace {
    public final static Rec709ColorSpace INSTANCE = new Rec709ColorSpace();

    public Rec709ColorSpace() {
        super("Rec. 709", new ParametricLinearRgbColorSpace("Linear Rec. 709",
                        new Point2D(0.640, 0.330),
                        new Point2D(0.3, 0.6),
                        new Point2D(0.15, 0.06),
                        ILLUMINANT_D65_XYZ
                ),
                Rec709ColorSpace::toLinear, Rec709ColorSpace::fromLinear
        );
    }

    /**
     * Convert an array of linear-light rec709 RGB  in the range 0.0-1.0
     * to gamma corrected form.
     * ITU-R BT.2020-2 p.4
     */
    public static float fromLinear(float linear) {
        float α = 1.09929682680944f;
        float β = 0.018053968510807f;

        float sign = Math.signum(linear);
        float abs = Math.abs(linear);

        float c;
        if (abs < β) {
            c = linear * 4.5f;
        } else {
            c = sign * ((float) Math.pow(abs, 0.45f) * α - α + 1);
        }
        return c;
    }

    /**
     * Convert an array of rec709 RGB values in the range 0.0 - 1.0
     * to linear light (un-companded) form.
     * ITU-R BT.2020-2 p.4
     */
    public static float toLinear(float nonlinear) {
        float α = 1.09929682680944f;
        float β = 0.018053968510807f;

        float sign = Math.signum(nonlinear);
        float abs = Math.abs(nonlinear);

        float cl;
        if (abs < β * 4.5f) {
            cl = nonlinear / 4.5f;
        } else {
            cl = sign * ((float) Math.pow((abs + α - 1) / α, 1 / 0.45f));
        }
        return cl;
    }
}
