/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * TechSmithCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class TechSmithCodecSpi implements CodecSpi {

    @Override
    public TechSmithCodec create() {
        return new TechSmithCodec();
    }

}
