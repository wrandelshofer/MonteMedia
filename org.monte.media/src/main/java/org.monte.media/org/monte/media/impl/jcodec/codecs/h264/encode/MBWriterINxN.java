package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.H264Const;
import org.monte.media.impl.jcodec.codecs.h264.H264Encoder.NonRdVector;
import org.monte.media.impl.jcodec.codecs.h264.decode.CoeffTransformer;
import org.monte.media.impl.jcodec.codecs.h264.decode.Intra4x4PredictionBuilder;
import org.monte.media.impl.jcodec.codecs.h264.io.CAVLC;
import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.codecs.h264.io.write.CAVLCWriter;
import org.monte.media.impl.jcodec.common.io.BitWriter;
import org.monte.media.impl.jcodec.common.model.Picture;

import static org.monte.media.impl.jcodec.codecs.h264.H264Const.BLK_DISP_MAP;
import static org.monte.media.impl.jcodec.common.tools.MathUtil.clip;

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
 * Encodes macroblock as I16x16
 *
 * @author Stanislav Vitvitskyy
 */
public class MBWriterINxN {
    public void encodeMacroblock(EncodingContext ctx, Picture pic, int mbX, int mbY, BitWriter out, EncodedMB outMB,
                                 int qp, NonRdVector params) {
        for (int bInd = 0; bInd < 16; bInd++) {
            int blkX = H264Const.MB_DISP_OFF_LEFT[bInd];
            int blkY = H264Const.MB_DISP_OFF_TOP[bInd];
            writePredictionI4x4Block(out, mbX > 0, mbY > 0, ctx.leftMBType, ctx.topMBType[mbX], blkX, blkY, mbX,
                    ctx.i4x4PredTop, ctx.i4x4PredLeft, params.lumaPred4x4[bInd]);
        }

        int[][] coeff = new int[16][16];
        int cbpLuma = lumaAnal(ctx, pic, mbX, mbY, out, qp, outMB, params.lumaPred4x4, coeff);
        int cbpChroma = 2;
        int cbp = cbpLuma | (cbpChroma << 4);
        CAVLCWriter.writeUE(out, params.chrPred);
        CAVLCWriter.writeUE(out, H264Const.CODED_BLOCK_PATTERN_INTRA_COLOR_INV[cbp]);
        if (cbp != 0)
            CAVLCWriter.writeSE(out, qp - ctx.prevQp); // MB QP delta

        outMB.setType(MBType.I_NxN);
        outMB.setQp(qp);

        lumaCode(ctx, pic, mbX, mbY, out, qp, outMB, params.lumaPred4x4, coeff, cbpLuma);
        MBWriterI16x16.chroma(ctx, pic, mbX, mbY, MBType.I_NxN, out, qp, outMB.getPixels(), params.chrPred);
        ctx.prevQp = qp;
    }

    private void writePredictionI4x4Block(BitWriter out, boolean leftAvailable, boolean topAvailable, MBType leftMBType,
                                          MBType topMBType, int blkX, int blkY, int mbX, int[] i4x4PredTop, int[] i4x4PredLeft, int mode) {
        int predMode = 2;
        if ((leftAvailable || blkX > 0) && (topAvailable || blkY > 0)) {
            int predModeB = topMBType == MBType.I_NxN || blkY > 0 ? i4x4PredTop[(mbX << 2) + blkX] : 2;
            int predModeA = leftMBType == MBType.I_NxN || blkX > 0 ? i4x4PredLeft[blkY] : 2;
            predMode = Math.min(predModeB, predModeA);
        }
        boolean prev4x4PredMode = mode == predMode;
        out.write1Bit(prev4x4PredMode ? 1 : 0);
        if (!prev4x4PredMode) {
            int wrMode = mode - (mode > predMode ? 1 : 0);
            out.writeNBit(wrMode, 3);
        }
        i4x4PredTop[(mbX << 2) + blkX] = i4x4PredLeft[blkY] = mode;
    }

