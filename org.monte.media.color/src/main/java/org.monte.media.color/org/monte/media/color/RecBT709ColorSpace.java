/*
 * @(#)Rec709ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.ParametricToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// Recommendation ITU-R BT.709-6 Color Space.
///
/// The BT.709-6 color space is used for encoding high definition television (HDTV).
/// It has the same primaries as sRGB but uses a different gamma curve.
///
/// BT.709-6 is intended for recording video in a bright environment.
/// To present such a video in a dark environment, the BT.1886 color space
/// is used on the BT.709-6 encoded data (instead of doing a proper conversion
/// from BT.709-6 to XYZ and then to BT.1886).
///
/// In other words, BT.709-6 is used for the transform from linear to curved
/// (opto-electronic transfer function EOTF). And then later BT.1886 is used
/// to perform the inverse operation/ from curved to linear
/// (electro-optical transfer function OETF).
///
///
/// Chromatic coordinates:
///
/// | Color       | x      | y      | X     | Y     | Z     |
/// |-------------|--------|--------|-------|-------|-------|
/// | Red (R)     | 0.640  | 0.330  | 0.436 | 0.222 | 0.014 |
/// | Green (G)   | 0.300  | 0.600  | 0.385 | 0.717 | 0.097 |
/// | Blue (B)    | 0.150  | 0.060  | 0.143 | 0.061 | 0.714 |
/// | White (D65) | 0.3127 | 0.3290 | 0.950 | 1.000 | 1.089 |
/// | Black       |        |        | 0.000 | 0.000 | 0.000 |
///
/// Conceptual Gamma correction performed by the transfer function:
///
/// ```
/// opto-electronic transfer gamma ~ 0.45
/// electro-optical transfer gamma ~ 1/0.45 = 2.222
/// ```
///
/// Transfer function: `linearToCurved(L:float) -> V:float`
/// The transfer function performs the opto-electronic transfer function (OETF).
/// The OETF converts linear RGB values (`ER`, `EG`, `EB`) to gamma corrected RGB
/// values (`EpR`, `EpG`, `EpB`).
///
/// ```
/// V = 1.099 * L^0.45 - 0.099  for L >= 0.018
/// V = 4.500 * L               for L < 0.018
///
/// V = 16.0/255.0 + V * (219.0/255.0)
/// ```
///
/// Derivation of luminance signal `EpY`:
///
/// ```
/// EpY = 0.2126 * EpR + 0.7152 * EpG + 0.0722 * EpR
/// ```
///
/// Derivation of color difference signal (analogue coding):
///
/// ```
/// EpCB = ( EpB - EpY )/ 1.8556
///      = ( -0.2126 * EpR - 0.7152 * EpG + 0.9278 EpB )/ 1.8556
///
/// EpCR = ( EpR - EpY )/ 1.5748
///      = ( -0.7874 * EpR - 0.7152 * EpG + 0.0722 EpB )/ 1.5748
/// ```
///
/// ITU. Recommendation BT.709 (06/2025)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.709/en)
///
/// CSS Color Module Level 4. Sample code for Color Conversions
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#color-conversion-code)
///
/// Wikipedia: BT.709-6
/// : [wikipedia](https://en.wikipedia.org/wiki/Rec._709)
public class RecBT709ColorSpace {


    public static ParametricNonLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricNonLinearRgbColorSpace INSTANCE = new ParametricNonLinearRgbColorSpace(
                    "Rec. ITU-R BT.709-6", new ParametricLinearRgbColorSpace("Linear BT.709-6",
                    new Point2D(0.640, 0.330),
                    new Point2D(0.3, 0.6),
                    new Point2D(0.15, 0.06),
                    ILLUMINANT_D65_XYZ, -1
            ),
                    new ParametricToneMapper(2.22222f, 0.9099121f, 0.09008789f, 0.22222f, 0.08099365f), -1
            );
        }
        return Holder.INSTANCE;
    }

}
