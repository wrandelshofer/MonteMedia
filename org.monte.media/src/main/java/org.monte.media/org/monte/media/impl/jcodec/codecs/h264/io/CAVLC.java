package org.monte.media.impl.jcodec.codecs.h264.io;

import org.monte.media.impl.jcodec.codecs.h264.H264Const;
import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.PictureParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.common.io.BitReader;
import org.monte.media.impl.jcodec.common.io.BitWriter;
import org.monte.media.impl.jcodec.common.io.VLC;
import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.tools.MathUtil;

import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readU;
import static org.monte.media.impl.jcodec.codecs.h264.decode.CAVLCReader.readZeroBitCount;
import static org.monte.media.impl.jcodec.common.model.ColorSpace.YUV422;
import static org.monte.media.impl.jcodec.common.model.ColorSpace.YUV444;

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
 * Non-CABAC H.264 symbols read/write routines
 *
 * @author Jay Codec
 */
public class CAVLC {

    private ColorSpace color;
    private VLC chromaDCVLC;

    private int[] tokensLeft;
    private int[] tokensTop;

    private int mbWidth;
    private int mbMask;
    private int mbW;
    private int mbH;

    public CAVLC(SeqParameterSet sps, PictureParameterSet pps, int mbW, int mbH) {
        this(sps.chromaFormatIdc, sps.picWidthInMbsMinus1 + 1, mbW, mbH);
    }

    private CAVLC(ColorSpace color, int mbWidth, int mbW, int mbH) {
        this.color = color;
        this.chromaDCVLC = codeTableChromaDC();
        this.mbWidth = mbWidth;
        this.mbMask = (1 << mbH) - 1;
        this.mbW = mbW;
        this.mbH = mbH;

        tokensLeft = new int[4];
        tokensTop = new int[mbWidth << mbW];
    }

    public CAVLC fork() {
        CAVLC ret = new CAVLC(color, mbWidth, mbW, mbH);
        System.arraycopy(tokensLeft, 0, ret.tokensLeft, 0, tokensLeft.length);
        System.arraycopy(tokensTop, 0, ret.tokensTop, 0, tokensTop.length);
        return ret;
    }

    public int writeACBlock(BitWriter out, int blkIndX, int blkIndY, MBType leftMBType, MBType topMBType, int[] coeff,
                            VLC[] totalZerosTab, int firstCoeff, int maxCoeff, int[] scan) {
        VLC coeffTokenTab = getCoeffTokenVLCForLuma(blkIndX != 0, leftMBType, tokensLeft[blkIndY & mbMask],
                blkIndY != 0, topMBType, tokensTop[blkIndX]);

        int coeffToken = writeBlockGen(out, coeff, totalZerosTab, firstCoeff, maxCoeff, scan, coeffTokenTab);

        tokensLeft[blkIndY & mbMask] = coeffToken;
        tokensTop[blkIndX] = coeffToken;

        return coeffToken;
    }

    public void writeChrDCBlock(BitWriter out, int[] coeff, VLC[] totalZerosTab, int firstCoeff, int maxCoeff,
                                int[] scan) {
        writeBlockGen(out, coeff, totalZerosTab, firstCoeff, maxCoeff, scan, getCoeffTokenVLCForChromaDC());
    }

    public void writeLumaDCBlock(BitWriter out, int blkIndX, int blkIndY, MBType leftMBType, MBType topMBType,
                                 int[] coeff, VLC[] totalZerosTab, int firstCoeff, int maxCoeff, int[] scan) {
        VLC coeffTokenTab = getCoeffTokenVLCForLuma(blkIndX != 0, leftMBType, tokensLeft[blkIndY & mbMask],
                blkIndY != 0, topMBType, tokensTop[blkIndX]);

        writeBlockGen(out, coeff, totalZerosTab, firstCoeff, maxCoeff, scan, coeffTokenTab);
    }

    private int writeBlockGen(BitWriter out, int[] coeff, VLC[] totalZerosTab, int firstCoeff, int maxCoeff,
                              int[] scan, VLC coeffTokenTab) {
        int trailingOnes = 0, totalCoeff = 0, totalZeros = 0;
        int[] runBefore = new int[maxCoeff];
        int[] levels = new int[maxCoeff];
        for (int i = 0; i < maxCoeff; i++) {
            int c = coeff[scan[i + firstCoeff]];
            if (c == 0) {
                runBefore[totalCoeff]++;
                totalZeros++;
            } else {
                levels[totalCoeff++] = c;
            }
        }
        if (totalCoeff < maxCoeff)
            totalZeros -= runBefore[totalCoeff];

        for (trailingOnes = 0; trailingOnes < totalCoeff && trailingOnes < 3
                && Math.abs(levels[totalCoeff - trailingOnes - 1]) == 1; trailingOnes++)
            ;

        int coeffToken = H264Const.coeffToken(totalCoeff, trailingOnes);

        coeffTokenTab.writeVLC(out, coeffToken);

        if (totalCoeff > 0) {
            writeTrailingOnes(out, levels, totalCoeff, trailingOnes);
            writeLevels(out, levels, totalCoeff, trailingOnes);

            if (totalCoeff < maxCoeff) {
                totalZerosTab[totalCoeff - 1].writeVLC(out, totalZeros);
                writeRuns(out, runBefore, totalCoeff, totalZeros);
            }
        }
        return coeffToken;
    }

