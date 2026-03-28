/*
 * @(#)KMeansColorQuantizer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.quant;

import org.monte.media.color.NamedColorSpace;
import org.monte.media.color.OKLabColorSpace;
import org.monte.media.color.kmeans.DistanceMatrix;
import org.monte.media.color.kmeans.Simple3DDistanceMatrix;
import org.monte.media.color.kmeans.algo.KMeansClusteringAlgo;
import org.monte.media.color.kmeans.algo.KMeansInitAlgo;
import org.monte.media.color.kmeans.algo.LabHueKMeansInitAlgo;
import org.monte.media.color.kmeans.algo.Simple3DKMeansClusteringAlgo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/// Estimates a color palette using a K-Means clustering algorithm.
///
/// References:
/// <dl>
/// <dt>M. Emre Celebi. Department of Computer Science.
///       Louisiana State University, Shreveport, LA, USA (2024).
///        Improving the Performance of K-Means for Color Quantization.</dt>
///  <dd>[harvard.edu](https://ui.adsabs.harvard.edu/abs/2011arXiv1101.0395E/abstract)
///
/// [arxiv.org](https://arxiv.org/pdf/1101.0395)</a></dd>
/// </dl>
public class KMeansColorQuantizer implements ColorQuantizer {
    private final int K;
    private final OKLabColorSpace cs = new OKLabColorSpace();
    private final List<float[]> samples = new ArrayList<>();
    private final List<Float> weights = new ArrayList<>();
    ///  key = rgb, value = index into array samples and weights
    private final HashMap<Integer, Integer> done = new HashMap<>();
    private final KMeansInitAlgo initAlgo;
    private float[][] C;
    private DistanceMatrix quantDmx;
    private int quantGuess;
    private float[] quantOklab;
    private float[] quantSrgb;


    public KMeansColorQuantizer(int k) {
        this(k, new LabHueKMeansInitAlgo());
    }

    public KMeansColorQuantizer(int k, KMeansInitAlgo initAlgo) {
        K = k;
        this.initAlgo = initAlgo;
    }

    record ColorData(float[][] X, float[] xWeights) {
    }

    public void addImage(BufferedImage image) {
        if (image == null) return;

        ColorData colorData = getColorDataOKLab(image);
        for (float[] x : colorData.X) {
            samples.add(x);
        }
        for (float xWeight : colorData.xWeights) {
            weights.add(xWeight);
        }

    }

    public IndexColorModel computeColorPalette() {
        Simple3DKMeansClusteringAlgo algo = new Simple3DKMeansClusteringAlgo();

        float[][] X = new float[samples.size()][0];
        float[] xWeights = new float[samples.size()];
        for (int i = 0, n = samples.size(); i < n; i++) {
            X[i] = samples.get(i);
        }
        for (int i = 0, n = weights.size(); i < n; i++) {
            xWeights[i] = weights.get(i);
        }
        KMeansClusteringAlgo.KMeans kMeans = algo.computeKMeans(X, xWeights, K, 256, initAlgo);

        C = kMeans.C();
        return toColorPalette(C, cs);
    }

    @Override
    public int quant(int rgb) {
        if (quantDmx == null) {
            quantDmx = new Simple3DDistanceMatrix(K);
            quantDmx.updateMatrix(C);
            quantOklab = new float[3];
            quantSrgb = new float[3];
        }

        quantSrgb[0] = ((rgb & 0xff0000) >>> 16) / 255f;
        quantSrgb[1] = ((rgb & 0xff00) >>> 8) / 255f;
        quantSrgb[2] = ((rgb & 0xff)) / 255f;
        cs.fromRGB(quantSrgb, quantOklab);
        quantGuess = quantDmx.findNearestCluster(quantOklab, quantGuess);
        return quantGuess;
    }

    public IndexColorModel toColorPalette(float[][] C, NamedColorSpace cs) {
        int[] cmap = new int[C.length];
        var colorSpace = cs;
        float[] oklab = new float[3];
        float[] srgb = new float[3];
        for (int i = 0; i < C.length; i++) {
            oklab[0] = (float) C[i][0];
            oklab[1] = (float) C[i][1];
            oklab[2] = (float) C[i][2];
            colorSpace.toRGB(oklab, srgb);
            cmap[i] = 0xff000000
                    | ((int) Math.clamp(srgb[0] * 255, 0, 255) << 16)
                    | ((int) Math.clamp(srgb[1] * 255, 0, 255) << 8)
                    | (int) Math.clamp(srgb[2] * 255, 0, 255);
        }
        return new IndexColorModel(8, C.length, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
    }

    private ColorData getColorDataOKLab(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        List<float[]> xList = new ArrayList<>();
        List<Float> weightList = new ArrayList<>();
        float[] srgb = new float[3];
        float[] oklab = new float[3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Integer index = done.get(rgb);
                if (index == null) {
                    index = done.size();
                    done.put(rgb, index);
                    int r = (rgb & 0xff0000) >> 16;
                    int g = (rgb & 0x00ff00) >> 8;
                    int b = (rgb & 0xff);

                    srgb[0] = r * (1 / 255f);
                    srgb[1] = g * (1 / 255f);
                    srgb[2] = b * (1 / 255f);
                    cs.fromRGB(srgb, oklab);
                    xList.add(new float[]{oklab[0], oklab[1], oklab[2]});
                    weightList.add(1.0f);
                } else {
                    weightList.set(index, weightList.get(index) + 1.0f);
                }
            }
        }
        float[][] X = new float[xList.size()][0];
        float[] xWeights = new float[xList.size()];
        for (int i = 0; i < X.length; i++) {
            X[i] = xList.get(i);
            xWeights[i] = weightList.get(i);
        }
        return new ColorData(X, xWeights);
    }
}
