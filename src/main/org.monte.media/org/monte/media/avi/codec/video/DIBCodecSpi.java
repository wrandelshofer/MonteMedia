/* @(#)DIBCodecSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
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
