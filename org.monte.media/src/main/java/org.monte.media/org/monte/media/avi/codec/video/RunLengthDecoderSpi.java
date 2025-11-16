/*
 * @(#)RunLengthDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RunLengthCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class RunLengthDecoderSpi implements CodecSpi {

    @Override
    public RunLengthDecoder create() {
        return new RunLengthDecoder();
    }

}
