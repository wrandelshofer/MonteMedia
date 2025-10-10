/*
 * @(#)AmigaBitmapCodecSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.av.CodecSpi;

/**
 * AmigaBitmapCodecSpi.
 *
 * @author Werner Randelshofer
 */
public class CDXLImageDecoderSpi implements CodecSpi {

    @Override
    public CDXLImageDecoder create() {
        return new CDXLImageDecoder();
    }

}
