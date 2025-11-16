/*
 * @(#)D65XyzColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import static org.monte.media.color.ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ;
import static org.monte.media.color.ParametricLinearRgbColorSpace.FROM_D65_TO_D50;

/**
 * XYZ Color Space with D50 white point.
 * <p>
 * The xyz color space accepts three numeric parameters, representing the X,Y and Z values.
 * <dl>
 *     <dt>CSS Color Module Level 4. The Predefined CIE XYZ Color Spaces: the xyz-d50, xyz-d65, and xyz keywords.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-xyz">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class D65XyzColorSpace extends ParametricXyzColorSpace {
    public final static D65XyzColorSpace INSTANCE = new D65XyzColorSpace();

    public D65XyzColorSpace() {
        super("D65 XYZ",
                FROM_D65_TO_D50.toFloat(),
                FROM_D50_XYZ_TO_D65_XYZ.toFloat()
        );

    }
}
