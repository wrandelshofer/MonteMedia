/*
 * @(#)JPEGCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.codec;

import org.monte.media.av.CodecSpi;

/**
 * JPEGCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class PictureCodecSpi implements CodecSpi {

    @Override
    public PictureCodec create() {
        return new PictureCodec();
    }

}
