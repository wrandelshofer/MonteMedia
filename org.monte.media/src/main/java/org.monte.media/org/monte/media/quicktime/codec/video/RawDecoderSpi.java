/*
 * @(#)RawDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RawCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class RawDecoderSpi implements CodecSpi {

    @Override
    public RawDecoder create() {
        return new RawDecoder();
    }

}
