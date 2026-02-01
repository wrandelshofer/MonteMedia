/*
 * @(#)CieRgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

/// Linear CIE RGB Color Space.
///
/// Wikipedia: CIE 1931 color space
/// : [wikipedia](https://en.wikipedia.org/wiki/CIE_1931_color_space)
///
public class CieRgbColorSpace extends ParametricLinearRgbColorSpace {
    public static CieRgbColorSpace getInstance() {
        class Holder {
            private static final CieRgbColorSpace INSTANCE = new CieRgbColorSpace();
        }
        return Holder.INSTANCE;
    }


    public CieRgbColorSpace() {
        super("Linear CIE RGB", new Point2D(0.73474284, 0.26525716),
                new Point2D(0.27377903, 0.7174777),
                new Point2D(0.16655563, 0.00891073),
                ILLUMINANT_E_XYZ
        );
    }
}
