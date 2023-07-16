/*
 * @(#)TechSmithCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text;

import org.monte.media.av.CodecSpi;

/**
 * TechSmithCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class AppleClosedCaptionCodecSpi implements CodecSpi {

    @Override
    public AppleClosedCaptionCodec create() {
        return new AppleClosedCaptionCodec();
    }

}
