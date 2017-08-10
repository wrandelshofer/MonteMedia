/* @(#)AmigaBitmapCodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap.codec.video;

import org.monte.media.av.CodecSpi;

/**
 * AmigaBitmapCodecSpi.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AmigaBitmapCodecSpi implements CodecSpi {

    @Override
    public AmigaBitmapCodec create() {
       return new AmigaBitmapCodec();
    }

}
