/*
 * @(#)AbstractCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import java.util.ArrayList;

/**
 * {@code AbstractCodec}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractCodec implements Codec {

    protected Format[] inputFormats;
    protected Format[] outputFormats;
    protected Format inputFormat;
    protected Format outputFormat;
    protected String name = "unnamed codec";

    public AbstractCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        this.inputFormats = supportedInputFormats;
        this.outputFormats = supportedOutputFormats;
    }

    public AbstractCodec(Format[] supportedInputOutputFormats) {
        this.inputFormats = supportedInputOutputFormats;
        this.outputFormats = supportedInputOutputFormats;
    }

    @Override
    public Format[] getInputFormats() {
        return inputFormats.clone();
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        ArrayList<Format> of = new ArrayList<>(outputFormats.length);
        for (Format f : outputFormats) {
            of.add(input == null ? f : f.append(input));
        }
        return of.toArray(new Format[of.size()]);
    }

    @Override
    public Format setInputFormat(Format f) {
        if (f != null)
            for (Format sf : getInputFormats()) {
                if (sf.matches(f)) {
                    this.inputFormat = sf.append(f);
                    return inputFormat;
                }
            }
        this.inputFormat = null;
        return null;
    }

    @Override
    public Format setOutputFormat(Format f) {
        this.outputFormat = null;
        for (Format sf : getOutputFormats(f)) {
            if (sf.matches(f)) {
                this.outputFormat = f.append(sf);
            }
        }
        return this.outputFormat;
    }

    @Override
    public Format getInputFormat() {
        return inputFormat;
    }

    @Override
    public Format getOutputFormat() {
        return outputFormat;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Empty implementation of the reset method. Don't call super.
     */
    @Override
    public void reset() {
        // empty
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        int p = className.lastIndexOf('.');
        return className.substring(p + 1) + "{" + "inputFormat=" + inputFormat + ", outputFormat=" + outputFormat + '}';
    }


}
