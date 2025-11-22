/*
 * @(#)AbstractAudioCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.audio;

import org.monte.media.av.Format;

/// `AbstractAudioCodec`.
///
/// @author Werner Randelshofer
public abstract class AbstractAudioCodec extends org.monte.media.av.AbstractCodec {

    public AbstractAudioCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }

    public AbstractAudioCodec(Format[] supportedInputOutputFormats) {
        super(supportedInputOutputFormats);
    }

}
