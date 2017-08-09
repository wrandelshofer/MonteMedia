/* @(#)AnimationCodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;
import org.monte.media.quicktime.codec.audio.QuickTimePCMAudioCodec;

/**
 * AnimationCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class AnimationCodecSpi implements CodecSpi {

    @Override
    public AnimationCodec create() {
       return new AnimationCodec();
    }

}
