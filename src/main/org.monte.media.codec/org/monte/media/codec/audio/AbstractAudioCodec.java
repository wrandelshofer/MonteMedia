/* @(#)AbstractAudioCodec.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.codec.audio;

import org.monte.media.codec.AbstractCodec;
import org.monte.media.codec.Format;

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
