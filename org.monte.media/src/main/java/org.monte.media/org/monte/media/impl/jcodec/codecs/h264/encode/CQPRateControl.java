package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.common.model.Size;
import org.monte.media.impl.jcodec.common.tools.MathUtil;

/**
 * Constant QP with psyvisual adjustments
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
 * @author Stanislav Vitvitskiy
 */
public class CQPRateControl implements RateControl {

    private static final int MINQP = 12;
    private int qp;
    private int initialQp;
    private int oldQp;
    private SliceType sliceType;

    public CQPRateControl(int qp) {
        this.initialQp = qp;
    }

    @Override
    public int startPicture(Size sz, int maxSize, SliceType sliceType) {
        this.qp = initialQp;
        this.oldQp = initialQp;
        this.sliceType = sliceType;
        return qp;
    }

    @Override
    public int accept(int bits) {
        return 0;
    }

    @Override
    public int initialQpDelta(Picture pic, int mbX, int mbY) {
        if (initialQp <= MINQP) {
            return 0;
        }
        byte[] patch = new byte[256];
        MBEncoderHelper.take(pic.getPlaneData(0), pic.getPlaneWidth(0), pic.getPlaneHeight(0), mbX << 4, mbY << 4,
                patch, 16, 16);
        int avg = calcAvg(patch);
        double var = calcVar(patch, avg);
        double bright = calcBright(avg);
        int newQp = initialQp;
        int range = (initialQp - MINQP) / 2;
        // Brightness
        double delta = var * 0.1 * Math.max(0, bright - 2);
        var += delta;

        // Variance
        if (var < 4) {
            newQp = sliceType == SliceType.I ? Math.max(initialQp / 2, 12) : Math.max(2 * initialQp / 3, 18);
        } else if (var < 8) {
            newQp = initialQp - range / 2;
        } else if (var < 16) {
            newQp = initialQp - range / 4;
        } else if (var < 32) {
            newQp = initialQp - range / 8;
        } else if (var < 64) {
            newQp = initialQp - range / 16;
        }
        int qpDelta = newQp - oldQp;
        oldQp = newQp;
        return qpDelta;
    }

    private int calcAvg(byte[] patch) {
        int sum = 0;
        for (int i = 0; i < 256; i++)
            sum += patch[i];
        return sum >> 8;
    }

    private double calcVar(byte[] patch, int avg) {
        long sum1 = 0;
        for (int i = 0; i < 256; i++) {
            int diff = patch[i] - avg;
            sum1 += diff * diff;
        }

        return Math.sqrt(sum1 >> 8);
    }

    private double calcBright(int avg) {
        return MathUtil.log2(avg + 128);
    }
}
