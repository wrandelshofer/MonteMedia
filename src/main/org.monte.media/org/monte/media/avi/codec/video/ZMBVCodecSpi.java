/* @(#)ZMBVCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.avi.codec.video;

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