    private void writeTrailingOnes(BitWriter out, int[] levels, int totalCoeff, int trailingOne) {
        for (int i = totalCoeff - 1; i >= totalCoeff - trailingOne; i--) {
            out.write1Bit(levels[i] >>> 31);
        }
    }

    private void writeLevels(BitWriter out, int[] levels, int totalCoeff, int trailingOnes) {

        int suffixLen = totalCoeff > 10 && trailingOnes < 3 ? 1 : 0;
        for (int i = totalCoeff - trailingOnes - 1; i >= 0; i--) {
            int absLev = unsigned(levels[i]);
            if (i == totalCoeff - trailingOnes - 1 && trailingOnes < 3)
                absLev -= 2;

            int prefix = absLev >> suffixLen;
            if (suffixLen == 0 && prefix < 14 || suffixLen > 0 && prefix < 15) {
                out.writeNBit(1, prefix + 1);
                out.writeNBit(absLev, suffixLen);
            } else if (suffixLen == 0 && absLev < 30) {
                out.writeNBit(1, 15);
                out.writeNBit(absLev - 14, 4);
            } else {
                if (suffixLen == 0)
                    absLev -= 15;
                int len, code;
                for (len = 12; (code = absLev - (len + 3 << suffixLen) - (1 << len) + 4096) >= (1 << len); len++)
                    ;
                out.writeNBit(1, len + 4);
                out.writeNBit(code, len);
                int mask = (1 << len) - 1;
            }
            if (suffixLen == 0)
                suffixLen = 1;
            if (MathUtil.abs(levels[i]) > (3 << (suffixLen - 1)) && suffixLen < 6)
                suffixLen++;
        }
    }

    private final int unsigned(int signed) {
        int sign = signed >>> 31;
        int s = signed >> 31;

        return (((signed ^ s) - s) << 1) + sign - 2;
    }

    private void writeRuns(BitWriter out, int[] run, int totalCoeff, int totalZeros) {
        for (int i = totalCoeff - 1; i > 0 && totalZeros > 0; i--) {
            H264Const.run[Math.min(6, totalZeros - 1)].writeVLC(out, run[i]);
            totalZeros -= run[i];
        }
    }

    public VLC getCoeffTokenVLCForLuma(boolean leftAvailable, MBType leftMBType, int leftToken, boolean topAvailable,
                                       MBType topMBType, int topToken) {

        int nc = codeTableLuma(leftAvailable, leftMBType, leftToken, topAvailable, topMBType, topToken);

        return H264Const.CoeffToken[Math.min(nc, 8)];
    }

    public VLC getCoeffTokenVLCForChromaDC() {
        return chromaDCVLC;
    }

    protected int codeTableLuma(boolean leftAvailable, MBType leftMBType, int leftToken, boolean topAvailable,
                                MBType topMBType, int topToken) {

        int nA = leftMBType == null ? 0 : totalCoeff(leftToken);
        int nB = topMBType == null ? 0 : totalCoeff(topToken);

        if (leftAvailable && topAvailable)
            return (nA + nB + 1) >> 1;
        else if (leftAvailable)
            return nA;
        else if (topAvailable)
            return nB;
        else
            return 0;
    }

    protected VLC codeTableChromaDC() {
        if (color == ColorSpace.YUV420J) {
            return H264Const.coeffTokenChromaDCY420;
        } else if (color == YUV422) {
            return H264Const.coeffTokenChromaDCY422;
        } else if (color == YUV444) {
            return H264Const.CoeffToken[0];
        }
        return null;
    }

