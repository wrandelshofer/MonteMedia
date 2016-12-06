/* @(#)SEQDemultiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.seq;

import java.io.File;
import java.io.IOException;
import ru.sbtqa.monte.media.Demultiplexer;
import ru.sbtqa.monte.media.Track;

/**
 * {@code SEQDemultiplexer}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class SEQDemultiplexer extends SEQReader implements Demultiplexer {

    private Track[] tracks;

    public SEQDemultiplexer(File file) throws IOException {
        super(file);
    }

    public SEQDemultiplexer(File file, boolean variableFramerate) throws IOException {
        super(file, variableFramerate);
    }

    @Override
    public Track[] getTracks() {
        if (tracks == null) {
            tracks = new Track[]{new SEQTrack(this)};
        }
        return tracks.clone();
    }
}
