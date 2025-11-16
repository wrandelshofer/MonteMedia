/*
 * @(#)KMeansInitAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;

/**
 * Initialization algorithm for {@link KMeansClusteringAlgoDouble}.
 */
public interface KMeansInitAlgo {
    /**
     * Selects K initial cluster centers given an array of data samples {@code X} and their weights {@code xWeights}.
     *
     * @param X        Array of data samples.
     * @param xWeights weights of the data samples
     * @param K        the number of clusters
     * @param C        Output array of cluster centers
     */
    void selectInitialClusterCenters(float[][] X, float[] xWeights, int K, float[][] C);
}
