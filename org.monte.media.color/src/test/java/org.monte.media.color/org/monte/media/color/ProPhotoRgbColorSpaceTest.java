/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Double;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ProPhotoRgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected ParametricNonLinearRgbColorSpace getInstance() {
        return ProPhotoRgbColorSpace.getInstance();
    }

    /**
     * References:
     * <dl>
     *     <dt>CSS Color Module Level 4. The Predefined ProPhoto RGB Color Space: the prophoto-rgb keyword.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-prophoto-rgb">w3.org</a></dd>
     *
     *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
     *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
     * </dl>
     */
    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = (ParametricLinearRgbColorSpace) getInstance().getLinearColorSpace();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3 expected =
                new Matrix3Double(
                        0.7977604896723027, 0.13518583717574031, 0.0313493495815248,
                        0.2880711282292934, 0.7118432178101014, 0.00008565396060525902,
                        0.0, 0.0, 0.8251046025104601);
        assertArrayEquals(expected.toDoubleArray(), actual.toDoubleArray(), EPSILON);
        Matrix3 actualInverse = instance.getToXyzMatrix().inv();
        Matrix3 expectedInverse = new Matrix3Double(
                1.3457989731028281, -0.25558010007997534, -0.05110628506753401,
                -0.5446224939028347, 1.5082327413132781, 0.02053603239147973,
                0.0, 0.0, 1.2119675456389454
        );
        assertArrayEquals(expectedInverse.toDoubleArray(), actualInverse.toDoubleArray(), EPSILON);
    }
}