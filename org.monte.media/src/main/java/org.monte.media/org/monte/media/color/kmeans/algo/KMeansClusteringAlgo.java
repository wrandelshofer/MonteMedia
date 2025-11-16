/*
 * @(#)KMeansClusteringAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;

/**
 * Interface for K-Means clustering algorithm using float coordinate values.
 */
public interface KMeansClusteringAlgo {
    /**
     * Result of the KMeansClusteringAlgo.
     *
     * @param C                  the clusters
     * @param clusterAssignments the cluster assignments of X
     */
    record KMeans(float[][] C, int[] clusterAssignments) {

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
    KMeans computeKMeans(float[][] X, float[] xWeights, int K, int numIterations, KMeansInitAlgo initMethod);

}
