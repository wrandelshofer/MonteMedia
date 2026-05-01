/*
 * @(#)CssColorSpaces.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import java.util.Map;

/// References:
///
/// CSS Color Module Level 4. Chapter 4. Representing Colors: the &lt;color&gt; type.
/// : [w3.org](https://www.w3.org/TR/2024/CRD-css-color-4-20240213/#typedef-colorspace-params)
///
public class CssColorSpaces {
    /// Don't let anyone instantiate this class.
    private CssColorSpaces() {

    }

    /// Map of CSS color spaces.
    public final static Map<String, NamedColorSpace> COLOR_SPACES;

    static {
        D65XyzColorSpace d65XyzColorSpace = new D65XyzColorSpace();
        COLOR_SPACES = Map.of(
                "srgb", SrgbColorSpace.getInstance(),
                "srgb-linear", LinearSrgbColorSpace.getInstance(),
                "display-p3", DisplayP3ColorSpace.getInstance(),
                "a98-rgb", A98RgbColorSpace.getInstance(),
                "prophoto-rgb", ProPhotoRgbColorSpace.getInstance(),
                "rec2020", Rec2020ColorSpace.getInstance(),
                "xyz", d65XyzColorSpace,
                "xyz-d65", d65XyzColorSpace,
                "xyz-d50", new D50XyzColorSpace()
        );
    }
}
