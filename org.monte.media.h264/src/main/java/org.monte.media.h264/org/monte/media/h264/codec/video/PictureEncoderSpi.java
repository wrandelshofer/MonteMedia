/*
 * @(#)PictureEncoderSpi.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.codec.video;

import org.monte.media.av.CodecSpi;

/// JPEGCodecSpi.
///
/// @author Werner Randelshofer
public class PictureEncoderSpi implements CodecSpi {

    @Override
    public PictureEncoder create() {
        return new PictureEncoder();
    }

}
