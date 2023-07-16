/*
 * @(#)AbstractVideoCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.text;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Format;

/**
 * {@code AbstractTextCodec}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractTextCodec extends AbstractCodec {

    public AbstractTextCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }

}
