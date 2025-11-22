/*
 * @(#)Rec2020ColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Double;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Rec2020ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected Rec2020ColorSpace getInstance() {
        return new Rec2020ColorSpace();
    }

    /// References:
    /// <dl>
    ///     <dt>CSS Color Module Level 4.  The Predefined ITU-R BT.2020-2 Color Space: the rec2020 keyword.</dt>
    ///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-rec2020)</dd>
    ///     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
    ///     <dd>[w3.org](https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code)</dd>
    /// </dl>
    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = (ParametricLinearRgbColorSpace) getInstance().getLinearColorSpace();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3 expected = ParametricLinearRgbColorSpace.FROM_D65_TO_D50
                .mul(new Matrix3Double(
                        63426534 / 99577255d, 20160776 / 139408157d, 47086771 / 278816314d,
                        26158966 / 99577255d, 472592308 / 697040785d, 8267143 / 139408157d,
                        0 / 1d, 19567812 / 697040785d, 295819943 / 278816314d
                ));
        assertArrayEquals(expected.toDoubleArray(), actual.toDoubleArray(), 1e-3);
        Matrix3 actualInverse = instance.getToXyzMatrix().inv();
        Matrix3 expectedInverse = new Matrix3Double(
                30757411 / 17917100d, -6372589 / 17917100d, -4539589 / 17917100d,
                -19765991 / 29648200d, 47925759 / 29648200d, 467509 / 29648200d,
                792561 / 44930125d, -1921689 / 44930125d, 42328811 / 44930125d
        ).mul(ParametricLinearRgbColorSpace.FROM_D50_XYZ_TO_D65_XYZ);
        assertArrayEquals(expectedInverse.toDoubleArray(), actualInverse.toDoubleArray(), 1e-3);
    }
}