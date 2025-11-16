/*
 * @(#)WritableImageCodecSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

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
