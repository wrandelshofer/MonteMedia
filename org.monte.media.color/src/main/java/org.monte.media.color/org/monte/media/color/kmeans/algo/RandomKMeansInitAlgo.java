/*
 * @(#)RandomKMeansInitAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomKMeansInitAlgo implements KMeansInitAlgo {
    private final Random rng = new Random();

    @Override
    public void selectInitialClusterCenters(float[][] X, float[] xWeights, int K, float[][] C) {
        Set<Integer> distinct = new HashSet<>();
        for (int i = 0; i < K; i++) {
            int xi;
            do {
                xi = rng.nextInt(X.length);
            } while (!distinct.add(xi));
            C[i] = X[xi].clone();
        }
    }
}
