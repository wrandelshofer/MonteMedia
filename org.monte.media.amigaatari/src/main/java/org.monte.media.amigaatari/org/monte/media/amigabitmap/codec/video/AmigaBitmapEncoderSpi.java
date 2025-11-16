/*
 * @(#)AmigaBitmapEncoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * AmigaBitmapCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class AmigaBitmapEncoderSpi implements CodecSpi {

    @Override
    public AmigaBitmapEncoder create() {
        return new AmigaBitmapEncoder();
    }

}
