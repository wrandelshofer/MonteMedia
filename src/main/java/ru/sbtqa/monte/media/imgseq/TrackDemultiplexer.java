/* @(#)TrackDemultiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.imgseq;

import java.io.IOException;
import ru.sbtqa.monte.media.Demultiplexer;
import ru.sbtqa.monte.media.Track;

/**
 * Can "demultiplex" an array of already demultiplexed tracks.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class TrackDemultiplexer implements Demultiplexer {

    private Track[] tracks;

    public TrackDemultiplexer(Track[] tracks) {
        this.tracks = tracks.clone();
    }

    @Override
    public Track[] getTracks() {
        return tracks.clone();
    }

    @Override
    public void close() throws IOException {
    }
}
