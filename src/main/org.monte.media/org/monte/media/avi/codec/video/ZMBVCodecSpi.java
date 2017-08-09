/* @(#)ZMBVCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.avi.codec.audio.*;
import org.monte.media.av.CodecSpi;

/**
 * ZMBVCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class ZMBVCodecSpi implements CodecSpi {

    @Override
    public ZMBVCodec create() {
       return new ZMBVCodec();
    }

}
