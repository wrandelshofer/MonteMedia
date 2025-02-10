package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Const;
import org.monte.media.impl.jcodec.codecs.h264.decode.aso.MapManager;
import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.codecs.h264.io.model.Frame;
import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.IntObjectMap;
import org.monte.media.impl.jcodec.common.logging.Logger;
import org.monte.media.impl.jcodec.common.model.Picture;

import static java.lang.System.arraycopy;
import static org.monte.media.impl.jcodec.codecs.h264.H264Const.PartPred.L0;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.debugPrint;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.B_Direct_16x16;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.P_16x16;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.P_16x8;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.P_8x16;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.P_8x8;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.MBType.P_8x8ref0;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType.P;

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
 * A decoder for an individual slice
 *
 * @author The JCodec project
 */
public class SliceDecoder {

    private Mapper mapper;

    private MBlockDecoderIntra16x16 decoderIntra16x16;
    private MBlockDecoderIntraNxN decoderIntraNxN;
    private MBlockDecoderInter decoderInter;
    private MBlockDecoderInter8x8 decoderInter8x8;
    private MBlockSkipDecoder skipDecoder;
    private MBlockDecoderBDirect decoderBDirect;
    private RefListManager refListManager;
    private MBlockDecoderIPCM decoderIPCM;
    private SliceReader parser;
    private SeqParameterSet activeSps;
    private Frame frameOut;
    private DecoderState decoderState;
    private DeblockerInput di;
    private IntObjectMap<Frame> lRefs;
    private Frame[] sRefs;

    public SliceDecoder(SeqParameterSet activeSps, Frame[] sRefs,
                        IntObjectMap<Frame> lRefs, DeblockerInput di, Frame result) {
        this.di = di;
        this.activeSps = activeSps;
        this.frameOut = result;
        this.sRefs = sRefs;
        this.lRefs = lRefs;
    }

    public void decodeFromReader(SliceReader sliceReader) {

        parser = sliceReader;

        initContext();

        debugPrint("============%d============= ", frameOut.getPOC());

        Frame[][] refList = refListManager.getRefList();

        decodeMacroblocks(refList);
    }

    private void initContext() {

        SliceHeader sh = parser.getSliceHeader();

        decoderState = new DecoderState(sh);
        mapper = new MapManager(sh.sps, sh.pps).getMapper(sh);

        decoderIntra16x16 = new MBlockDecoderIntra16x16(mapper, sh, di, frameOut.getPOC(), decoderState);
        decoderIntraNxN = new MBlockDecoderIntraNxN(mapper, sh, di, frameOut.getPOC(), decoderState);
        decoderInter = new MBlockDecoderInter(mapper, sh, di, frameOut.getPOC(), decoderState);
        decoderBDirect = new MBlockDecoderBDirect(mapper, sh, di, frameOut.getPOC(), decoderState);
        decoderInter8x8 = new MBlockDecoderInter8x8(mapper, decoderBDirect, sh, di, frameOut.getPOC(), decoderState);
        skipDecoder = new MBlockSkipDecoder(mapper, decoderBDirect, sh, di, frameOut.getPOC(), decoderState);
        decoderIPCM = new MBlockDecoderIPCM(mapper, decoderState);

        refListManager = new RefListManager(sh, sRefs, lRefs, frameOut);
    }

    private void decodeMacroblocks(Frame[][] refList) {
        Picture mb = Picture.create(16, 16, activeSps.chromaFormatIdc);
        int mbWidth = activeSps.picWidthInMbsMinus1 + 1;

        MBlock mBlock = new MBlock(activeSps.chromaFormatIdc);
        while (parser.readMacroblock(mBlock)) {
            int mbAddr = mapper.getAddress(mBlock.mbIdx);
            int mbX = mbAddr % mbWidth;
            int mbY = mbAddr / mbWidth;
            decode(mBlock, parser.getSliceHeader().sliceType, mb, refList);
            putMacroblock(frameOut, mb, mbX, mbY);
            di.shs[mbAddr] = parser.getSliceHeader();
            di.refsUsed[mbAddr] = refList;
            fillCoeff(mBlock, mbX, mbY);
            mb.fill(0);
            mBlock.clear();
        }
    }

    private void fillCoeff(MBlock mBlock, int mbX, int mbY) {
        for (int i = 0; i < 16; i++) {
            int blkOffLeft = H264Const.MB_DISP_OFF_LEFT[i];
            int blkOffTop = H264Const.MB_DISP_OFF_TOP[i];
            int blkX = (mbX << 2) + blkOffLeft;
            int blkY = (mbY << 2) + blkOffTop;

            di.nCoeff[blkY][blkX] = mBlock.nCoeff[i];
        }
    }

