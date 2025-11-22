/*
 * @(#)JPEGDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/// JPEGCodecSpi.
///
/// @author Werner Randelshofer
public class JPEGDecoderSpi implements CodecSpi {

    @Override
    public JPEGDecoder create() {
        return new JPEGDecoder();
    }

}
