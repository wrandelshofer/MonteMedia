/*
 * @(#)Demultiplexer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

import java.io.IOException;

/**
 * A {@code Demultiplexer} takes a data source with multiplexed media
 * as an input and outputs the media in individual tracks.
 *
 * @author Werner Randelshofer
 */
public interface Demultiplexer {
    /**
     * Returns the tracks.
     *
     * @return the tracks
     */
    public Track[] getTracks();

    /**
     * Closes the Demultiplexer.
     *
     * @throws java.io.IOException if closing fails
     */
    public void close() throws IOException;
}
