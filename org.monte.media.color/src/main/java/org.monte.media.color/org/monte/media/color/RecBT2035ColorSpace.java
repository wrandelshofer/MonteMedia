/*
 * @(#)Rec709ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.ParametricToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// BT.2035 Color Space.
///
/// The BT.2035 color space is used to present a video that has been encoded
/// with BT.2020 in a dark environment. The BT.2035 color space
/// is used on the Rec.709 encoded data (instead of doing a proper conversion
/// from Rec.709 to XYZ and then to BT.2035).
///
/// Chromatic coordinates:
///
/// | Color       | x      | y      |
/// |-------------|--------|--------|
/// | Red (R)     | 0.708  | 0.292  |
/// | Green (G)   | 0.170  | 0.797  |
/// | Blue (B)    | 0.131  | 0.046  |
/// | White (D65) | 0.3127 | 0.3290 |
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
/// ITU. Recommendation BT.2035 (03/2011)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.2035)
///
/// Wikipedia: BT.2035
/// : [wikipedia](https://en.wikipedia.org/wiki/ITU-R_BT.2035)
public class RecBT2035ColorSpace {


    public static ParametricNonLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricNonLinearRgbColorSpace INSTANCE = new ParametricNonLinearRgbColorSpace(
                    "Rec. ITU-R BT.2035", new ParametricLinearRgbColorSpace("Linear BT.2035",
                    new Point2D(0.708, 0.292),
                    new Point2D(0.170, 0.797),
                    new Point2D(0.131, 0.046),
                    ILLUMINANT_D65_XYZ, -1
            ),
                    new ParametricToneMapper(2.4f), -1
            );
        }
        return Holder.INSTANCE;
    }

}
