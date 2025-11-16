/*
 * @(#)PNGEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * PNGCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class PNGEncoderSpi implements CodecSpi {

    @Override
    public PNGEncoder create() {
        return new PNGEncoder();
    }

}
