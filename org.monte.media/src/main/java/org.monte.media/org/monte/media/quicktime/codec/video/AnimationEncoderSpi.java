/*
 * @(#)AnimationEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * AnimationCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class AnimationEncoderSpi implements CodecSpi {

    @Override
    public AnimationEncoder create() {
        return new AnimationEncoder();
    }

}
