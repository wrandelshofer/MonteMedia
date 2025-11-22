/*
 * @(#)DistanceMatrix.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans;

/// Interface for a distance matrix for speeding up the search for the nearest cluster.
/// <pre>
/// Construct a K × K matrix M in which row i is a permutation of 1, 2, . . . , K that
/// represents the clusters in increasing order of distance of their centers from ci;
/// </pre>
/// <pre>
/// |  Let Sp be the cluster that xi was assigned to in the previous iteration;
/// |  p= m[i];
/// |  min dist = prev dist = ∥xi− cp∥2;
/// |  Update the nearest center if necessary;
/// |  for (j = 2; j ≤ K; j= j + 1) do
/// |  |  t= M [p][j];
/// |  |  if d[p][t] ≥ 4 prev dist then
/// |  |  | There can be no other closer center. Stop checking;
/// |  |  break;
/// |  end
/// </pre>
/// References:
/// <dl>
/// <dt>M. Emre Celebi. Department of Computer Science.
///       Louisiana State University, Shreveport, LA, USA (2024).
///        Improving the Performance of K-Means for Color Quantization.</dt>
///  <dd>[harvard.edu](https://ui.adsabs.harvard.edu/abs/2011arXiv1101.0395E/abstract)
///
/// [arxiv.org](https://arxiv.org/pdf/1101.0395)</a></dd>
/// </dl>
public interface DistanceMatrix {
    /// Updates the distance matrix with new cluster centers.
    ///
    /// @param C the new cluster center values
    void updateMatrix(float[][] C);

    /// Finds the nearest cluster center to the provided data sample `x`.
    ///
    /// @param x            a data sample
    /// @param initialGuess the initially guessed cluster center
    /// @return the nearest cluster center
    int findNearestCluster(float[] x, int initialGuess);

}