    public int readCoeffs(BitReader _in, VLC coeffTokenTab, VLC[] totalZerosTab, int[] coeffLevel, int firstCoeff,
                          int nCoeff, int[] zigzag) {
        int coeffToken = coeffTokenTab.readVLC(_in);
        int totalCoeff = totalCoeff(coeffToken);
        int trailingOnes = trailingOnes(coeffToken);

        if (totalCoeff > 0) {
            int suffixLength = totalCoeff > 10 && trailingOnes < 3 ? 1 : 0;

            int[] level = new int[totalCoeff];
            int i;
            for (i = 0; i < trailingOnes; i++) {
                int read1Bit = _in.read1Bit();
                level[i] = 1 - 2 * read1Bit;
            }

            for (; i < totalCoeff; i++) {
                int level_prefix = readZeroBitCount(_in, "");
                int levelSuffixSize = suffixLength;
                if (level_prefix == 14 && suffixLength == 0)
                    levelSuffixSize = 4;
                if (level_prefix >= 15)
                    levelSuffixSize = level_prefix - 3;

                int levelCode = (Min(15, level_prefix) << suffixLength);
                if (levelSuffixSize > 0) {
                    int level_suffix = readU(_in, levelSuffixSize, "RB: level_suffix");
                    levelCode += level_suffix;
                }
                if (level_prefix >= 15 && suffixLength == 0)
                    levelCode += 15;
                if (level_prefix >= 16)
                    levelCode += (1 << (level_prefix - 3)) - 4096;
                if (i == trailingOnes && trailingOnes < 3)
                    levelCode += 2;

                if (levelCode % 2 == 0)
                    level[i] = (levelCode + 2) >> 1;
                else
                    level[i] = (-levelCode - 1) >> 1;

                if (suffixLength == 0)
                    suffixLength = 1;
                if (Abs(level[i]) > (3 << (suffixLength - 1)) && suffixLength < 6)
                    suffixLength++;
            }

            int zerosLeft;
            if (totalCoeff < nCoeff) {
                if (coeffLevel.length == 4) {
                    zerosLeft = H264Const.totalZeros4[totalCoeff - 1].readVLC(_in);
                } else if (coeffLevel.length == 8) {
                    zerosLeft = H264Const.totalZeros8[totalCoeff - 1].readVLC(_in);
                } else {
                    zerosLeft = H264Const.totalZeros16[totalCoeff - 1].readVLC(_in);
                }
            } else
                zerosLeft = 0;

            int[] runs = new int[totalCoeff];
            int r;
            for (r = 0; r < totalCoeff - 1 && zerosLeft > 0; r++) {
                int run = H264Const.run[Math.min(6, zerosLeft - 1)].readVLC(_in);
                zerosLeft -= run;
                runs[r] = run;
            }
            runs[r] = zerosLeft;

            for (int j = totalCoeff - 1, cn = 0; j >= 0 && cn < nCoeff; j--, cn++) {
                cn += runs[j];
                coeffLevel[zigzag[cn + firstCoeff]] = level[j];
            }
        }

        return coeffToken;
    }

    private static int Min(int i, int level_prefix) {
        return i < level_prefix ? i : level_prefix;
    }

    private static int Abs(int i) {
        return i < 0 ? -i : i;
    }

    public static final int totalCoeff(int coeffToken) {
        return coeffToken >> 4;
    }

    public static final int trailingOnes(int coeffToken) {
        return coeffToken & 0xf;
    }

    public static int[] NO_ZIGZAG = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    public void readChromaDCBlock(BitReader reader, int[] coeff, boolean leftAvailable, boolean topAvailable) {
        VLC coeffTokenTab = getCoeffTokenVLCForChromaDC();

        readCoeffs(reader, coeffTokenTab, coeff.length == 16 ? H264Const.totalZeros16
                        : (coeff.length == 8 ? H264Const.totalZeros8 : H264Const.totalZeros4), coeff, 0, coeff.length,
                NO_ZIGZAG);
    }

    public void readLumaDCBlock(BitReader reader, int[] coeff, int mbX, boolean leftAvailable, MBType leftMbType,
                                boolean topAvailable, MBType topMbType, int[] zigzag4x4) {
        VLC coeffTokenTab = getCoeffTokenVLCForLuma(leftAvailable, leftMbType, tokensLeft[0], topAvailable, topMbType,
                tokensTop[mbX << 2]);

        readCoeffs(reader, coeffTokenTab, H264Const.totalZeros16, coeff, 0, 16, zigzag4x4);
    }

    public int readACBlock(BitReader reader, int[] coeff, int blkIndX, int blkIndY, boolean leftAvailable,
                           MBType leftMbType, boolean topAvailable, MBType topMbType, int firstCoeff, int nCoeff, int[] zigzag4x4) {
        VLC coeffTokenTab = getCoeffTokenVLCForLuma(leftAvailable, leftMbType, tokensLeft[blkIndY & mbMask],
                topAvailable, topMbType, tokensTop[blkIndX]);

        int readCoeffs = readCoeffs(reader, coeffTokenTab, H264Const.totalZeros16, coeff, firstCoeff, nCoeff, zigzag4x4);
        tokensLeft[blkIndY & mbMask] = tokensTop[blkIndX] = readCoeffs;

        return totalCoeff(readCoeffs);
    }

    public void setZeroCoeff(int blkIndX, int blkIndY) {
        tokensLeft[blkIndY & mbMask] = tokensTop[blkIndX] = 0;
    }
}