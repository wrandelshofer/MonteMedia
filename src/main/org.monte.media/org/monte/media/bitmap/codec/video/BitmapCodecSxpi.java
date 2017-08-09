/* @(#)BitmapCodecSxpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.bitmap.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * BitmapCodecSxpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class BitmapCodecSxpi implements CodecSpi {

    @Override
    public BitmapCodec create() {
       return new BitmapCodec();
    }

}
