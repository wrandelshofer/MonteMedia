/*
 * @(#)PictureEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.mp4.codec.video;

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
