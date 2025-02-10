package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.common.model.Picture;

import static org.monte.media.impl.jcodec.codecs.h264.decode.CoeffTransformer.reorderDC4x4;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.collectPredictors;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.saveMvsIntra;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.saveVectIntra;

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
public class MBlockDecoderIntra16x16 extends MBlockDecoderBase {

    private Mapper mapper;

    public MBlockDecoderIntra16x16(Mapper mapper, SliceHeader sh, DeblockerInput di, int poc,
                                   DecoderState decoderState) {
        super(sh, di, poc, decoderState);
        this.mapper = mapper;
    }

    public void decode(MBlock mBlock, Picture mb) {
        int mbX = mapper.getMbX(mBlock.mbIdx);
        int mbY = mapper.getMbY(mBlock.mbIdx);
        int address = mapper.getAddress(mBlock.mbIdx);
        boolean leftAvailable = mapper.leftAvailable(mBlock.mbIdx);
        boolean topAvailable = mapper.topAvailable(mBlock.mbIdx);
        s.qp = (s.qp + mBlock.mbQPDelta + 52) % 52;
        di.mbQps[0][address] = s.qp;

        residualLumaI16x16(mBlock, leftAvailable, topAvailable, mbX, mbY);

        Intra16x16PredictionBuilder.predictWithMode(mBlock.luma16x16Mode, mBlock.ac[0], leftAvailable, topAvailable,
                s.leftRow[0], s.topLine[0], s.topLeft[0], mbX << 4, mb.getPlaneData(0));

        decodeChroma(mBlock, mbX, mbY, leftAvailable, topAvailable, mb, s.qp);
        di.mbTypes[address] = mBlock.curMbType;

        collectPredictors(s, mb, mbX);
        saveMvsIntra(di, mbX, mbY);
        saveVectIntra(s, mapper.getMbX(mBlock.mbIdx));
    }

    private void residualLumaI16x16(MBlock mBlock, boolean leftAvailable, boolean topAvailable, int mbX, int mbY) {
        CoeffTransformer.invDC4x4(mBlock.dc);
        int[] scalingList = getScalingList(0);
        CoeffTransformer.dequantizeDC4x4(mBlock.dc, s.qp, scalingList);
        reorderDC4x4(mBlock.dc);

        for (int bInd = 0; bInd < 16; bInd++) {
            int ind8x8 = bInd >> 2;
            int mask = 1 << ind8x8;
            if ((mBlock.cbpLuma() & mask) != 0) {
                CoeffTransformer.dequantizeAC(mBlock.ac[0][bInd], s.qp, scalingList);
            }
            mBlock.ac[0][bInd][0] = mBlock.dc[bInd];
            CoeffTransformer.idct4x4(mBlock.ac[0][bInd]);
        }
    }
}