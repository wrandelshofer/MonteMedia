/*
 * @(#)LabHueKMeansInitAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.kmeans.algo;


import org.monte.media.math.Angles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/// Sort colors by brightness, and then analyzes the hue in each brightness level.
///
/// This requires that data samples represent colors in a Lab space!
///
/// At each brightness level, we create a histogram with 9 buckets:
/// <pre>
///       -----------------
///     /light\  yellow /    \
///    /green \       /orange\
///    |----- -------- -------|
///    |      |       |       |
///    |green |  avg  | red   |
///    |      |   0   |       |
///    |----- -------- -------|
///    \cyan /        \purple/
///     \   /   blue   \    /
///       -----------------
/// </pre>
public class LabHueKMeansInitAlgo implements KMeansInitAlgo {
    private record Level(float[] avg, DoubleSummaryStatistics saturationStats, float[][] buckets, int[] bucketCount) {
        public Level() {
            this(new float[3], new DoubleSummaryStatistics(), new float[9][3], new int[9]);
        }
    }


    @Override
    public void selectInitialClusterCenters(float[][] X, float[] xWeights, int K, float[][] C) {
        // sort by brightness
        List<float[]> sorted = new ArrayList<>(X.length);
        for (int i = 0; i < X.length; i++) {
            sorted.add(X[i]);
        }
        sorted.sort(Comparator.comparingDouble(a -> a[0]));

        if (X.length <= K || K < 2) {
            for (int i = 0; i < Math.min(X.length, C.length); i++) {
                System.arraycopy(X[i], 0, C[i], 0, 3);
            }
        } else {


            // analyze hue at different brightness levels between the darkest and the lightest color
            int step = Math.max(1, X.length / K);
            Level[] levels = new Level[K];

            // Compute the average color at each level
            for (int l = 0; l < K; l++) {
                Level level = new Level();
                levels[l] = level;
                float[] avg = level.avg;
                int offset = l * step;
                for (int j = 0; j < step; j++) {
                    float[] cxi = sorted.get(offset + j);
                    avg[0] += cxi[0];
                    avg[1] += cxi[1];
                    avg[2] += cxi[2];
                }
                avg[0] /= step;
                avg[1] /= step;
                avg[2] /= step;
            }

            // Compute saturation stats for the level
            for (int l = 0; l < K; l++) {
                Level level = levels[l];
                float[] avg = level.avg;
                DoubleSummaryStatistics stats = level.saturationStats;
                int offset = l * step;
                for (int j = 0; j < step; j++) {
                    float[] cxi = sorted.get(offset + j);
                    float da = cxi[1] - avg[1];
                    float db = cxi[2] - avg[2];
                    float saturation = (da * da + db * db);
                    stats.accept(saturation);
                }
                avg[0] /= step;
                avg[1] /= step;
                avg[2] /= step;
            }

            // Distribute colors into buckets at each level
            float[] maxSaturation = new float[9];
            for (int l = K - 1; l >= 0; l--) {
                Level level = levels[l];
                float[] avg = level.avg;
                float[][] buckets = level.buckets;
                int[] bucketCount = level.bucketCount;
                DoubleSummaryStatistics stats = level.saturationStats;
                float avgSaturation = (float) stats.getAverage();
                int offset = l * step;
                Arrays.fill(maxSaturation, Float.NEGATIVE_INFINITY);
                maxSaturation[0] = Float.POSITIVE_INFINITY;
                for (int j = 0; j < step; j++) {
                    float[] cxi = sorted.get(offset + j);
                    float da = cxi[1] - avg[1];
                    float db = cxi[2] - avg[2];
                    float saturation = (da * da + db * db);
                    int bucketIndex;
                    if (saturation > avgSaturation) {
                        float hue = (float) Angles.atan2(da, db);
                        bucketIndex = 1 + (int) (hue * 4 / Math.PI + Math.PI);
                        if (saturation > maxSaturation[bucketIndex]) {
                            System.arraycopy(cxi, 0, buckets[bucketIndex], 0, 3);
                            maxSaturation[bucketIndex] = saturation;
                        }
                    } else {
                        bucketIndex = 0;
                        if (saturation < maxSaturation[bucketIndex]) {
                            System.arraycopy(cxi, 0, buckets[bucketIndex], 0, 3);
                            maxSaturation[bucketIndex] = saturation;
                        }
                    }
                    bucketCount[bucketIndex]++;
                }
                avg[0] /= step;
                avg[1] /= step;
                avg[2] /= step;
            }

            // For each brightness level assign the largest bucket to C, avoid already used hues
            int index = 0;
            boolean[] usedHues = new boolean[9];
            for (int l = 0; l < K; l++) {
                int maxCount = 0;
                Level level = levels[l];
                int p = -1;
                for (int b = 0; b < 9; b++) {
                    if (!usedHues[b] && level.bucketCount[b] > maxCount) {
                        maxCount = level.bucketCount[b];
                        p = b;
                    }
                }
                if (p == -1) {
                    Arrays.fill(usedHues, false);
                    for (int b = 0; b < 9; b++) {
                        if (!usedHues[b] && level.bucketCount[b] > maxCount) {
                            maxCount = level.bucketCount[b];
                            p = b;
                        }
                    }
                }
                if (p != -1) {
                    usedHues[p] = true;
                    System.arraycopy(levels[l].buckets[p], 0, C[index++], 0, 3);
                }
            }

        }

    }
}
