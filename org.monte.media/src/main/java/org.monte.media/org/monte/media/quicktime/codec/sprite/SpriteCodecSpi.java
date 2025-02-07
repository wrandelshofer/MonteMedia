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
public class SpriteCodecSpi implements CodecSpi {

    @Override
    public SpriteCodec create() {
        return new SpriteCodec();
    }

}
