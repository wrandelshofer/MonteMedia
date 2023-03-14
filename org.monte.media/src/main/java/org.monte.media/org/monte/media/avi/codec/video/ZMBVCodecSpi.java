/*
 * @(#)ZMBVCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * ZMBVCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class ZMBVCodecSpi implements CodecSpi {

    @Override
    public ZMBVCodec create() {
        return new ZMBVCodec();
    }

}
