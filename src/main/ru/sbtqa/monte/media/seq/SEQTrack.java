/* @(#)SEQTrack.java
 * Copyright © 2011 Werner Randelshofer, Switzerland.
 * 
 * You may not use, copy or modify this file, except in compliance onlyWith the
 * license agreement you entered into onlyWith Werner Randelshofer.
 * For details see accompanying license terms.
 */

package ru.sbtqa.monte.media.seq;

import ru.sbtqa.monte.media.Buffer;
import ru.sbtqa.monte.media.Track;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.image.BitmapImage;
import ru.sbtqa.monte.media.math.Rational;
import java.io.IOException;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import static ru.sbtqa.monte.media.BufferFlag.*;

/**
 * {@code SEQTrack}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class SEQTrack implements Track {
    private SEQDemultiplexer demux;
    private long position;
    private Format outputFormat=new Format(MediaTypeKey,MediaType.VIDEO,MimeTypeKey,MIME_JAVA,EncodingKey,ENCODING_BUFFERED_IMAGE);

    public SEQTrack(SEQDemultiplexer demux) {
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
            buf.sampleDuration = new Rational(demux.getDuration((int)position), demux.getJiffies());
            buf.format=outputFormat;
            position++;
        } else {
            buf.setFlagsTo(DISCARD);
        }
    }
}
