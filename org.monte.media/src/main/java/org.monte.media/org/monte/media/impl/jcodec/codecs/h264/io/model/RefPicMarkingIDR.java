/*
 * @(#)RefPicMarkingIDR.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.codecs.h264.io.model;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
///
/// Reference picture marking used for IDR frames
///
/// @author The JCodec project
public class RefPicMarkingIDR {
    boolean discardDecodedPics;
    boolean useForlongTerm;

    public RefPicMarkingIDR(boolean discardDecodedPics, boolean useForlongTerm) {
        this.discardDecodedPics = discardDecodedPics;
        this.useForlongTerm = useForlongTerm;
    }

    public boolean isDiscardDecodedPics() {
        return discardDecodedPics;
    }

    public boolean isUseForlongTerm() {
        return useForlongTerm;
    }


}
