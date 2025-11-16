/*
 * @(#)DistanceMatrixTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DistanceMatrixTest {
    @Test
    public void shouldFindNearestClusterAlongLine() {
        float[][] C = {
                {0, 0, 0},
                {1, 0, 0},
                {2, 0, 0},
        };
        Simple3DDistanceMatrix dm = new Simple3DDistanceMatrix(C.length);
        dm.updateMatrix(C);
        float[][] X = {
                {-5, 0, 0},
                {-0.5f, 0, 0},
                {1, 0, 0},
                {1.25f, 0, 0},
                {1.75f, 0, 0},
                {2, 0, 0},
                {2.5f, 0, 0},
                {5, 0, 0}
        };
        int[] expected = {0, 0, 1, 1, 2, 2, 2, 2};
        int[] actual = new int[X.length];
        for (int initialGuess = 0; initialGuess < C.length; initialGuess++) {
            for (int i = 0; i < X.length; i++) {
                float[] x = X[i];
                int result = dm.findNearestCluster(x, initialGuess);
                actual[i] = result;
            }
            assertArrayEquals(expected, actual, "initial guess=" + initialGuess + " actual=" + Arrays.toString(actual));
        }
    }

}