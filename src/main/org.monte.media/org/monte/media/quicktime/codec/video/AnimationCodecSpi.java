/* @(#)AnimationCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.quicktime.codec.video;

import org.monte.media.av.CodecSpi;

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
