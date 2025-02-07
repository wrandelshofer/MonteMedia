/*
 * @(#)AffineTransform.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Affine transformation matrix.
 * <pre>
 *       [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
 *       [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
 *       [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
 * </pre>
 */
public class AffineTransform {
    public final static AffineTransform IDENTITY = new AffineTransform(1, 0, 0, 0, 1, 0);

    private final double[] m;

    /**
     * Creates a new instance.
     *
     * @param flatMatrix with values { 0:m00 1:m10 2:m01 3:m11 4:m02 5:m12 }
     */
    public AffineTransform(double... flatMatrix) {
        this.m = Arrays.copyOf(flatMatrix, 6);
    }

    public static AffineTransform translate(double x, double y) {
        return new AffineTransform(1, 0, 0, 1, x, y);
    }

    /**
     * Returns a flat matrix with values { m00 m10 m01 m11 m02 m12 }
     *
     * @return the flat matrix
     */
    public double[] getFlatMatrix() {
        return m.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AffineTransform that = (AffineTransform) o;
        return Arrays.equals(m, that.m);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m);
    }

    public boolean isIdentity() {
        return m[0] == 1 && m[1] == 0 && m[2] == 0 && m[3] == 1 && m[4] == 0 && m[5] == 0;
    }

    public <T> T transform(double x, double y, BiFunction<Double, Double, T> constructor) {
        return constructor.apply(m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]);
    }

    public static AffineTransform createReshapeTransform(double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        double scaleX = dw / sw;
        double scaleY = dh / sh;

        java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
        t.translate(dx - sx, dy - sy);
        if (!Double.isNaN(scaleX) && !Double.isNaN(scaleY)
                && !Double.isInfinite(scaleX) && !Double.isInfinite(scaleY)
                && (scaleX != 1d || scaleY != 1d)) {
            t.translate(-sx, sy);
            t.scale(scaleX, scaleY);
            t.translate(sx, sy);
        }
        double[] flatmatrix = new double[6];
        t.getMatrix(flatmatrix);
        return new AffineTransform(flatmatrix);
    }

    @Override
    public String toString() {
        return "AffineTransform{" +
                Arrays.toString(m) +
                '}';
    }
}
