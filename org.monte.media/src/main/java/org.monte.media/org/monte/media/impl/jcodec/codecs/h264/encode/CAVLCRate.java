package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.common.io.VLC;

/**
 * Returns the number of bits it would take to write certain types of CAVLC
 * symbols
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
 * @author Stanislav Vitvitskyy
 */
public class CAVLCRate {

    public static int rateTE(int refIdx, int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int rateSE(int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int rateUE(int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int rateACBlock(int i, int j, MBType p16x16, MBType p16x162, int[] ks, VLC[] totalzeros16, int k, int l,
                           int[] zigzag4x4) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int rateChrDCBlock(int[] dc, VLC[] totalzeros4, int i, int length, int[] js) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int rateLumaDCBlock(int mbLeftBlk, int mbTopBlk, MBType leftMBType, MBType topMBType,
                               int[] dc, VLC[] totalzeros16, int i, int j, int[] zigzag4x4) {
        // TODO Auto-generated method stub
        return 0;
    }
}
