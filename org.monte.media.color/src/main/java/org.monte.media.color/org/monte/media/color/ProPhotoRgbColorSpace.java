/*
 * @(#)ProPhotoRgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.trc.GammaToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ;

/// ProPhoto RGB Color Space.
///
/// The ProPhoto RGB color space, also known as ROMM RGB (Reference Output Medium Metric), is an output referred RGB
/// color space developed by Kodak. It offers an especially large gamut designed for use with photographic output in
/// mind.
///
/// Transfer curve is gamma 1.8 with a small linear portion.
///
/// References:
/// <dl>
///     <dt>Wikipedia: ProPhoto RGB color space.</dt>
///     <dd>[wikipedia](https://en.wikipedia.org/wiki/ProPhoto_RGB_color_space)</dd>
///     <dt>CSS Color Module Level 4. The Predefined ProPhoto RGB Color Space: the prophoto-rgb keyword.</dt>
///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-prophoto-rgb)</dd>
///     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code)</dd>
/// </dl>
public class ProPhotoRgbColorSpace extends ParametricNonLinearRgbColorSpace {
    public static ProPhotoRgbColorSpace getInstance() {
        class Holder {
            private static final ProPhotoRgbColorSpace INSTANCE = new ProPhotoRgbColorSpace();
        }
        return Holder.INSTANCE;
    }


    public ProPhotoRgbColorSpace() {
        super("ProPhoto RGB", new ParametricLinearRgbColorSpace("Linear ProPhoto RGB",
                        new Point2D(0.734699, 0.265301),
                        new Point2D(0.159597, 0.840403),
                        new Point2D(0.036598, 0.000105),
                        ILLUMINANT_D50_XYZ
                ),
                new GammaToneMapper(1.8f, 1 / 1.8f, 0f, 16f, 1 / 512f)
        );
    }
}
