package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Const;
import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.common.model.Picture;

/**
 * A decoder for I16x16 macroblocks.
 * <p>
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
public class MBlockDecoderIntraNxN extends MBlockDecoderBase {
    private Mapper mapper;
    private Intra8x8PredictionBuilder prediction8x8Builder;

    public MBlockDecoderIntraNxN(Mapper mapper, SliceHeader sh, DeblockerInput di, int poc,
                                 DecoderState decoderState) {
        super(sh, di, poc, decoderState);
        this.mapper = mapper;
        this.prediction8x8Builder = new Intra8x8PredictionBuilder();
    }

    public void decode(MBlock mBlock, Picture mb) {

        int mbX = mapper.getMbX(mBlock.mbIdx);
        int mbY = mapper.getMbY(mBlock.mbIdx);

        int mbAddr = mapper.getAddress(mBlock.mbIdx);
        boolean leftAvailable = mapper.leftAvailable(mBlock.mbIdx);
        boolean topAvailable = mapper.topAvailable(mBlock.mbIdx);
        boolean topLeftAvailable = mapper.topLeftAvailable(mBlock.mbIdx);
        boolean topRightAvailable = mapper.topRightAvailable(mBlock.mbIdx);

        if (mBlock.cbpLuma() > 0 || mBlock.cbpChroma() > 0) {
            s.qp = (s.qp + mBlock.mbQPDelta + 52) % 52;
        }
        di.mbQps[0][mbAddr] = s.qp;

        residualLuma(mBlock, leftAvailable, topAvailable, mbX, mbY);

        if (!mBlock.transform8x8Used) {
            for (int bInd = 0; bInd < 16; bInd++) {
                int dInd = H264Const.BLK_DISP_MAP[bInd];
                int blkX = (dInd & 3) << 2;
                int blkY = dInd & ~3;

                boolean trAvailable = ((bInd == 0 || bInd == 1 || bInd == 4) && topAvailable)
                        || (bInd == 5 && topRightAvailable) || bInd == 2 || bInd == 6 || bInd == 8 || bInd == 9 || bInd == 10
                        || bInd == 12 || bInd == 14;

                Intra4x4PredictionBuilder.predictWithMode(mBlock.lumaModes[bInd], mBlock.ac[0][bInd],
                        blkX == 0 ? leftAvailable : true, blkY == 0 ? topAvailable : true, trAvailable, s.leftRow[0],
                        s.topLine[0], s.topLeft[0], (mbX << 4), blkX, blkY, mb.getPlaneData(0));
            }
        } else {
            for (int i = 0; i < 4; i++) {
                int blkX = (i & 1) << 1;
                int blkY = i & 2;

                boolean trAvailable = (i == 0 && topAvailable) || (i == 1 && topRightAvailable) || i == 2;
                boolean tlAvailable = i == 0 ? topLeftAvailable : (i == 1 ? topAvailable : (i == 2 ? leftAvailable
                        : true));

                prediction8x8Builder.predictWithMode(mBlock.lumaModes[i], mBlock.ac[0][i],
                        blkX == 0 ? leftAvailable : true, blkY == 0 ? topAvailable : true, tlAvailable, trAvailable,
                        s.leftRow[0], s.topLine[0], s.topLeft[0], (mbX << 4), blkX << 2, blkY << 2, mb.getPlaneData(0));
            }
        }

        decodeChroma(mBlock, mbX, mbY, leftAvailable, topAvailable, mb, s.qp);

        di.mbTypes[mbAddr] = mBlock.curMbType;
        di.tr8x8Used[mbAddr] = mBlock.transform8x8Used;

        MBlockDecoderUtils.collectChromaPredictors(s, mb, mbX);

        MBlockDecoderUtils.saveMvsIntra(di, mbX, mbY);
        MBlockDecoderUtils.saveVectIntra(s, mapper.getMbX(mBlock.mbIdx));
    }
}
