/* @(#)ANIMTrack.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

package org.monte.media.anim;

import java.util.EnumSet;
import org.monte.media.av.Buffer;
import org.monte.media.av.Track;
import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.math.Rational;
import static org.monte.media.av.BufferFlag.*;

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
            if (!(buf.data instanceof AmigaBitmapImage)) {
                buf.data = demux.createCompatibleBitmap();
            }
            demux.readFrame((int)position,(AmigaBitmapImage) buf.data);
            buf.sampleDuration = new Rational(demux.getDuration((int)position),demux.getJiffies());
            position++;
        } else {
            buf.setFlagsTo(DISCARD);
        }
    }
}
