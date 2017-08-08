/* @(#)AVIPCMAudioCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
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
