/*
 * @(#)PixelStore.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.impl.jcodec.api.transcode;

import org.monte.media.h264.impl.jcodec.common.model.ColorSpace;
import org.monte.media.h264.impl.jcodec.common.model.Picture;

/// PixelStore.
///
/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
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