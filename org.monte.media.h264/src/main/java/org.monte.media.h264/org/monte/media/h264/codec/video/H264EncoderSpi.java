/*
 * @(#)H264EncoderSpi.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.codec.video;

import org.monte.media.av.CodecSpi;

/// JPEGCodecSpi.
///
/// @author Werner Randelshofer
public class H264EncoderSpi implements CodecSpi {

    @Override
    public H264Encoder create() {
        return new H264Encoder();
    }

}
