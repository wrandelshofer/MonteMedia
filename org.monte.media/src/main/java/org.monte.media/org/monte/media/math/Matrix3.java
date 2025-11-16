/*
 * @(#)Matrix3.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

public interface Matrix3 {
    double det();

    Matrix3Float toFloat();

    Matrix3Double toDouble();


    Matrix3 transpose();


    boolean equals(Matrix3 that, double eps);

    double[] toDoubleArray();

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    Point3DDouble mul(double a1, double a2, double a3);

    /**
     * Vector multiplication.
     * <pre>
     * y0       [x0]
     * y1 = M * [x1]
     * y2       [x2]
     * </pre>
     */
    float[] mul(float[] x, float[] y);

    /**
     * Vector multiplication.
     * <pre>
     * y0       [x0]
     * y1 = M * [x1]
     * y2       [x2]
     * </pre>
     */
    double[] mul(double[] x, double[] y);

    /**
     * Matrix multiplication.
     * <pre>
     * result = this * M*
     * </pre>
     */
    Matrix3 mul(Matrix3 M);

    /**
     * Returns the inverse of the matrix.
     *
     * @throws ArithmeticException if the matrix is not invertible
     */
    Matrix3 inv();

}
