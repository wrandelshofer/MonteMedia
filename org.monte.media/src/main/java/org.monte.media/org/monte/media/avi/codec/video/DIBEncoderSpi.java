/*
 * @(#)DIBCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * DIBCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class DIBEncoderSpi implements CodecSpi {

    @Override
    public DIBEncoder create() {
        return new DIBEncoder();
    }

}
