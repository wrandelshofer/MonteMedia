/* @(#)Track.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import java.io.IOException;

/**
 * A {@code Track} refers to media data that can be interpreted in a time
 * coordinate system.
 *
 * @author Werner Randelshofer
 * @version $Id$
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
