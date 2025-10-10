package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.common.model.Size;

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
 * Dumb rate control policy, always maintains the same QP for the whole video
 *
 * @author The JCodec project
 */
public class DumbRateControl implements RateControl {
    private static final int QP = 20;
    private int bitsPerMb;
    private int totalQpDelta;
    private boolean justSwitched;

    @Override
    public int accept(int bits) {
        if (bits >= bitsPerMb) {
            totalQpDelta++;
            justSwitched = true;
            return 1;
        } else {
            // Only decrease qp if we got too few bits (more then 12.5%)
            if (totalQpDelta > 0 && !justSwitched && (bitsPerMb - bits > (bitsPerMb >> 3))) {
                --totalQpDelta;
                justSwitched = true;
                return -1;
            } else {
                justSwitched = false;
            }
            return 0;
        }
    }

    @Override
    public int startPicture(Size sz, int maxSize, SliceType sliceType) {
        int totalMb = ((sz.getWidth() + 15) >> 4) * ((sz.getHeight() + 15) >> 4);
        bitsPerMb = (maxSize << 3) / totalMb;
        totalQpDelta = 0;
        justSwitched = false;
        return QP + (sliceType == SliceType.P ? 6 : 0);
    }

    @Override
    public int initialQpDelta(Picture pic, int mbX, int mbY) {
        return 0;
    }
}
