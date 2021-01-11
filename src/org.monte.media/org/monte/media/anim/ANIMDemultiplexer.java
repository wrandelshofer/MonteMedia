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
            ANIMMovieResources res = getResources();
            for (int i = 0, n = res.getFrameCount(); i<n; i++) {
                ANIMFrame frame = res.getFrame(i);
                for (ANIMAudioCommand cmd : frame.getAudioCommands()) {
                    switch (cmd.getCommand()) {
                    case ANIMAudioCommand.COMMAND_PLAY_SOUND:
                        case ANIMAudioCommand.COMMAND_SET_FREQVOL:
                            case ANIMAudioCommand.COMMAND_STOP_SOUND:
                                System.out.println("AudioCommand "+cmd.getCommand());
                                break;
                    default:
                        break;
                    }
                }

            }

            tracks = new Track[]{new ANIMVideoTrack(this)};
        }
        return tracks.clone();
    }
}
