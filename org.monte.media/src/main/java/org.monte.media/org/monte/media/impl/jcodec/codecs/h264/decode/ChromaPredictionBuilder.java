package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.common.tools.MathUtil;

import static org.monte.media.impl.jcodec.codecs.h264.H264Const.CHROMA_BLOCK_LUT;
import static org.monte.media.impl.jcodec.codecs.h264.H264Const.CHROMA_POS_LUT;
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
 * Prediction builder for chroma samples
 *
 * @author The JCodec project
 */
public class ChromaPredictionBuilder {

    public static void predictWithMode(int[][] residual, int chromaMode, int mbX, boolean leftAvailable,
                                       boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] topLeft, byte[] pixOut) {

        switch (chromaMode) {
            case 0:
                predictDC(residual, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
                break;
            case 1:
                predictHorizontal(residual, mbX, leftAvailable, leftRow, pixOut);
                break;
            case 2:
                predictVertical(residual, mbX, topAvailable, topLine, pixOut);
                break;
            case 3:
                predictPlane(residual, mbX, leftAvailable, topAvailable, leftRow, topLine, topLeft, pixOut);
                break;
        }

    }

    public static void buildPred(int chromaMode, int mbX, boolean leftAvailable,
                                 boolean topAvailable, byte[] leftRow, byte[] topLine, byte topLeft, byte[][] pixOut) {

        switch (chromaMode) {
            case 0:
                buildPredDC(mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
                break;
            case 1:
                buildPredHorz(mbX, leftAvailable, leftRow, pixOut);
                break;
            case 2:
                buildPredVert(mbX, topAvailable, topLine, pixOut);
                break;
            case 3:
                buildPredPlane(mbX, leftAvailable, topAvailable, leftRow, topLine, topLeft, pixOut);
                break;
        }

    }

    public static int predSAD(int chromaMode, int mbX, boolean leftAvailable, boolean topAvailable, byte[] leftRow,
                              byte[] topLine, byte topLeft, byte[] pix) {
        switch (chromaMode) {
            default:
            case 0:
                return predDCSAD(mbX, leftAvailable, topAvailable, leftRow, topLine, pix);
            case 1:
                return predHorizontalSAD(mbX, leftAvailable, leftRow, pix);
            case 2:
                return predVerticalSAD(mbX, topAvailable, topLine, pix);
            case 3:
                return predPlaneSAD(mbX, leftAvailable, topAvailable, leftRow, topLine, topLeft, pix);
        }
    }

    public static boolean predAvb(int chromaMode, boolean leftAvailable, boolean topAvailable) {
        switch (chromaMode) {
            default:
            case 0:
                return true;
            case 1:
                return leftAvailable;
            case 2:
                return topAvailable;
            case 3:
                return leftAvailable && topAvailable;
        }
    }

    public static void predictDC(int[][] planeData, int mbX, boolean leftAvailable, boolean topAvailable,
                                 byte[] leftRow, byte[] topLine, byte[] pixOut) {
        predictDCInside(planeData, 0, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCTopBorder(planeData, 1, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCLeftBorder(planeData, 0, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCInside(planeData, 1, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
    }

    public static int predDCSAD(int mbX, boolean leftAvailable, boolean topAvailable, byte[] leftRow, byte[] topLine,
                                byte[] pixOut) {
        return predictDCInsideSAD(0, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut)
                + predictDCTopBorderSAD(1, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut)
                + predictDCLeftBorderSAD(0, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut)
                + predictDCInsideSAD(1, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
    }

    public static void buildPredDC(int mbX, boolean leftAvailable, boolean topAvailable, byte[] leftRow, byte[] topLine,
                                   byte[][] pixOut) {
        buildPredDCIns(0, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut[0]);
        buildPredDCTop(1, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut[1]);
        buildPredDCLft(0, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut[2]);
        buildPredDCIns(1, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut[3]);
    }

    public static void predictVertical(int[][] residual, int mbX, boolean topAvailable, byte[] topLine, byte[] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[off] = (byte) clip(
                        residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + topLine[(mbX << 3) + i], -128, 127);
        }
    }

    public static int predVerticalSAD(int mbX, boolean topAvailable, byte[] topLine, byte[] pixOut) {
        int sad = 0;
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                sad += MathUtil.abs(pixOut[off] - topLine[(mbX << 3) + i]);
        }
        return sad;
    }

    public static void buildPredVert(int mbX, boolean topAvailable, byte[] topLine, byte[][] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] = topLine[(mbX << 3) + i];
        }
    }

    public static void predictHorizontal(int[][] residual, int mbX, boolean leftAvailable, byte[] leftRow,
                                         byte[] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + leftRow[j], -128, 127);
        }
    }

    public static int predHorizontalSAD(int mbX, boolean leftAvailable, byte[] leftRow, byte[] pixOut) {
        int sad = 0;
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                sad += MathUtil.abs(pixOut[off] - leftRow[j]);
        }
        return sad;
    }

    public static void buildPredHorz(int mbX, boolean leftAvailable, byte[] leftRow, byte[][] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] = leftRow[j];
        }
    }

    public static void predictDCInside(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
                                       boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s0, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;

        if (leftAvailable && topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[i + blkOffY];
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];

            s0 = (s0 + 4) >> 3;
        } else if (leftAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[blkOffY + i];
            s0 = (s0 + 2) >> 2;
        } else if (topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];
            s0 = (s0 + 2) >> 2;
        } else {
            s0 = 0;
        }
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s0, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s0, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s0, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s0, -128, 127);
        }
    }

    public static int predictDCInsideSAD(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                         byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s0, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;

        if (leftAvailable && topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[i + blkOffY];
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];

            s0 = (s0 + 4) >> 3;
        } else if (leftAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[blkOffY + i];
            s0 = (s0 + 2) >> 2;
        } else if (topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];
            s0 = (s0 + 2) >> 2;
        } else {
            s0 = 0;
        }
        int sad = 0;
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            sad += MathUtil.abs(pixOut[off + 0] - s0);
            sad += MathUtil.abs(pixOut[off + 1] - s0);
            sad += MathUtil.abs(pixOut[off + 2] - s0);
            sad += MathUtil.abs(pixOut[off + 3] - s0);
        }
        return sad;
    }

    public static void buildPredDCIns(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                      byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s0, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;

        if (leftAvailable && topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[i + blkOffY];
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];

            s0 = (s0 + 4) >> 3;
        } else if (leftAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[blkOffY + i];
            s0 = (s0 + 2) >> 2;
        } else if (topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];
            s0 = (s0 + 2) >> 2;
        } else {
            s0 = 0;
        }
        for (int j = 0; j < 16; j++) {
            pixOut[j] = (byte) s0;
        }
    }

    public static void predictDCTopBorder(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
                                          boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s1, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (topAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += topLine[blkOffX + i];

            s1 = (s1 + 2) >> 2;
        } else if (leftAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += leftRow[blkOffY + i];
            s1 = (s1 + 2) >> 2;
        } else {
            s1 = 0;
        }
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s1, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s1, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s1, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s1, -128, 127);
        }
    }

    public static int predictDCTopBorderSAD(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                            byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s1, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (topAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += topLine[blkOffX + i];

            s1 = (s1 + 2) >> 2;
        } else if (leftAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += leftRow[blkOffY + i];
            s1 = (s1 + 2) >> 2;
        } else {
            s1 = 0;
        }
        int sad = 0;
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            sad += MathUtil.abs(pixOut[off + 0] - s1);
            sad += MathUtil.abs(pixOut[off + 1] - s1);
            sad += MathUtil.abs(pixOut[off + 2] - s1);
            sad += MathUtil.abs(pixOut[off + 3] - s1);
        }
        return sad;
    }

    public static void buildPredDCTop(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                      byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s1, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (topAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += topLine[blkOffX + i];

            s1 = (s1 + 2) >> 2;
        } else if (leftAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += leftRow[blkOffY + i];
            s1 = (s1 + 2) >> 2;
        } else {
            s1 = 0;
        }
        for (int j = 0; j < 16; j++) {
            pixOut[j] = (byte) s1;
        }
    }

    public static void predictDCLeftBorder(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
                                           boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s2, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (leftAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += leftRow[blkOffY + i];
            s2 = (s2 + 2) >> 2;
        } else if (topAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += topLine[blkOffX + i];
            s2 = (s2 + 2) >> 2;
        } else {
            s2 = 0;
        }
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s2, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s2, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s2, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s2, -128, 127);
        }
    }

    public static int predictDCLeftBorderSAD(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                             byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s2, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (leftAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += leftRow[blkOffY + i];
            s2 = (s2 + 2) >> 2;
        } else if (topAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += topLine[blkOffX + i];
            s2 = (s2 + 2) >> 2;
        } else {
            s2 = 0;
        }
        int sad = 0;
        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            sad += MathUtil.abs(pixOut[off] - s2);
            sad += MathUtil.abs(pixOut[off + 1] - s2);
            sad += MathUtil.abs(pixOut[off + 2] - s2);
            sad += MathUtil.abs(pixOut[off + 3] - s2);
        }
        return sad;
    }

    public static void buildPredDCLft(int blkX, int blkY, int mbX, boolean leftAvailable, boolean topAvailable,
                                      byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s2, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (leftAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += leftRow[blkOffY + i];
            s2 = (s2 + 2) >> 2;
        } else if (topAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += topLine[blkOffX + i];
            s2 = (s2 + 2) >> 2;
        } else {
            s2 = 0;
        }
        for (int j = 0; j < 16; j++) {
            pixOut[j] = (byte) s2;
        }
    }

    public static void predictPlane(int[][] residual, int mbX, boolean leftAvailable, boolean topAvailable,
                                    byte[] leftRow, byte[] topLine, byte[] topLeft, byte[] pixOut) {
        int H = 0, blkOffX = (mbX << 3);

        for (int i = 0; i < 3; i++) {
            H += (i + 1) * (topLine[blkOffX + 4 + i] - topLine[blkOffX + 2 - i]);
        }
        H += 4 * (topLine[blkOffX + 7] - topLeft[0]);

        int V = 0;
        for (int j = 0; j < 3; j++) {
            V += (j + 1) * (leftRow[4 + j] - leftRow[2 - j]);
        }
        V += 4 * (leftRow[7] - topLeft[0]);

        int c = (34 * V + 32) >> 6;
        int b = (34 * H + 32) >> 6;
        int a = 16 * (leftRow[7] + topLine[blkOffX + 7]);

        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++) {
                int val = (a + b * (i - 3) + c * (j - 3) + 16) >> 5;
                pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + clip(val, -128, 127),
                        -128, 127);
            }
        }
    }

    public static int predPlaneSAD(int mbX, boolean leftAvailable, boolean topAvailable, byte[] leftRow, byte[] topLine,
                                   byte topLeft, byte[] pixOut) {
        int sad = 0;
        int H = 0, blkOffX = (mbX << 3);

        for (int i = 0; i < 3; i++) {
            H += (i + 1) * (topLine[blkOffX + 4 + i] - topLine[blkOffX + 2 - i]);
        }
        H += 4 * (topLine[blkOffX + 7] - topLeft);

        int V = 0;
        for (int j = 0; j < 3; j++) {
            V += (j + 1) * (leftRow[4 + j] - leftRow[2 - j]);
        }
        V += 4 * (leftRow[7] - topLeft);

        int c = (34 * V + 32) >> 6;
        int b = (34 * H + 32) >> 6;
        int a = 16 * (leftRow[7] + topLine[blkOffX + 7]);

        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++) {
                int val = (a + b * (i - 3) + c * (j - 3) + 16) >> 5;
                sad += MathUtil.abs(pixOut[off] - clip(val, -128, 127));
            }
        }
        return sad;
    }

    public static void buildPredPlane(int mbX, boolean leftAvailable, boolean topAvailable, byte[] leftRow, byte[] topLine,
                                      byte topLeft, byte[][] pixOut) {
        int H = 0, blkOffX = (mbX << 3);

        for (int i = 0; i < 3; i++) {
            H += (i + 1) * (topLine[blkOffX + 4 + i] - topLine[blkOffX + 2 - i]);
        }
        H += 4 * (topLine[blkOffX + 7] - topLeft);

        int V = 0;
        for (int j = 0; j < 3; j++) {
            V += (j + 1) * (leftRow[4 + j] - leftRow[2 - j]);
        }
        V += 4 * (leftRow[7] - topLeft);

        int c = (34 * V + 32) >> 6;
        int b = (34 * H + 32) >> 6;
        int a = 16 * (leftRow[7] + topLine[blkOffX + 7]);

        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++) {
                int val = (a + b * (i - 3) + c * (j - 3) + 16) >> 5;
                pixOut[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] = (byte) clip(val, -128, 127);
            }
        }
    }
}
