/*
 * @(#)Rec709ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.ParametricToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// BT.1886 Color Space.
///
/// The BT.1886 color space is used to present a video in a dark environment,
/// that has been encoded with BT.709-6 in a bright environment.
/// The BT.1886 color space is used on the Rec.709 encoded data (instead of
/// doing a proper conversion from Rec.709 to XYZ and then to BT.1886).
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
/// opto-electronic transfer gamma ~ 0.4166667
/// electro-optical transfer gamma ~ 1/0.4166667 = 2.40
/// ```
///
/// Transfer function: `curvedToLinear(V:float) -> L:float`
/// The transfer function performs the electro-optical transfer function (OETF),
/// The EOTF converts non-linear RGB values linear RGB values.
///
/// ```
/// L = a * ( max(V + b, 0))^g
/// Where:
///
/// L: Screen luminance in cd/m2
/// LW: Screen luminance for white
/// LB: Screen luminance for Blakc
/// V: Input video signal (normalized, black at V = 0, to white at V = 1)
/// g: Exponent of power function g = 2.40
/// a: Variable for user gain (legacy "contrast" control)
///    a = ( LW^(1/g) - LB^(1/g) )^g
/// b: Variable for user black level lift (legacy "brightness" control)
///    b = LB^(1/g) / ( LW^(1/g) - LB^(1/g) )
/// ```
///
///
/// ITU. Recommendation BT.1886 (03/2011)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.1886)
///
/// Wikipedia: BT.1886
/// : [wikipedia](https://en.wikipedia.org/wiki/ITU-R_BT.1886)
public class RecBT1886ColorSpace {


    public static ParametricNonLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricNonLinearRgbColorSpace INSTANCE = new ParametricNonLinearRgbColorSpace(
                    "Rec. ITU-R BT.1886", new ParametricLinearRgbColorSpace("Linear BT.1886",
                    new Point2D(0.640, 0.330),
                    new Point2D(0.3, 0.6),
                    new Point2D(0.15, 0.06),
                    ILLUMINANT_D65_XYZ, -1
            ),
                    new ParametricToneMapper(2.4f), -1
            );
        }
        return Holder.INSTANCE;
    }

}
