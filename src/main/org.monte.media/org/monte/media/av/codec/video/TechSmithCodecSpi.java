/* @(#)TechSmithCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * TechSmithCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class TechSmithCodecSpi implements CodecSpi {

    @Override
    public TechSmithCodec create() {
       return new TechSmithCodec();
    }

}
