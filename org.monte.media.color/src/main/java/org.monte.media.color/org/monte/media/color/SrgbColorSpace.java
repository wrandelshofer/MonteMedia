/*
 * @(#)SrgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


/// The `sRGB` color space.
///
/// sRGB is a standard RGB (red, green, blue) color space that HP and Microsoft created cooperatively in 1996 to use
/// on monitors, printers, and the World Wide Web.
/// It uses the same primaries as Rec.709 but uses a different gamma curve.
/// sRGB is intended for use on a computer screen in a bright environment,
/// whereas Rec.709 is inteded for use on a TV screen in a dark environment.
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
/// gamma ~ 0.416667
/// ```
///
/// Transfer function `fromLinear(cl:float) -> c:float`
/// `cl` is the linear-light red, green, or blue component.
/// `c` is the corresponding gamma-encoded component.
///
/// ```javascript
/// let sign = cl < 0? -1 : 1;
/// let abs = Math.abs(cl);
///
/// if (abs > 0.0031308){
///    c = sign * (1.055 * Math.pow(abs, 1/2.4) - 0.055);
/// } else {
///    c = 12.92 * cl;
/// }
/// ```
///
///
/// Transfer function `toLinear(c:float) -> cl:float`
/// `c` is the gamma-encoded red, green or blue component.
/// `cl` is the corresponding linear-light component.
///
/// ```javascript
/// let sign = c < 0? -1 : 1;
/// let abs = Math.abs(c);
///
/// if (abs <= 0.04045){
///   cl = c / 12.92;
/// } else {
///   cl = sign * (Math.pow((abs + 0.055)/ 1.055, 2.4));
/// }
/// ```
///
///
/// Wikipedia: sRGB
/// : [wikipedia](https://en.wikipedia.org/wiki/SRG)
///
/// ITU. Recommendation BT.709 (06/2025)
/// : [itu.int](https://www.itu.int/rec/R-REC-BT.709/en)
///
/// CSS Color Module Level 4. The Predefined sRGB Color Space: the sRGB keyword
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#predefined-sRGB)
///
/// CSS Color Module Level 4. The Predefined Linear-light sRGB Color Space: the srgb-linear keyword
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#predefined-sRGB-linear)
///
/// CSS Color Module Level 4. 18. Sample code for Color Conversions
/// : [w3.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#color-conversion-code)
public class SrgbColorSpace extends ParametricNonLinearRgbColorSpace {
    public static SrgbColorSpace getInstance() {
        class Holder {
            private static final SrgbColorSpace INSTANCE = new SrgbColorSpace();
        }
        return Holder.INSTANCE;
    }


    public SrgbColorSpace() {
        super("sRGB", new LinearSrgbColorSpace(),
                SrgbColorSpace::toLinear,
                SrgbColorSpace::fromLinear
        );
    }

    @Override
    public boolean isCS_sRGB() {
        return true;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        System.arraycopy(colorvalue, 0, rgb, 0, 3);
        return rgb;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        System.arraycopy(rgb, 0, colorvalue, 0, 3);
        return colorvalue;
    }

    /// Inverse "Gamma" transfer function.
    public static float fromLinear(float linear) {
        return LinearSrgbColorSpace.fromLinear(linear);
    }

    /// "Gamma" transfer function.
    public static float toLinear(float corrected) {
        return LinearSrgbColorSpace.toLinear(corrected);
    }
}