    public void decode(MBlock mBlock, SliceType sliceType, Picture mb, Frame[][] references) {
        if (mBlock.skipped) {
            skipDecoder.decodeSkip(mBlock, references, mb, sliceType);
        } else if (sliceType == SliceType.I) {
            decodeMBlockI(mBlock, mb);
        } else if (sliceType == SliceType.P) {
            decodeMBlockP(mBlock, mb, references);
        } else {
            decodeMBlockB(mBlock, mb, references);
        }
    }

    private void decodeMBlockI(MBlock mBlock, Picture mb) {
        decodeMBlockIInt(mBlock, mb);
    }

    private void decodeMBlockIInt(MBlock mBlock, Picture mb) {
        if (mBlock.curMbType == MBType.I_NxN) {
            decoderIntraNxN.decode(mBlock, mb);
        } else if (mBlock.curMbType == MBType.I_16x16) {
            decoderIntra16x16.decode(mBlock, mb);
        } else {
            Logger.warn("IPCM macroblock found. Not tested, may cause unpredictable behavior.");
            decoderIPCM.decode(mBlock, mb);
        }
    }

    private void decodeMBlockP(MBlock mBlock, Picture mb, Frame[][] references) {
        if (P_16x16 == mBlock.curMbType) {
            decoderInter.decode16x16(mBlock, mb, references, L0);
        } else if (P_16x8 == mBlock.curMbType) {
            decoderInter.decode16x8(mBlock, mb, references, L0, L0);
        } else if (P_8x16 == mBlock.curMbType) {
            decoderInter.decode8x16(mBlock, mb, references, L0, L0);
        } else if (P_8x8 == mBlock.curMbType) {
            decoderInter8x8.decode(mBlock, references, mb, P, false);
        } else if (P_8x8ref0 == mBlock.curMbType) {
            decoderInter8x8.decode(mBlock, references, mb, P, true);
        } else {
            decodeMBlockIInt(mBlock, mb);
        }
    }

    private void decodeMBlockB(MBlock mBlock, Picture mb, Frame[][] references) {
        if (mBlock.curMbType.isIntra()) {
            decodeMBlockIInt(mBlock, mb);
        } else {
            if (mBlock.curMbType == B_Direct_16x16) {
                decoderBDirect.decode(mBlock, mb, references);
            } else if (mBlock.mbType <= 3) {
                decoderInter.decode16x16(mBlock, mb, references, H264Const.bPredModes[mBlock.mbType][0]);
            } else if (mBlock.mbType == 22) {
                decoderInter8x8.decode(mBlock, references, mb, SliceType.B, false);
            } else if ((mBlock.mbType & 1) == 0) {
                decoderInter.decode16x8(mBlock, mb, references, H264Const.bPredModes[mBlock.mbType][0],
                        H264Const.bPredModes[mBlock.mbType][1]);
            } else {
                decoderInter.decode8x16(mBlock, mb, references, H264Const.bPredModes[mBlock.mbType][0],
                        H264Const.bPredModes[mBlock.mbType][1]);
            }
        }
    }

    private static void putMacroblock(Picture tgt, Picture decoded, final int mbX, final int mbY) {
        byte[] luma = tgt.getPlaneData(0);
        int stride = tgt.getPlaneWidth(0);

        byte[] cb = tgt.getPlaneData(1);
        byte[] cr = tgt.getPlaneData(2);
        int strideChroma = tgt.getPlaneWidth(1);

        int dOff = 0;
        final int mbx16 = mbX * 16;
        final int mby16 = mbY * 16;
        final byte[] decodedY = decoded.getPlaneData(0);
        for (int i = 0; i < 16; i++) {
            arraycopy(decodedY, dOff, luma, (mby16 + i) * stride + mbx16, 16);
            dOff += 16;
        }

        final int mbx8 = mbX * 8;
        final int mby8 = mbY * 8;
        final byte[] decodedCb = decoded.getPlaneData(1);
        final byte[] decodedCr = decoded.getPlaneData(2);
        for (int i = 0; i < 8; i++) {
            int decodePos = i << 3;
            int chromaPos = (mby8 + i) * strideChroma + mbx8;
            arraycopy(decodedCb, decodePos, cb, chromaPos, 8);
            arraycopy(decodedCr, decodePos, cr, chromaPos, 8);
        }
    }
}