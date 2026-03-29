/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Float;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class DisplayP3ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected DisplayP3ColorSpace getInstance() {
        return new DisplayP3ColorSpace();
    }

    /**
     * References:
     * <dl>
     *     <dt>CSS Color Module Level 4. The Predefined Display P3 Color Space: the display-p3 keyword.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-display-p3">w3.org</a></dd>
     *
     *     <dt>CSS Color Module Level 4. The Predefined Linear-light sRGB Color Space: the srgb-linear keyword.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-sRGB-linear">w3.org</a></dd>
     * </dl>
     */
    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = (ParametricLinearRgbColorSpace) getInstance().getLinearColorSpace();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3 expected = ParametricLinearRgbColorSpace.FROM_D65_TO_D50
                .mul(new Matrix3Float(
                        608311 / 1250200f, 189793 / 714400f, 198249 / 1000160f,
                        35783 / 156275f, 247089 / 357200f, 198249 / 2500400f,
                        0 / 1f, 32229 / 714400f, 5220557 / 5000800f));
        assertArrayEquals(expected.toDoubleArray(), actual.toDoubleArray(), 1e-3);
        Matrix3 actualInverse = instance.getToXyzMatrix().inv();
        Matrix3 expectedInverse = new Matrix3Float(
                446124 / 178915f, -333277 / 357830f, -72051 / 178915f,
                -14852 / 17905f, 63121 / 35810f, 423 / 17905f,
                11844 / 330415f, -50337 / 660830f, 316169 / 330415f
        ).mul(ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ);
        assertArrayEquals(expectedInverse.toDoubleArray(), actualInverse.toDoubleArray(), 1e-3);
    }
}