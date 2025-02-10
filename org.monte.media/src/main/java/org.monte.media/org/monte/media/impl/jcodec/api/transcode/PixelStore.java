package org.monte.media.impl.jcodec.api.transcode;

import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.model.Picture;

/**
 * PixelStore.
 * <p>
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 */
public interface PixelStore {
    public static class LoanerPicture {
        private Picture picture;
        private int refCnt;

        public LoanerPicture(Picture picture, int refCnt) {
            this.picture = picture;
            this.refCnt = refCnt;
        }

        public Picture getPicture() {
            return picture;
        }

        public int getRefCnt() {
            return refCnt;
        }

        public void decRefCnt() {
            --refCnt;
        }

        public boolean unused() {
            return refCnt <= 0;
        }

        public void incRefCnt() {
            ++refCnt;
        }
    }

    LoanerPicture getPicture(int width, int height, ColorSpace color);

    void putBack(LoanerPicture frame);

    void retake(LoanerPicture frame);
}