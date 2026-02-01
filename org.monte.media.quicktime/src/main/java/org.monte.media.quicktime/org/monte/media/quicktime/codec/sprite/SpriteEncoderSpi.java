/*
 * @(#)SpriteEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.sprite;

import org.monte.media.av.CodecSpi;

/// RawCodecSpi.
///
/// @author Werner Randelshofer
public class SpriteEncoderSpi implements CodecSpi {

    @Override
    public SpriteEncoder create() {
        return new SpriteEncoder();
    }

}
