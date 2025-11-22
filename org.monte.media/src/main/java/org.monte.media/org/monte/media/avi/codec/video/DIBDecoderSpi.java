/*
 * @(#)DIBDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/// DIBCodecSpi.
///
/// @author Werner Randelshofer
public class DIBDecoderSpi implements CodecSpi {

    @Override
    public DIBDecoder create() {
        return new DIBDecoder();
    }

}
