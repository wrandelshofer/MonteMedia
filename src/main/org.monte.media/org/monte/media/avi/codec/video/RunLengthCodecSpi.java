/* @(#)RunLengthCodecSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.avi.codec.audio.*;
import org.monte.media.av.CodecSpi;

/**
 * RunLengthCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class RunLengthCodecSpi implements CodecSpi {

    @Override
    public RunLengthCodec create() {
       return new RunLengthCodec();
    }

}
