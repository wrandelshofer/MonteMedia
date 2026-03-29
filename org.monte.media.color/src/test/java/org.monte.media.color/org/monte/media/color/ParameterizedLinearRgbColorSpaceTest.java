package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3;
import org.monte.media.math.Matrix3Double;
import org.monte.media.math.Point2D;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * References:
 * <dl>
 *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromacity Coordinates.</dt>
 *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
 * </dl>
 */
public class ParameterizedLinearRgbColorSpaceTest {
    private static final double EPSILON = 0x1p-10;

    @Test
    public void shouldYieldExpectedMatrixForNtscWithIlluminantE() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("NTSC EE",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                ParametricLinearRgbColorSpace.ILLUMINANT_E_XYZ
        );
        Matrix3Double expectedMatrix =
                ParametricLinearRgbColorSpace.computeChromaticAdaptationMatrix(ParametricLinearRgbColorSpace.ILLUMINANT_E_XYZ,
                        ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ).mul(
                        new Matrix3Double(
                                0.6611, 0.1711, 0.1678,
                                0.3256, 0.5785, 0.0959,
                                0, 0.0652, 0.9348
                        ));
        Matrix3 actualMatrix = cs.getToXyzMatrix();
        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), EPSILON);
    }

    @Test
    public void shouldYieldExpectedMatrixForNtscWithIlluminantC() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("NTSC C",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                ParametricLinearRgbColorSpace.ILLUMINANT_C_XYZ
        );
        Matrix3Double expectedMatrix =
                ParametricLinearRgbColorSpace.computeChromaticAdaptationMatrix(ParametricLinearRgbColorSpace.ILLUMINANT_C_XYZ,
                        ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ).mul(
                        new Matrix3Double(
                                0.6070, 0.1734, 0.2006,
                                0.2990, 0.5864, 0.1146,
                                0, 0.0661, 1.1175
                        ));
        Matrix3 actualMatrix = cs.getToXyzMatrix();
        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), EPSILON);
    }

    @Test
    public void shouldYieldExpectedMatrixForsRGbLinearWithIlluminantD65() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("sRGB Linear",
                new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ
        );
        Matrix3Double expectedMatrix =
                ParametricLinearRgbColorSpace.computeChromaticAdaptationMatrix(ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ,
                        ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ).mul(
                        new Matrix3Double(
                                0.4124, 0.3576, 0.1805,
                                0.2126, 0.7152, 0.0722,
                                0.0193, 0.1192, 0.9505
                        ));
        Matrix3 actualMatrix = cs.getToXyzMatrix();

        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), EPSILON);
    }

    /**
     * <a href="http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html">brucelindbloom.com</a>
     */
    @Test
    public void shouldYieldBradfordMatrix() {
        {
            Matrix3Double mD65toD50 = ParametricLinearRgbColorSpace.computeChromaticAdaptationMatrix(
                    ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ,
                    ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ);
            Matrix3Double expectedMatrix = new Matrix3Double(
                    0.9555766, -0.0230393, 0.0631636,
                    -0.0282895, 1.0099416, 0.0210077,
                    0.0122982, -0.0204830, 1.3299098
            );
            Matrix3 actualMatrix = mD65toD50;
            assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), EPSILON);
        }

        {
            Matrix3Double mD65toD50 = ParametricLinearRgbColorSpace.computeChromaticAdaptationMatrix(
                    ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ,
                    ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ);
            Matrix3Double expectedMatrix = new Matrix3Double(
                    1.0478112, 0.0228866, -0.0501270,
                    0.0295424, 0.9904844, -0.0170491,
                    -0.0092345, 0.0150436, 0.7521316
            );
            Matrix3 actualMatrix = mD65toD50;

            assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), EPSILON);
        }
    }

}