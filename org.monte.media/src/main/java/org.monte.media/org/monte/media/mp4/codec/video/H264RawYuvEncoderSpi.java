/*
 * @(#)H264RawYuvEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.mp4.codec.video;

import org.monte.media.av.CodecSpi;

/// JPEGCodecSpi.
///
/// @author Werner Randelshofer
public class H264RawYuvEncoderSpi implements CodecSpi {

    @Override
    public H264RawYuvEncoder create() {
        return new H264RawYuvEncoder();
    }

}
