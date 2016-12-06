/* @(#)Track.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

import java.io.IOException;

/**
 * A {@code Track} refers to media data that can be interpreted in a time
 * coordinate system.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public interface Track {

    /**
     * Returns the numbers of samples in this track.
     *
     * @return sample count
     */
    public long getSampleCount();

    /**
     * Sets the read position.
     *
     * @param pos desired position
     */
    public void setPosition(long pos);

    /**
     * Gets the read position.
     *
     * @return the current position
     */
    public long getPosition();

    /**
     * Reads a sample from the input stream. If the end of the track is reached,
     * the discard-flag in the buffer is set to true.
     *
     * @param buf The buffer for the sample.
     * @throws java.io.IOException if an error occurs
     */
    public void read(Buffer buf) throws IOException;
}
