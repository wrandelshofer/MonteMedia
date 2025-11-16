/*
 * @(#)Simple3DDistanceMatrix.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans;

import java.util.Arrays;

/**
 * Implements a distance matrix for speeding up the search for the nearest cluster.
 * <pre>
 * Construct a K × K matrix M in which row i is a permutation of 1, 2, . . . , K that
 * represents the clusters in increasing order of distance of their centers from ci;
 * </pre>
 * <pre>
 * |  Let Sp be the cluster that xi was assigned to in the previous iteration;
 * |  p= m[i];
 * |  min dist = prev dist = ∥xi− cp∥2;
 * |  Update the nearest center if necessary;
 * |  for (j = 2; j ≤ K; j= j + 1) do
 * |  |  t= M [p][j];
 * |  |  if d[p][t] ≥ 4 prev dist then
 * |  |  | There can be no other closer center. Stop checking;
 * |  |  break;
 * |  end
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
public class Simple3DDistanceMatrix implements DistanceMatrix {
    private final int K;
    private float[][] C;

    /**
     *
     * @param c        cluster index
     * @param distance squared distance to reference cluster
     */
    record Entry(int c, float distance) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry o) {
            return Double.compare(this.distance, o.distance);
        }
    }

    /**
     * Matrix of squared distances.
     */
    private final Entry[] M;

    public Simple3DDistanceMatrix(int K) {
        this.K = K;
        this.M = new Entry[K * K];

    }

    public void updateMatrix(float[][] C) {
        this.C = C;
        for (int i = 0; i < K; i++) {
            float[] ci = C[i];
            M[i * K + i] = new Entry(i, 0);
            for (int j = i + 1; j < K; j++) {
                float[] cj = C[j];
                float a = ci[0] - cj[0];
                float b = ci[1] - cj[1];
                float c = ci[2] - cj[2];
                float distance = a * a + b * b + c * c;
                M[i * K + j] = new Entry(j, distance);
                M[i + j * K] = new Entry(i, distance);
            }
        }

        for (int i = 0; i < K; i++) {
            Arrays.sort(M, i * K, i * K + K);
        }
    }

    public int findNearestCluster(float[] x, int initialGuess) {
        float minDist, prevDist;
        int result = initialGuess;
        int p = initialGuess;
        float[] cp = C[p];
        float a = cp[0] - x[0];
        float b = cp[1] - x[1];
        float c = cp[2] - x[2];
        minDist = prevDist = a * a + b * b + c * c;
        for (int j = 1; j < K; j++) {
            Entry t = M[p * K + j];
            if (t.distance >= 4 * prevDist) {
                //There can be no other closer center. Stop checking;
                return result;
            }
            float[] ct = C[t.c];
            a = ct[0] - x[0];
            b = ct[1] - x[1];
            c = ct[2] - x[2];
            float dist = a * a + b * b + c * c;
            if (dist <= minDist) {
                //ct is closer to xi than cp
                minDist = dist;
                result = t.c;
            }
        }
        return result;
    }
}
