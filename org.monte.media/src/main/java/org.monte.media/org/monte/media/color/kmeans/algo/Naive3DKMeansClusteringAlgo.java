/*
 * @(#)Naive3DKMeansClusteringAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.fma;

/**
 * Implements a K-Means clustering algorithm.
 * <pre>
 * This implementation only works with 3-dimensional data.
 * <pre>
 * input : X= {x1, x2, . . . , xN } ∈ D (N × D input data set)
 * output: C= {c1, c2, . . . , cK } ∈ D (K cluster centers)
 * Select a random subset C of X as the initial set of cluster centers;
 * while termination criterion is not met do
 * |  for (i = 1; i ≤ N ; i= i + 1) do
 * |  |  Assign xi to the nearest cluster;
 * |  |  m[i] = argmin k∈{1,2,...,K} ∥xi− ck ∥^2 ;
 * |  end
 * |  Recalculate the cluster centers;
 * |  for (k = 1; k ≤ K; k= k + 1) do
 * |  |  Cluster Sk contains the set of points xi that are nearest to the center ck ;
 * |  |  Sk = {xi |m[i] = k };
 * |  |  Calculate the new center ck as the mean of the points that belong to Sk ;
 * |  |  ck = 1/|Sk| ∑ xi∈Sk * xi;
 * |  end
 * end
 * </pre>
 * References:
 * <dl>
 * <dt>M. Emre Celebi. Department of Computer Science.
 *       Louisiana State University, Shreveport, LA, USA (2024).
 *        Improving the Performance of K-Means for Color Quantization.</dt>
 *  <dd><a href="https://ui.adsabs.harvard.edu/abs/2011arXiv1101.0395E/abstract">harvard.edu</a>
 *     <br><a href="https://arxiv.org/pdf/1101.0395">arxiv.org</a></a></dd>
 * </dl>
 */
public class Naive3DKMeansClusteringAlgo {
    private Random rng = new Random();


    public record KMeans(float[][] C, int[] clusterAssignments) {

    }

    /**
     * Compute kMeans.
     *
     * @param X             the input data set with float[N][D] data elements. D must be 3.
     * @param xWeights      the weights of the data elements
     * @param K             the number of clusters
     * @param numIterations the number of iterations
     * @param initMethod    algorithm for selecting the initial cluster positions
     * @return C the K cluster centers
     */
    public KMeans computeKMeans(float[][] X, float[] xWeights, int K, int numIterations, KMeansInitAlgo initMethod) {
        int N = X.length;
        int[] assignments = new int[N];
        if (N <= K) {
            for (int i = 0; i < N; i++) assignments[i] = i;
            return new KMeans(X, assignments);
        }
        if (K <= 0) {
            return new KMeans(new float[0][0], new int[0]);
        }
        float[][] C = new float[K][X[0].length];
        float[] cWeights = new float[C.length];
        initMethod.selectInitialClusterCenters(X, xWeights, K, C);
        boolean changed = true;
        for (int iter = 0; changed && iter < numIterations; iter++) {
            changed = assignToNearestCluster(X, C, assignments);
            recalculateClusterCenters(X, xWeights, C, cWeights, assignments);
        }
        return new KMeans(C, assignments);
    }

    private void recalculateClusterCenters(float[][] X, float[] xWeights, float[][] C, float[] cWeights, int[] clusterAssignment) {
        for (float[] floats : C) {
            Arrays.fill(floats, 0);
        }
        Arrays.fill(cWeights, 0.0f);
        for (int i = 0; i < X.length; i++) {
            float[] xi = X[i];
            float xwi = xWeights[i];
            float[] ci = C[clusterAssignment[i]];
            ci[0] = fma(xi[0], xwi, ci[0]);
            ci[1] = fma(xi[1], xwi, ci[1]);
            ci[2] = fma(xi[2], xwi, ci[2]);
            cWeights[clusterAssignment[i]] += xwi;
        }
        for (int i = 0; i < C.length; i++) {
            float[] ci = C[i];
            float cwi = cWeights[i];
            if (cwi != 0) {
                ci[0] = ci[0] / cwi;
                ci[1] = ci[1] / cwi;
                ci[2] = ci[2] / cwi;
            }
        }
    }


    private boolean assignToNearestCluster(float[][] X, float[][] C, int[] clusterAssignment) {
        boolean changed = false;
        for (int i = 0; i < X.length; i++) {
            int c = findNearestCluster(X[i], C);
            changed |= clusterAssignment[i] != c;
            clusterAssignment[i] = c;
        }
        return changed;
    }

    private int findNearestCluster(float[] x, float[][] C) {
        // We use squared distances here, because we only need them to compare them to each other.
        float nearestDistanceSq = Float.POSITIVE_INFINITY;
        int nearestCluster = -1;
        for (int j = 0; j < C.length; j++) {
            float[] ci = C[j];
            float distanceSq = 0L;
            float d0 = ci[0] - x[0];
            float d1 = ci[1] - x[1];
            float d2 = ci[2] - x[2];
            distanceSq += (d0 * d0 + d1 * d1 + d2 * d2);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearestCluster = j;
            }
        }
        if (nearestCluster == -1) {
            System.err.println("COULD NOT FIND nearest cluster for " + Arrays.toString(x));
        }
        return nearestCluster;
    }
}
