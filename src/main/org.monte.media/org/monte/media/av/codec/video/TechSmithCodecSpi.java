/* @(#)TechSmithCodecSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

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
