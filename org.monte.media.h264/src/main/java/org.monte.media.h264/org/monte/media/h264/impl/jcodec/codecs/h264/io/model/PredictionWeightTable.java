/*
 * @(#)PredictionWeightTable.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.impl.jcodec.codecs.h264.io.model;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
/// @author The JCodec project
public class PredictionWeightTable {
    // luma_log2_weight_denom
    public int lumaLog2WeightDenom;
    // chroma_log2_weight_denom
    public int chromaLog2WeightDenom;

    // luma_weight
    public int[][] lumaWeight;
    // chroma_weight
    public int[][][] chromaWeight;

    // luma_offset
    public int[][] lumaOffset;
    // chroma_offset
    public int[][][] chromaOffset;

    public PredictionWeightTable() {
        this.lumaWeight = new int[2][];
        this.chromaWeight = new int[2][][];

        this.lumaOffset = new int[2][];
        this.chromaOffset = new int[2][][];
    }
}
