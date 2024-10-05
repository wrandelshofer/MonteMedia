/*
 * @(#)JPEGCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.mp4.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * JPEGCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class JCodecPictureCodecSpi implements CodecSpi {

    @Override
    public JCodecPictureCodec create() {
        return new JCodecPictureCodec();
    }

}
