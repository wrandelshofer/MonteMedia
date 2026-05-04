/*
 * @(#)RawCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.sprite;

import org.monte.media.av.CodecSpi;

/**
 * RawCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class SpriteDecoderSpi implements CodecSpi {

    @Override
    public SpriteDecoder create() {
        return new SpriteDecoder();
    }

}
