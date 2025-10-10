package org.monte.media.impl.jcodec.codecs.h264.io.model;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
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
