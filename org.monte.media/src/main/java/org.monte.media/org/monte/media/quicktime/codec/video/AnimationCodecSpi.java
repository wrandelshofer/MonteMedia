/*
 * @(#)AnimationCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * AnimationCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class AnimationCodecSpi implements CodecSpi {

    @Override
    public AnimationCodec create() {
        return new AnimationCodec();
    }

}
