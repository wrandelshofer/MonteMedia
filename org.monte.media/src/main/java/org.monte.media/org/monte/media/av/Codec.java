/*
 * @(#)Codec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

/**
 * A {@code Codec} processes a {@code Buffer} and stores the result in another
 * {@code Buffer}.
 *
 * @author Werner Randelshofer
 */
public interface Codec {

    /**
     * The codec successfully converted the input to output.
     */
    int CODEC_OK = 0;
    /**
     * The codec could not handle the input.
     */
    int CODEC_FAILED = 1;
    /**
     * The codec did not fully consume the input buffer.
     * The codec has updated the input buffer to
     * reflect the amount of data that it has processed.
     * The codec must be called again with the same input buffer.
     */
    int CODEC_INPUT_NOT_CONSUMED = 2;
    /**
     * The codec did not fully fill the output buffer.
     * The codec has updated the output buffer to
     * reflect the amount of data that it has processed.
     * The codec must be called again with the same output buffer.
     */
    int CODEC_OUTPUT_NOT_FILLED = 4;

    /**
     * Lists all the input formats that this codec accepts.
     */
    Format[] getInputFormats();

    /**
     * Lists all the output formats that this codec can generate
     * with the provided input format. If the input format is null, returns
     * all supported output formats.
     */
    Format[] getOutputFormats(Format input);

    /**
     * Sets the input format.
     * Returns the format that was actually set. This is the closest format
     * that the Codec supports. Returns null if the specified format is not
     * supported and no reasonable match could be found.
     */
    Format setInputFormat(Format input);

    Format getInputFormat();

    /**
     * Sets the output format.
     * Returns the format that was actually set. This is the closest format
     * that the Codec supports. Returns null if the specified format is not
     * supported and no reasonable match could be found.
     */
    Format setOutputFormat(Format output);

    Format getOutputFormat();

    /**
     * Performs the media processing defined by this codec.
     * <p>
     * Copies the data from the input buffer into the output buffer.
     *
     * @return A combination of processing flags.
     */
    int process(Buffer in, Buffer out);

    /**
     * Returns a human-readable name of the codec.
     */
    String getName();

    /**
     * Resets the state of the codec.
     */
    void reset();
}
