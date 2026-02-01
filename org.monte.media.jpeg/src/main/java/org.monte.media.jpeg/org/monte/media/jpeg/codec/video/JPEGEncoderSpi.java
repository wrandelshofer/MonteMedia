/*
 * @(#)JPEGEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jpeg.codec.video;

import org.monte.media.av.CodecSpi;

/// JPEGCodecSpi.
///
/// @author Werner Randelshofer
public class JPEGEncoderSpi implements CodecSpi {

    @Override
    public JPEGEncoder create() {
        return new JPEGEncoder();
    }

}
