/*
 * @(#)D50XyzColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


import java.awt.color.ColorSpace;

/// CIE XYZ Color Space with D50 white point.
///
/// The xyz color space accepts three numeric parameters, representing the X,Y and Z values.
///
/// CSS Color Module Level 4. The Predefined CIE XYZ Color Spaces: the xyz-d50, xyz-d65, and xyz keywords.
/// : [w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-xyz)
///
public class D50XyzColorSpace extends AbstractNamedColorSpace {
    public static D50XyzColorSpace getInstance() {
        class Holder {
            private static final D50XyzColorSpace INSTANCE = new D50XyzColorSpace();
        }
        return Holder.INSTANCE;
    }


    private final static SrgbColorSpace SRGB_COLOR_SPACE = new SrgbColorSpace();

    public D50XyzColorSpace() {
        super(ColorSpace.TYPE_XYZ, 3);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        System.arraycopy(colorvalue, 0, xyz, 0, 3);
        return xyz;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        System.arraycopy(xyz, 0, colorvalue, 0, 3);
        return colorvalue;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return SrgbColorSpace.getInstance().toCIEXYZ(rgb, colorvalue);
    }

    @Override
    public String getName() {
        return "D50 XYZ";
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return SrgbColorSpace.getInstance().fromCIEXYZ(colorvalue, rgb);
    }
}
