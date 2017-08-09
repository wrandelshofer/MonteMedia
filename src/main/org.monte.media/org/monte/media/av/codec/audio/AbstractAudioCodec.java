/* @(#)AbstractAudioCodec.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

package org.monte.media.av.codec.audio;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Format;

/**
 * {@code AbstractAudioCodec}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-10 Created.
 */
public abstract class AbstractAudioCodec extends AbstractCodec {

    public AbstractAudioCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }
    public AbstractAudioCodec(Format[] supportedInputOutputFormats) {
        super(supportedInputOutputFormats);
    }

}
