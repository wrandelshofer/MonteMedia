/*
 * @(#)AVIPCMAudioCodecSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.audio;

import org.monte.media.av.CodecSpi;

/// AVIPCMAudioCodecSpi.
///
/// @author Werner Randelshofer
public class AVIPCMAudioCodecSpi implements CodecSpi {

    @Override
    public AVIPCMAudioCodec create() {
        return new AVIPCMAudioCodec();
    }

}
