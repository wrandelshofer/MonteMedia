/* @(#)ANIMDemultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.anim;

import org.monte.media.av.Demultiplexer;
import org.monte.media.av.Track;
import java.io.File;
import java.io.IOException;

/**
 * {@code ANIMDemultiplexer}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class ANIMDemultiplexer extends ANIMReader implements Demultiplexer {

    private Track[] tracks;

    public ANIMDemultiplexer(File file) throws IOException {
        super(file);
    }

    @Override
    public Track[] getTracks() {
        if (tracks == null) {
            tracks = new Track[]{new ANIMTrack(this)};
        }
        return tracks.clone();
    }
}
