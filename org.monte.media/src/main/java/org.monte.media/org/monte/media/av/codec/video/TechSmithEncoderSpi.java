/*
 * @(#)TechSmithEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * TechSmithCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class TechSmithEncoderSpi implements CodecSpi {

    @Override
    public TechSmithEncoder create() {
        return new TechSmithEncoder();
    }

}
