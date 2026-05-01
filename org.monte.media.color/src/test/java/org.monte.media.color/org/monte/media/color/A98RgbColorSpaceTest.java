/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Float;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class A98RgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected ParametricNonLinearRgbColorSpace getInstance() {
        return A98RgbColorSpace.getInstance();
    }

    /**
     * References:
     * <dl>
     *     <dt>CSS Color Module Level 4. The Predefined A98 RGB Color Space: the a98-rgb keyword.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-a98-rgb3">w3.org</a></dd>
     *
     *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
     * </dl>
     */
    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = (ParametricLinearRgbColorSpace) getInstance().getLinearColorSpace();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3 expected = ParametricLinearRgbColorSpace.FROM_D65_TO_D50.mul(
                new Matrix3Float(
                        573536 / 994567f, 263643 / 1420810f, 187206 / 994567f,
                        591459 / 1989134f, 6239551 / 9945670f, 374412 / 4972835f,
                        53769 / 1989134f, 351524 / 4972835f, 4929758 / 4972835f
                ));
        assertArrayEquals(expected.toDoubleArray(), actual.toDoubleArray(), 1e-3);
        Matrix3 actualInverse = instance.getToXyzMatrix().inv();
        Matrix3 expectedInverse = new Matrix3Float(
                1829569 / 896150f, -506331 / 896150f, -308931 / 896150f,
                -851781 / 878810f, 1648619 / 878810f, 36519 / 878810f,
                16779 / 1248040f, -147721 / 1248040f, 1266979 / 1248040f
        ).mul(ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ);
        assertArrayEquals(expectedInverse.toDoubleArray(), actualInverse.toDoubleArray(), 1e-3);
    }
}