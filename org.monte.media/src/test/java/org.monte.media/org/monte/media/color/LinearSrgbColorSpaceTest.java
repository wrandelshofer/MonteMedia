/*
 * @(#)LinearSrgbColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Float;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class LinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }

    /// References:
    /// <dl>
    ///     <dt>Wikipedia. sRGB.</dt>
    ///     <dd>[wikipedia](https://en.wikipedia.org/wiki/SRGB)</dd>
    ///     <dt>CSS Color Module Level 4. The Predefined sRGB Color Space: the sRGB keyword.</dt>
    ///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-sRGB)</dd>
    ///     <dt>CSS Color Module Level 4. The Predefined Linear-light sRGB Color Space: the srgb-linear keyword.</dt>
    ///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-sRGB-linear)</dd>
    /// </dl>
    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = getInstance();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3 expected = ParametricLinearRgbColorSpace.FROM_D65_TO_D50
                .mul(new Matrix3Float(
                        506752 / 1228815f, 87881 / 245763f, 12673 / 70218f,
                        87098 / 409605f, 175762 / 245763f, 12673 / 175545f,
                        7918 / 409605f, 87881 / 737289f, 1001167 / 1053270f
                ));
        assertArrayEquals(expected.toDoubleArray(), actual.toDoubleArray(), 1e-3);
        Matrix3 actualInverse = instance.getToXyzMatrix().inv();
        Matrix3 expectedInverse = new Matrix3Float(
                12831 / 3959f, -329 / 214f, -1974 / 3959f,
                -851781 / 878810f, 1648619 / 878810f, 36519 / 878810f,
                705 / 12673f, -2585 / 12673f, 705 / 667f
        ).mul(ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ);
        assertArrayEquals(expectedInverse.toDoubleArray(), actualInverse.toDoubleArray(), 1e-3);
    }
}