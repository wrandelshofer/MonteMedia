/* @(#)Demultiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

import java.io.IOException;

/**
 * A {@code Demultiplexer} takes a data source with multiplexed media as an
 * input and outputs the media in individual tracks.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-19 Created.
 */
public interface Demultiplexer {

    /**
     * Returns the tracks.
     *
     * @return TODO
     */
    public Track[] getTracks();

    /**
     * Closes the Demultiplexer.
     *
     * @throws java.io.IOException TODO
     */
    public void close() throws IOException;
}
