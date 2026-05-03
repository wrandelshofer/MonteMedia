/*
 * @(#)Rec2020ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.GammaToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// Recommentation ITU-R BT.2020-2 Color Space.
///
/// The BT.2020-2color space is used for encoding Ultra High Definition, 4k
/// and 8k television.
///
/// BT.2020-2 is intended for recording video in a bright environment.
/// To present such a video in a dark environment, the BT.2035 color space
/// is used on the BT.2020-2 encoded data (instead of doing a proper conversion
/// from BT.2020-2 to XYZ and then to BT.2035).
///
/// In other words, BT.2020-2 is used for the transform from linear to curved
/// (opto-electronic transfer function EOTF). And then later BT.1886 is used
/// to perform the inverse operation/ from curved to linear
/// (electro-optical transfer function OETF).
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
/// gamma ~ 0.45
/// ```
///
/// Transfer function `fromLinear(E:float) -> Ep:float`
/// ```
/// α = 1.09929682680944
/// β = 0.018053968510807
///
/// Ep = 4.5 * E               for 0 <= E < β
/// Ep = α * E^0.45 - (α - 1)  for β <= E <= 1
/// ```
///
/// ITU. Recommendation BT.2020-2 (10/2015)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.2020/recommendation.asp?lang=en&parent=R-REC-BT.2020-2-201510-I)
///
/// CSS Color Module Level 4.  The Predefined ITU-R BT.2020-2 Color Space: the rec2020 keyword
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#predefined-rec2020)
///
/// CSS Color Module Level 4. Sample code for Color Conversions
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#color-conversion-code)
///
/// Wikipedia: Rec. 2020
/// : [wikipedia](https://en.wikipedia.org/wiki/Rec_2020)
public class RecBT2020ColorSpace {
    public static ParametricNonLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricNonLinearRgbColorSpace INSTANCE = new ParametricNonLinearRgbColorSpace(
                    "Rec. ITU-R BT.2020-2", new ParametricLinearRgbColorSpace("Linear Rec. 2020",
                    new Point2D(0.708, 0.292),
                    new Point2D(0.170, 0.797),
                    new Point2D(0.131, 0.046),
                    ILLUMINANT_D65_XYZ, -1
            ),
                    new GammaToneMapper(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f), -1
            );
        }
        return Holder.INSTANCE;
    }
}
