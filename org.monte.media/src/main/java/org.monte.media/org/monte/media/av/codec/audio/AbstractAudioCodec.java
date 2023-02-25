/* @(#)AbstractAudioCodec.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.audio;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Format;

/**
 * {@code AbstractAudioCodec}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractAudioCodec extends AbstractCodec {

    public AbstractAudioCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }

    public AbstractAudioCodec(Format[] supportedInputOutputFormats) {
        super(supportedInputOutputFormats);
    }

}
