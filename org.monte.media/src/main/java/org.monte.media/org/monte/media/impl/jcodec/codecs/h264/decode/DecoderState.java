package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Utils;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.common.model.ColorSpace;

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
 * Current state of the decoder, this data is accessed from many methods
 *
 * @author The JCodec project
 */
public class DecoderState {
    public int[] chromaQpOffset;
    public int qp;
    public byte[][] leftRow;
    public byte[][] topLine;
    public byte[][] topLeft;

    ColorSpace chromaFormat;

    H264Utils.MvList mvTop;
    H264Utils.MvList mvLeft;
    H264Utils.MvList mvTopLeft;

    public DecoderState(SliceHeader sh) {
        int mbWidth = sh.sps.picWidthInMbsMinus1 + 1;
        chromaQpOffset = new int[]{sh.pps.chromaQpIndexOffset,
                sh.pps.extended != null ? sh.pps.extended.secondChromaQpIndexOffset : sh.pps.chromaQpIndexOffset};

        chromaFormat = sh.sps.chromaFormatIdc;

        mvTop = new H264Utils.MvList((mbWidth << 2) + 1);
        mvLeft = new H264Utils.MvList(4);
        mvTopLeft = new H264Utils.MvList(1);

        leftRow = new byte[3][16];
        topLeft = new byte[3][4];
        topLine = new byte[3][mbWidth << 4];

        qp = sh.pps.picInitQpMinus26 + 26 + sh.sliceQpDelta;
    }
}
