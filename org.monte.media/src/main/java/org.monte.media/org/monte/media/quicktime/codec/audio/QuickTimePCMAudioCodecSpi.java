/*
 * @(#)QuickTimePCMAudioCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.audio;

import org.monte.media.av.CodecSpi;

/**
 * QuickTimePCMAudioCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class QuickTimePCMAudioCodecSpi implements CodecSpi {

    @Override
    public QuickTimePCMAudioCodec create() {
        return new QuickTimePCMAudioCodec();
    }

}
