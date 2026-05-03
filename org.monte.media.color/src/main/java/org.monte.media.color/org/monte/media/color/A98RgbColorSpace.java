/*
 * @(#)A98RgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.tonecurve.GammaToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// A98 RGB Color Space.
///
/// The Adobe RGB (1998) color space or opRGB is a color space developed by Adobe Inc. in 1998.
/// It was designed to encompass most of the colors achievable on CMYK color printers, but by using RGB primary colors on
/// a device such as a computer display.
///
/// A gamma of 563/256, or 2.19921875, is used, without the linear segment near zero that is present in sRGB.
///
/// References:
///
/// Wikipedia: Adobe RGB color space
/// [wikipedia](https://en.wikipedia.org/wiki/Adobe_RGB_color_space)
///
/// CSS Color Module Level 4. The Predefined A98 RGB Color Space: the a98-rgb keyword.
/// [w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-a98-rgb3)
public class A98RgbColorSpace {
    public static ParametricNonLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricNonLinearRgbColorSpace INSTANCE = new ParametricNonLinearRgbColorSpace(
                    "A98 RGB", new ParametricLinearRgbColorSpace("Linear A98 RGB",
                    new Point2D(0.64, 0.33),
                    new Point2D(0.21, 0.71),
                    new Point2D(0.15, 0.06),
                    ILLUMINANT_D65_XYZ, -1
            ), new GammaToneMapper(563 / 256f, 1f, 0f, 1f, 0f), -1);
        }
        return Holder.INSTANCE;
    }
}
