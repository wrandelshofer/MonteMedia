/*
 * @(#)ZMBVDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/// ZMBVCodecSpi.
///
/// @author Werner Randelshofer
public class ZMBVDecoderSpi implements CodecSpi {

    @Override
    public ZMBVDecoder create() {
        return new ZMBVDecoder();
    }

}
