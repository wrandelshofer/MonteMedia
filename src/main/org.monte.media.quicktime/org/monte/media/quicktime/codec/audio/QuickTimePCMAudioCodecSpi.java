/* @(#)QuickTimePCMAudioCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
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
