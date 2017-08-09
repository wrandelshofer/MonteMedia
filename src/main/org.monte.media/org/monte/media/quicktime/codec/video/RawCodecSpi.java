/* @(#)RawCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RawCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class RawCodecSpi implements CodecSpi {

    @Override
    public RawCodec create() {
        return new RawCodec();
    }

}
