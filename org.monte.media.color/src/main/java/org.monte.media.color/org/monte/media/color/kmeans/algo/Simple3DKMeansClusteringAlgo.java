/*
 * @(#)Simple3DKMeansClusteringAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;

import org.monte.media.color.kmeans.Simple3DDistanceMatrix;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.fma;

/// Implements a K-Means clustering algorithm.
/// This implementation only works with 3-dimensional data.
/// ```
/// input : X= {x1, x2, . . . , xN } ∈ D (N × D input data set)
/// output: C= {c1, c2, . . . , cK } ∈ D (K cluster centers)
/// Select a random subset C of X as the initial set of cluster centers;
/// while termination criterion is not met do
/// |  Create a distance matrix M.
/// |  for (i = 1; i ≤ N ; i= i + 1) do
/// |  |  Assign xi to the nearest cluster using the distance matrix M;
/// |  |  m[i] = argmin k∈{1,2,...,K} ∥xi− ck ∥^2 ;
/// |  end
/// |  Recalculate the cluster centers;
/// |  for (k = 1; k ≤ K; k= k + 1) do
/// |  |  Cluster Sk contains the set of points xi that are nearest to the center ck ;
/// |  |  Sk = {xi |m[i] = k };
/// |  |  Calculate the new center ck as the mean of the points that belong to Sk ;
/// |  |  ck = 1/|Sk| ∑ xi∈Sk * xi;
/// |  end
/// end
/// ```
/// References:
/// <dl>
/// <dt>M. Emre Celebi. Department of Computer Science.
///       Louisiana State University, Shreveport, LA, USA (2024).
///        Improving the Performance of K-Means for Color Quantization.</dt>
///  <dd>[harvard.edu](https://ui.adsabs.harvard.edu/abs/2011arXiv1101.0395E/abstract)
///
/// [arxiv.org](https://arxiv.org/pdf/1101.0395)</a></dd>
/// </dl>
public class Simple3DKMeansClusteringAlgo implements KMeansClusteringAlgo {


    /// Compute kMeans.
    ///
    /// @param X             the input data set with `float[N][D]` data elements. N can be arbitrarily large. D must be 3.
    /// @param xWeights      the weights of the data elements
    /// @param K             the number of clusters
    /// @param numIterations the number of iterations
    /// @param initMethod    algorithm for selecting the initial cluster positions
    /// @return C the K cluster centers
    public KMeans computeKMeans(float[][] X, float[] xWeights, int K, int numIterations, KMeansInitAlgo initMethod) {
        int N = X.length;
        int[] assignments = new int[N];
        if (N <= K) {
            for (int i = 0; i < N; i++) assignments[i] = i;
            return new KMeans(X, assignments);
        }
        Simple3DDistanceMatrix M = new Simple3DDistanceMatrix(K);
        if (K <= 0) {
            return new KMeans(new float[0][0], new int[0]);
        }
        float[][] C = new float[K][X[0].length];
        float[] cWeights = new float[C.length];
        initMethod.selectInitialClusterCenters(X, xWeights, K, C);
        boolean changed = true;
        for (int iter = 0; changed && iter < numIterations; iter++) {
            M.updateMatrix(C);
            changed = assignToNearestCluster(X, M, assignments);
            recalculateClusterCenters(X, xWeights, C, cWeights, assignments);
        }
        return new KMeans(C, assignments);
    }

    private void recalculateClusterCenters(float[][] X, float[] xWeights, float[][] C, float[] cWeights, int[] clusterAssignment) {
        for (float[] doubles : C) {
            Arrays.fill(doubles, 0);
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


    private boolean assignToNearestCluster(float[][] X, Simple3DDistanceMatrix M, int[] clusterAssignment) {
        int result = IntStream.range(0, X.length).parallel().map(i -> {
            boolean changed = false;
            //for (int i = 0; i < X.length; i++) {
            int previousAssignment = clusterAssignment[i];
            int c = M.findNearestCluster(X[i], previousAssignment);
            changed |= clusterAssignment[i] != c;
            clusterAssignment[i] = c;
            // }
            return changed ? 1 : 0;
        }).max().orElse(0);
        return result > 0;

    }
}
