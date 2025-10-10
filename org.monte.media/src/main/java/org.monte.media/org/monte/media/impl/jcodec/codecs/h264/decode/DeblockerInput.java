package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Utils;
import org.monte.media.impl.jcodec.codecs.h264.io.model.Frame;
import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;

import static org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet.getPicHeightInMbs;

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
 * Contains an input for deblocking filter
 *
 * @author The JCodec project
 */
public class DeblockerInput {
    public int[][] nCoeff;
    public H264Utils.MvList2D mvs;
    public MBType[] mbTypes;
    public int[][] mbQps;
    public boolean[] tr8x8Used;
    public Frame[][][] refsUsed;
    public SliceHeader[] shs;

    public DeblockerInput(SeqParameterSet activeSps) {
        int picWidthInMbs = activeSps.picWidthInMbsMinus1 + 1;
        int picHeightInMbs = getPicHeightInMbs(activeSps);

        nCoeff = new int[picHeightInMbs << 2][picWidthInMbs << 2];
        mvs = new H264Utils.MvList2D(picWidthInMbs << 2, picHeightInMbs << 2);
        mbTypes = new MBType[picHeightInMbs * picWidthInMbs];
        tr8x8Used = new boolean[picHeightInMbs * picWidthInMbs];
        mbQps = new int[3][picHeightInMbs * picWidthInMbs];
        shs = new SliceHeader[picHeightInMbs * picWidthInMbs];
        refsUsed = new Frame[picHeightInMbs * picWidthInMbs][][];
    }
}
