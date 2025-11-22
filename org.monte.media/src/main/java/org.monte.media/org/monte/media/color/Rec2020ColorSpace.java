/*
 * @(#)Rec2020ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// Rec. 2020 Color Space.
///
/// The Rec. 2020 color space is used for Ultra High Definition, 4k and 8k television.
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
/// Transfer function `fromLinear(E:float) -> Ep:float`
/// ```
/// α = 1.09929682680944
/// β = 0.018053968510807
///
/// Ep = 4.5 * E               for 0 <= E < β
/// Ep = α * E^0.45 - (α - 1)  for β <= E <= 1
///```
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
public class Rec2020ColorSpace extends ParametricNonLinearRgbColorSpace {
    public static Rec2020ColorSpace getInstance() {
        class Holder {
            private static final Rec2020ColorSpace INSTANCE = new Rec2020ColorSpace();
        }
        return Holder.INSTANCE;
    }


    public Rec2020ColorSpace() {
        super("Rec. 2020", new ParametricLinearRgbColorSpace("Linear Rec. 2020",
                        new Point2D(0.708, 0.292),
                        new Point2D(0.170, 0.797),
                        new Point2D(0.131, 0.046),
                        ILLUMINANT_D65_XYZ
                ),
                Rec2020ColorSpace::toLinear, Rec2020ColorSpace::fromLinear
        );
    }

    /// Convert an array of linear-light rec2020 RGB  in the range 0.0-1.0
    /// to gamma corrected form.
    /// ITU-R BT.2020-2 p.4
    public static float fromLinear(float linear) {
        float α = 1.09929682680944f;
        float β = 0.018053968510807f;

        float sign = Math.signum(linear);
        float abs = Math.abs(linear);

        float c;
        if (abs < β) {
            c = linear * 4.5f;
        } else {
            c = sign * (α * (float) Math.pow(abs, 0.45f) - (α - 1));
        }
        return c;
    }

    /// Convert an array of rec2020 RGB values in the range 0.0 - 1.0
    /// to linear light (un-companded) form.
    /// ITU-R BT.2020-2 p.4
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
