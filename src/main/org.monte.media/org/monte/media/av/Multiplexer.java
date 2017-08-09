/* @(#)Multiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

package org.monte.media.av;

import java.io.IOException;

/**
 * A {@code Multiplexer} can write multiple media tracks into a
 * single output stream.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-19 Created.
 */
public interface Multiplexer {
    /** Writes a sample.
     * Does nothing if the discard-flag or the prefetch-flag in the buffer is set to true.
     *
     * @param track The track number.
     * @param buf The buffer containing the sample data.
     * @throws java.io.IOException if the write fails
     */
    public void write(int track, Buffer buf) throws IOException;

    /** Closes the Multiplexer.
     * @throws java.io.IOException if close fails */
    public void close() throws IOException;
}
