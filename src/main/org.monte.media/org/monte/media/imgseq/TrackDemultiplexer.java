/* @(#)TrackDemultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.imgseq;

import org.monte.media.av.Demultiplexer;
import org.monte.media.av.Track;
import java.io.IOException;

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
