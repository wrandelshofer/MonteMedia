/*
 * @(#)PNGDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/// PNGCodecSpi.
///
/// @author Werner Randelshofer
public class PNGDecoderSpi implements CodecSpi {

    @Override
    public PNGDecoder create() {
        return new PNGDecoder();
    }

}
