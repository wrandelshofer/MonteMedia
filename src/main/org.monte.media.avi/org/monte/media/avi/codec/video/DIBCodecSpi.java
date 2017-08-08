/* @(#)DIBCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.avi.codec.audio.*;
import org.monte.media.av.CodecSpi;

/**
 * DIBCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class DIBCodecSpi implements CodecSpi {

    @Override
    public DIBCodec create() {
       return new DIBCodec();
    }

}