    private int lumaAnal(EncodingContext ctx, Picture pic, int mbX, int mbY, BitWriter out, int qp, EncodedMB outMB,
                         int predType[], int[][] _coeff) {
        int cbp = 0;
        byte[] pred = new byte[16];
        int[] coeff = new int[16];
        byte[] tl = new byte[]{ctx.topLeft[0], ctx.leftRow[0][3], ctx.leftRow[0][7], ctx.leftRow[0][11]};
        for (int i8x8 = 0; i8x8 < 4; i8x8++) {
            boolean hasNz = false;
            for (int i4x4 = 0; i4x4 < 4; i4x4++) {
                int bIdx = (i8x8 << 2) + i4x4;
                int blkOffLeft = H264Const.MB_DISP_OFF_LEFT[bIdx];
                int blkOffTop = H264Const.MB_DISP_OFF_TOP[bIdx];
                int blkX = (mbX << 2) + blkOffLeft;
                int blkY = (mbY << 2) + blkOffTop;

                int dIdx = BLK_DISP_MAP[bIdx];
                boolean hasLeft = (dIdx & 0x3) != 0 || mbX != 0;
                boolean hasTop = dIdx >= 4 || mbY != 0;
                boolean hasTr = ((bIdx == 0 || bIdx == 1 || bIdx == 4) && mbY != 0)
                        || (bIdx == 5 && mbX < ctx.mbWidth - 1) || bIdx == 2 || bIdx == 6 || bIdx == 8 || bIdx == 9
                        || bIdx == 10 || bIdx == 12 || bIdx == 14;

                Intra4x4PredictionBuilder.lumaPred(predType[bIdx], hasLeft, hasTop, hasTr, ctx.leftRow[0],
                        ctx.topLine[0], tl[blkOffTop], blkX << 2, blkOffTop << 2, pred);
                MBEncoderHelper.takeSubtract(pic.getPlaneData(0), pic.getPlaneWidth(0), pic.getPlaneHeight(0),
                        blkX << 2, blkY << 2, coeff, pred, 4, 4);
                CoeffTransformer.fdct4x4(coeff);
                CoeffTransformer.quantizeAC(coeff, qp);
                System.arraycopy(coeff, 0, _coeff[bIdx], 0, 16);
                hasNz |= MBWriterI16x16.hasNz(coeff);
                CoeffTransformer.dequantizeAC(coeff, qp, null);
                CoeffTransformer.idct4x4(coeff);
                MBEncoderHelper.putBlk(outMB.pixels.getPlaneData(0), coeff, pred, 4, blkOffLeft << 2, blkOffTop << 2, 4,
                        4);

                tl[blkOffTop] = ctx.topLine[0][(blkX << 2) + 3];
                for (int p = 0; p < 4; p++) {
                    ctx.leftRow[0][(blkOffTop << 2) + p] = (byte) clip(coeff[3 + (p << 2)] + pred[3 + (p << 2)], -128,
                            127);
                    ctx.topLine[0][(blkX << 2) + p] = (byte) clip(coeff[12 + p] + pred[12 + p], -128, 127);
                }
            }
            cbp |= ((hasNz ? 1 : 0) << i8x8);
        }
        ctx.topLeft[0] = tl[0];
        return cbp;
    }

    private void lumaCode(EncodingContext ctx, Picture pic, int mbX, int mbY, BitWriter out, int qp, EncodedMB outMB,
                          int predType[], int[][] _coeff, int cbpLuma) {
        int cbp = 0;
        for (int i8x8 = 0; i8x8 < 4; i8x8++) {
            int bx = (mbX << 2) | ((i8x8 << 1) & 2);
            int by = (mbY << 2) | (i8x8 & 2);

            if ((cbpLuma & (1 << i8x8)) != 0) {
                cbp |= (1 << i8x8);
                for (int i4x4 = 0; i4x4 < 4; i4x4++) {
                    int blkX = bx | (i4x4 & 1);
                    int blkY = by | ((i4x4 >> 1) & 1);
                    int blkOffLeft = blkX & 0x3;
                    int blkOffTop = blkY & 0x3;
                    int bIdx = (i8x8 << 2) + i4x4;
                    int dIdx = BLK_DISP_MAP[bIdx];

                    outMB.nc[dIdx] = CAVLC.totalCoeff(
                            ctx.cavlc[0].writeACBlock(out, blkX, blkY, blkOffLeft == 0 ? ctx.leftMBType : MBType.I_NxN,
                                    blkOffTop == 0 ? ctx.topMBType[mbX] : MBType.I_NxN, _coeff[bIdx], H264Const.totalZeros16, 0,
                                    16, CoeffTransformer.zigzag4x4));

                }
            } else {
                for (int i4x4 = 0; i4x4 < 4; i4x4++) {
                    int blkX = bx | (i4x4 & 1);
                    int blkY = by | ((i4x4 >> 1) & 1);
                    ctx.cavlc[0].setZeroCoeff(blkX, blkY);
                }
            }
        }
    }
}