/* @(#)JPEGCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * JPEGCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class JPEGCodecSpi implements CodecSpi {

    @Override
    public JPEGCodec create() {
       return new JPEGCodec();
    }

}
