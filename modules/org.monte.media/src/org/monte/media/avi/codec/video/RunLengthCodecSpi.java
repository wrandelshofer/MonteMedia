/* @(#)RunLengthCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.avi.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * RunLengthCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class RunLengthCodecSpi implements CodecSpi {

    @Override
    public RunLengthCodec create() {
       return new RunLengthCodec();
    }

}
