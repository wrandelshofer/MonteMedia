package org.monte.media.impl.jcodec.codecs.h264.decode.aso;

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
 * <p>
 * Contains a mapping of macroblocks to slice groups. Groups is an array of
 * group slice group indices having a dimension picWidthInMbs x picHeightInMbs
 *
 * @author The JCodec project
 */
public class MBToSliceGroupMap {
    private int[] groups;
    private int[] indices;
    private int[][] inverse;

    public MBToSliceGroupMap(int[] groups, int[] indices, int[][] inverse) {
        this.groups = groups;
        this.indices = indices;
        this.inverse = inverse;
    }

    public int[] getGroups() {
        return groups;
    }

    public int[] getIndices() {
        return indices;
    }

    public int[][] getInverse() {
        return inverse;
    }
}
