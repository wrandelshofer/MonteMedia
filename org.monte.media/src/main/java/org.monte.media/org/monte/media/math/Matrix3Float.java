/*
 * @(#)Matrix3Float.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import static java.lang.Math.fma;
import static org.monte.media.math.Scalars.almostEqual;

/**
 * A 3x3 matrix with float precision.
 * <p>
 * This matrix is intended for use in performance-critical code.
 *
 * @param a row 0 column 0
 * @param b row 0 column 1
 * @param c row 0 column 2
 * @param d row 1 column 0
 * @param e row 1 column 1
 * @param f row 1 column 2
 * @param g row 2 column 0
 * @param h row 2 column 1
 * @param i row 2 column 2
 */
public record Matrix3Float(float a, float b, float c,
                           float d, float e, float f,
                           float g, float h, float i) implements Matrix3 {
    public double det() {
        return toDouble().det();
    }

    @Override
    public Matrix3Float toFloat() {
        return this;
    }

    public Matrix3Float transpose() {
        return new Matrix3Float(
                a, d, g,
                b, e, h,
                c, f, i);
    }

    @Override
    public boolean equals(Matrix3 M, double eps) {
        Matrix3Float that = M.toFloat();
        return almostEqual(a, that.a, eps)
                && almostEqual(b, that.b, eps)
                && almostEqual(c, that.c, eps)
                && almostEqual(d, that.d, eps)
                && almostEqual(e, that.e, eps)
                && almostEqual(f, that.f, eps)
                && almostEqual(g, that.g, eps)
                && almostEqual(h, that.h, eps)
                && almostEqual(i, that.i, eps)
                ;
    }

    public double[] toDoubleArray() {
        return new double[]{a, b, c, d, e, f, g, h, i};
    }

    @Override
    public Point3DDouble mul(double a1, double a2, double a3) {
        return new Point3DDouble(
                a * a1 + b * a2 + c * a3,
                d * a1 + e * a2 + f * a3,
                g * a1 + h * a2 + i * a3
        );
    }


    @Override
    public float[] mul(float[] x, float[] y) {
        float x0 = x[0];
        float x1 = x[1];
        float x2 = x[2];
        y[0] = fma(a, x0, fma(b, x1, c * x2));
        y[1] = fma(d, x0, fma(e, x1, f * x2));
        y[2] = fma(g, x0, fma(h, x1, i * x2));
        return y;
    }

    @Override
    public double[] mul(double[] x, double[] y) {
        double x0 = x[0];
        double x1 = x[1];
        double x2 = x[2];
        y[0] = fma(a, x0, fma(b, x1, c * x2));
        y[1] = fma(d, x0, fma(e, x1, f * x2));
        y[2] = fma(g, x0, fma(h, x1, i * x2));
        return y;
    }


    public Matrix3Float mul(Matrix3 M) {
        Matrix3Float that = M.toFloat();
        return new Matrix3Float(
                fma(a, that.a, fma(b, that.d, c * that.g)),
                fma(a, that.b, fma(b, that.e, c * that.h)),
                fma(a, that.c, fma(b, that.f, c * that.i)),
                fma(d, that.a, fma(e, that.d, f * that.g)),
                fma(d, that.b, fma(e, that.e, f * that.h)),
                fma(d, that.c, fma(e, that.f, f * that.i)),
                fma(g, that.a, fma(h, that.d, i * that.g)),
                fma(g, that.b, fma(h, that.e, i * that.h)),
                fma(g, that.c, fma(h, that.f, i * that.i))
        );
    }

    public Matrix3Double toDouble() {
        return new Matrix3Double(a, b, c, d, e, f, g, h, i);
    }

    /**
     * Inverses the matrix. The inversion is performed in double precision.
     * <p>
     * References:<br>
     * <a href="https://www.wikihow.com/Find-the-Inverse-of-a-3x3-Matrix">wikihow.com</a><br>
     * <a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-2x2-matrix/">chilimath.com</a>
     */
    public Matrix3Float inv() {
        return toDouble().inv().toFloat();
    }
}
