/*
 * @(#)Multiplexer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

import java.io.Closeable;
import java.io.IOException;

/**
 * A {@code Multiplexer} can write multiple media tracks into a
 * single output stream.
 *
 * @author Werner Randelshofer
 */
public interface Multiplexer extends Closeable {
    /**
     * Adds a track.
     *
     * @param fmt The format of the track.
     * @return The track number.
     */
    int addTrack(Format fmt) throws IOException;

    void setCodec(int trackIndex, Codec codec);

    /**
     * Writes a sample.
     * Does nothing if the discard-flag or the prefetch-flag in the buffer is set to true.
     *
     * @param track The track number.
     * @param buf   The buffer containing the sample data.
     * @throws java.io.IOException if the write fails
     */
    void write(int track, Buffer buf) throws IOException;

    /**
     * Closes the Multiplexer.
     *
     * @throws java.io.IOException if close fails
     */
    void close() throws IOException;
}
