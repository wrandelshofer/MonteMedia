/*
 * @(#)LinearSrgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

import java.awt.color.ColorSpace;

/// Linear `sRGB` Color Space.
/// <dl>
///     <dt>Wikipedia: sRGB. Transformation</dt>
///     <dd>[wikipedia](https://en.wikipedia.org/wiki/SRGB#Transformation)</dd>
///     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code)</dd>
/// </dl>
public class LinearSrgbColorSpace {
    public static ParametricLinearRgbColorSpace getInstance() {
        class Holder {
            private static final ParametricLinearRgbColorSpace INSTANCE = new ParametricLinearRgbColorSpace(
                    "sRGB Linear",
                    new Point2D(0.64, 0.33),
                    new Point2D(0.3, 0.6),
                    new Point2D(0.15, 0.06),
                    ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ, ColorSpace.CS_LINEAR_RGB
            );
        }
        return Holder.INSTANCE;
    }


}
