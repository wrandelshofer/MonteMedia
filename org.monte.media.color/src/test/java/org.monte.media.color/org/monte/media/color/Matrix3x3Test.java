package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.monte.media.math.Matrix3Double;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Matrix3x3Test {
    @Test
    public void shouldInvertMatrix() {
        // identity matrix
        Matrix3Double m = new Matrix3Double(1, 0, 0, 0, 1, 0, 0, 0, 1);
        Matrix3Double actualInv = m.inv();
        assertTrue(m.equals(actualInv, 1e-6), "inversion of identity matrix");

        // example matrix
        m = new Matrix3Double(1, 2, 3, 0, 1, 4, 5, 6, 0);
        Matrix3Double expectedInv = new Matrix3Double(-24, 18, 5, 20, -15, -4, -5, 4, 1);
        actualInv = m.inv();
        assertTrue(expectedInv.equals(actualInv, 1e-6), "inversion of m");

        Matrix3Double actualInvInv = actualInv.inv();
        assertTrue(m.equals(actualInvInv, 1e-6), "inversion of inverse");
    }

    @Test
    public void shouldMultiplyVector() {
        Matrix3Double m;
        float[] actual;
        // identity matrix
        m = new Matrix3Double(1, 0, 0, 0, 1, 0, 0, 0, 1);
        actual = m.mul(new float[]{1, 2, 3}, new float[3]);
        assertArrayEquals(new float[]{1, 2, 3}, actual);

        // example matrix
        m = new Matrix3Double(2, 3, 4, 5, 6, 7, 8, 9, 10);
        actual = m.mul(new float[]{11, 12, 13}, new float[3]);
        assertArrayEquals(new float[]{110, 218, 326}, actual);

    }
}
