/* @(#)AVIPCMAudioCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.avi.codec.audio;

import org.monte.media.av.CodecSpi;

/**
 * AVIPCMAudioCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class AVIPCMAudioCodecSpi implements CodecSpi {

    @Override
    public AVIPCMAudioCodec create() {
       return new AVIPCMAudioCodec();
    }

}
