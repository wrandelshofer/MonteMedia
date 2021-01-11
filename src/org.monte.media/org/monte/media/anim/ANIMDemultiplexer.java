/* @(#)ANIMDemultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.av.Demultiplexer;
import org.monte.media.av.Track;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Demultiplexes an ANIM file into a video track and an audio track.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ANIMDemultiplexer extends ANIMReader implements Demultiplexer {

    private Track[] tracks;

    public ANIMDemultiplexer(File file) throws IOException {
        super(file);
    }
    public ANIMDemultiplexer(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public Track[] getTracks() {
        if (tracks == null) {
            tracks = new Track[]{new ANIMVideoTrack(this)};
        }
        return tracks.clone();
    }
}
