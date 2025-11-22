/*
 * @(#)AbstractTextCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.text;

import org.monte.media.av.Format;

/// `AbstractTextCodec`.
///
/// @author Werner Randelshofer
public abstract class AbstractTextCodec extends org.monte.media.av.AbstractCodec {

    public AbstractTextCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }

}
