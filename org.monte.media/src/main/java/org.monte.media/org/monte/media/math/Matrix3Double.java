/*
 * @(#)Matrix3Double.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import static java.lang.Math.fma;
import static org.monte.media.math.Scalars.almostEqual;

/**
 * A 3x3 matrix with double precision.
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
public record Matrix3Double(double a, double b, double c,
                            double d, double e, double f,
                            double g, double h, double i) implements Matrix3 {
    public double det() {
        return det(
                a, b, c,
                d, e, f,
                g, h, i);
    }

    public Matrix3Float toFloat() {
        return new Matrix3Float((float) a, (float) b, (float) c, (float) d, (float) e, (float) f, (float) g, (float) h, (float) i);
    }

    @Override
    public Matrix3Double toDouble() {
        return this;
    }


    public Matrix3Double transpose() {
        return new Matrix3Double(
                a, d, g,
                b, e, h,
                c, f, i);
    }


    public boolean equals(Matrix3 M, double eps) {
        Matrix3Double that = M.toDouble();
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

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
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
        y[0] = (float) fma(a, x0, fma(b, x1, c * x2));
        y[1] = (float) fma(d, x0, fma(e, x1, f * x2));
        y[2] = (float) fma(g, x0, fma(h, x1, i * x2));
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

    @Override
    public Matrix3Double mul(Matrix3 M) {
        Matrix3Double that = M.toDouble();
        return new Matrix3Double(
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

    /**
     * References:
     * <dl>
     *     <dt>Mario Banuelos. wikiHow. How to Find the Inverse of a 3x3 Matrix.</dt>
     *     <dd><a href="https://www.wikihow.com/Find-the-Inverse-of-a-3x3-Matrix">wikihow.com</a></dd>
     *
     *     <dt>Chilimath. Determinant of a 2×2 Matrix.</dt>
     *     <dd><a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-2x2-matrix/">chilimath.com</a></dd>
     *
     * </dl>
     */
    public Matrix3Double inv() {
        double invdet = 1.0 / det();
        double ad = det(e, f, h, i);
        double bd = det(d, f, g, i);
        double cd = det(d, e, g, h);
        double dd = det(b, c, h, i);
        double ed = det(a, c, g, i);
        double fd = det(a, b, g, h);
        double gd = det(b, c, e, f);
        double hd = det(a, c, d, f);
        double id = det(a, b, d, e);
        return new Matrix3Double(
                invdet * ad, invdet * -dd, invdet * gd,
                invdet * -bd, invdet * ed, invdet * -hd,
                invdet * cd, invdet * -fd, invdet * id);
    }

    /**
     * References:
     * <dl>
     *     <dt>Chilimath. The Formula of the Determinant of 3×3 Matrix.</dt>
     *     <dd><a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-3x3-matrix/">chilimath.com</a></dd>
     * </dl>
     */
    private double det(double a, double b, double c,
                       double d, double e, double f,
                       double g, double h, double i) {
        return a * det(e, f, h, i)
                - b * det(d, f, g, i)
                + c * det(d, e, g, h);
    }

    /**
     * Computes the determinant of a 2x2 matrix:
     * <pre>
     *    | [a b] |
     *    | [c d] | = a * d - b * c
     * </pre>
     * <p>
     * References:
     * <dl>
     *     <dt>John D. Cook. Accurately computing a 2x2 determinant.</dt>
     *     <dd><a href="https://www.johndcook.com/blog/2020/05/31/kahan-determinant/">johndcook.com</a></dd>
     * </dl>
     */
    private double det(double a, double b, double c, double d) {
        double w = b * c;
        double e = fma(-b, c, w);
        double f = fma(a, d, -w);
        return (f + e);
    }
}
