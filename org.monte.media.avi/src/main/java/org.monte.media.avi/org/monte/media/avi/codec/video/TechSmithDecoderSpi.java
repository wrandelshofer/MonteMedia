/*
 * @(#)TechSmithDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/// TechSmithCodecSpi.
///
/// @author Werner Randelshofer
public class TechSmithDecoderSpi implements CodecSpi {

    @Override
    public TechSmithDecoder create() {
        return new TechSmithDecoder();
    }

}
