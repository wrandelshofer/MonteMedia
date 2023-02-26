/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RawCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class RawCodecSpi implements CodecSpi {

    @Override
    public RawCodec create() {
        return new RawCodec();
    }

}
