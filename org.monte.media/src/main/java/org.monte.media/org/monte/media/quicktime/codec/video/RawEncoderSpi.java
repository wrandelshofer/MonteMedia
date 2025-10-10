/*
 * @(#)RawCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RawCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class RawEncoderSpi implements CodecSpi {

    @Override
    public RawEncoder create() {
        return new RawEncoder();
    }

}
