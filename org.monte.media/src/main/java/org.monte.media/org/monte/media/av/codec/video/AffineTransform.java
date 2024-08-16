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
     * @param flatMatrix with values { m00 m10 m01 m11 m02 m12 }
     */
    public AffineTransform(double... flatMatrix) {
        this.m = Arrays.copyOf(flatMatrix, 6);
    }

    /**
     * Returns a flat matrix with values { m00 m10 m01 m11 m02 m12 }
     *
     * @return the flat matrix
     */
    public double[] getFlatMatrix() {
        return m.clone();
    }

    public <T> T transform(double x, double y, BiFunction<Double, Double, T> constructor) {
        return constructor.apply(m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]);
    }
}
