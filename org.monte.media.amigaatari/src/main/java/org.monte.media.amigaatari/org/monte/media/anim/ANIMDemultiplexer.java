/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.av.Demultiplexer;
import org.monte.media.av.Track;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Demultiplexes an ANIM file into a video track and an audio track.
 *
 * @author Werner Randelshofer
 */
public class ANIMDemultiplexer extends ANIMReader implements Demultiplexer {

    private Track[] tracks;

    private boolean swapLeftRightChannels;

    public ANIMDemultiplexer(File file) throws IOException {
        super(file);
    }

    public ANIMDemultiplexer(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public Track[] getTracks() {
        if (tracks == null) {
            List<Track> trackList = new ArrayList<>();

            ANIMAudioTrack e = new ANIMAudioTrack(this, swapLeftRightChannels);
            if (e.getSampleCount() != 0) {
                trackList.add(e);
            }
            trackList.add(new ANIMVideoTrack(this));
            tracks = trackList.toArray(new Track[0]);
        }
        return tracks.clone();
    }


    public boolean isSwapLeftRightChannels() {
        return swapLeftRightChannels;
    }

    public void setSwapLeftRightChannels(boolean swapLeftRightChannels) {
        this.swapLeftRightChannels = swapLeftRightChannels;
    }
}
