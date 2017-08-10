/* @(#)PNGCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * PNGCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class PNGCodecSpi implements CodecSpi {

    @Override
    public PNGCodec create() {
       return new PNGCodec();
    }

}
