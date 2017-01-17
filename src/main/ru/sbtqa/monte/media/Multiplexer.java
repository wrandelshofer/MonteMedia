/* @(#)Multiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package ru.sbtqa.monte.media;

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
     * Does nothing if the discard-flag in the buffer is set to true.
     *
     * @param track The track number.
     * @param buf The buffer containing the sample data.
     */
    public void write(int track, Buffer buf) throws IOException;

    /** Closes the Multiplexer. */
    public void close() throws IOException;
}
