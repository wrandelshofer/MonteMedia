/*
 * @(#)CssColorSpaces.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import java.util.Map;

/**
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. Chapter 4. Representing Colors: the &lt;color&gt; type.</dt>
 *     <dd><a href="https://www.w3.org/TR/2024/CRD-css-color-4-20240213/#typedef-colorspace-params">w3.org</a></dd>
 * </dl>
 */
public class CssColorSpaces {
    /**
     * Don't let anyone instantiate this class.
     */
    private CssColorSpaces() {

    }

    /**
     * Map of CSS color spaces.
     */
    public final static Map<String, NamedColorSpace> COLOR_SPACES;

    static {
        D65XyzColorSpace d65XyzColorSpace = new D65XyzColorSpace();
        COLOR_SPACES = Map.of(
                "srgb", new SrgbColorSpace(),
                "srgb-linear", new LinearSrgbColorSpace(),
                "display-p3", new DisplayP3ColorSpace(),
                "a98-rgb", new A98RgbColorSpace(),
                "prophoto-rgb", new ProPhotoRgbColorSpace(),
                "rec2020", new Rec2020ColorSpace(),
                "xyz", d65XyzColorSpace,
                "xyz-d65", d65XyzColorSpace,
                "xyz-d50", new D50XyzColorSpace()
        );
    }
}
