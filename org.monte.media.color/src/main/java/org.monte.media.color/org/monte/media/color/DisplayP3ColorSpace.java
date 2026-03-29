/*
 * @(#)DisplayP3ColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.color.trc.GammaToneMapper;
import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/// Display P3 Color Space.
///
/// P3 is an RGB color space. DCI-P3 (Digital Cinema Initiative) is used with digital theatrical motion picture
/// distribution. Display P3 is a variant developed by Apple Inc. for wide-gamut displays.
/// <dl>
///     <dt>Wikipedia: DCI-P3.</dt>
///     <dd>[wikipedia](https://en.wikipedia.org/wiki/DCI-P3)</dd>
///     <dt>CSS Color Module Level 4. The Predefined Display P3 Color Space: the display-p3 keyword.</dt>
///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-display-p3)</dd>
///     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code)</dd>
/// </dl>
public class DisplayP3ColorSpace extends ParametricNonLinearRgbColorSpace {
    public static DisplayP3ColorSpace getInstance() {
        class Holder {
            private static final DisplayP3ColorSpace INSTANCE = new DisplayP3ColorSpace();
        }
        return Holder.INSTANCE;
    }


    public DisplayP3ColorSpace() {
        super("Display P3", new ParametricLinearRgbColorSpace("Linear Display P3",
                        new Point2D(0.68, 0.32),
                        new Point2D(0.265, 0.69),
                        new Point2D(0.15, 0.06),
                        ILLUMINANT_D65_XYZ
                ), new GammaToneMapper(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f)
        );
    }
}
