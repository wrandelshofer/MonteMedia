/* @(#)ANIMTrack.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.anim;

import java.util.EnumSet;
import org.monte.media.Buffer;
import org.monte.media.Track;
import org.monte.media.image.BitmapImage;
import org.monte.media.math.Rational;
import static org.monte.media.BufferFlag.*;

/**
 * {@code ANIMTrack}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class ANIMTrack implements Track {
    private ANIMDemultiplexer demux;
    private long position;

    public ANIMTrack(ANIMDemultiplexer demux) {
        this.demux=demux;
    }

    @Override
    public long getSampleCount() {
       return demux.getFrameCount();
    }

    @Override
    public void setPosition(long pos) {
       this.position=pos;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void read(Buffer buf) {
        if (position < demux.getFrameCount()) {
            buf.setFlagsTo(KEYFRAME);
            if (!(buf.data instanceof BitmapImage)) {
                buf.data = demux.createCompatibleBitmap();
            }
            demux.readFrame((int)position,(BitmapImage) buf.data);
            buf.sampleDuration = new Rational(demux.getDuration((int)position),demux.getJiffies());
            position++;
        } else {
            buf.setFlagsTo(DISCARD);
        }
    }
}
