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
public class H264CodecSpi implements CodecSpi {

    @Override
    public H264Codec create() {
        return new H264Codec();
    }

}
