/*
 * @(#)CDXLImageDecoderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.av.CodecSpi;

/// AmigaBitmapCodecSpi.
///
/// @author Werner Randelshofer
public class CDXLImageDecoderSpi implements CodecSpi {

    @Override
    public CDXLImageDecoder create() {
        return new CDXLImageDecoder();
    }

}
