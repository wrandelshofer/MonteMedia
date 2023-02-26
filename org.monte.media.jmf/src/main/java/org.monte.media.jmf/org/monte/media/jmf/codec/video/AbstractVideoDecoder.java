/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.jmf.codec.video;

import org.monte.media.jmf.codec.AbstractCodec;

import javax.media.Format;
import javax.media.format.VideoFormat;

/**
 * {@code AbstractVideoDecoder}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractVideoDecoder extends AbstractCodec {

    protected VideoFormat[] defaultOutputFormats = new VideoFormat[0];
    protected VideoFormat[] supportedInputFormats = new VideoFormat[0];
    protected VideoFormat inputFormat;
    protected VideoFormat outputFormat;


    @Override
    public Format[] getSupportedInputFormats() {
        return supportedInputFormats.clone();
    }

    @Override
    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return defaultOutputFormats.clone();
        }

        Format[] sop = getMatchingOutputFormats(input);
        return sop;
    }

    protected abstract Format[] getMatchingOutputFormats(Format input);

    @Override
    public Format setInputFormat(Format format) {
        inputFormat = (VideoFormat) format;
        return inputFormat;
    }

    @Override
    public Format setOutputFormat(Format format) {
        Format[] ops = getSupportedOutputFormats(inputFormat);

        outputFormat = null;
        for (Format f : ops) {
            if (f.matches(format)) {
                outputFormat = (VideoFormat) f;
                break;
            }
        }
        return outputFormat;
    }

    protected VideoFormat getInputFormat() {
        return inputFormat;
    }

    protected VideoFormat getOutputFormat() {
        return outputFormat;
    }

}
