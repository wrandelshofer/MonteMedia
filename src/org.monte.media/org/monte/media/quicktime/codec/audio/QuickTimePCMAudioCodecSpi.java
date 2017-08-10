/* @(#)QuickTimePCMAudioCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.audio;

import org.monte.media.av.CodecSpi;

/**
 * QuickTimePCMAudioCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class QuickTimePCMAudioCodecSpi implements CodecSpi {

    @Override
    public QuickTimePCMAudioCodec create() {
       return new QuickTimePCMAudioCodec();
    }

}
