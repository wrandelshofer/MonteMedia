/*
 * @(#)JPEGCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;

import org.monte.media.av.CodecSpi;

/**
 * JPEGCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class WritableImageCodecSpi implements CodecSpi {

    @Override
    public WritableImageCodec create() {
        return new WritableImageCodec();
    }

}
