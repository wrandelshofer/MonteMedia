/* @(#)ImageSequenceTrack.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package ru.sbtqa.monte.media.imgseq;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import static java.util.Arrays.sort;
import ru.sbtqa.monte.media.Buffer;
import static ru.sbtqa.monte.media.BufferFlag.*;
import ru.sbtqa.monte.media.Track;
import ru.sbtqa.monte.media.math.Rational;

/**
 * {@code ImageSequenceTrack}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class ImageSequenceTrack implements Track {


    private File[] files;
    private int position;
    private long duration=1;
    private long timeScale=30;

    public ImageSequenceTrack(File dir, FileFilter filter) throws IOException {
        files = dir.listFiles(filter);
        sort(files, new FileComparator());
    }

    public ImageSequenceTrack(File[] files) throws IOException {
        this.files = files.clone();
    }

    public long getFileDuration() {
        return duration;
    }
    public void setFileDuration(long newValue) {
        this.duration=newValue;
    }
    public long getTimeScale() {
        return timeScale;
    }
    public void setTimeScale(long newValue) {
        this.timeScale=newValue;
    }


    @Override
    public long getSampleCount() {
        return files.length;
    }

    @Override
    public void setPosition(long pos) {
        this.position=(int)pos;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void read(Buffer buf) throws IOException {
        if (position>=files.length) {
            buf.setFlagsTo(DISCARD);
            return;
        }
        buf.clearFlags();
        buf.data=files[position];
        buf.sampleDuration=new Rational(duration,timeScale);
        position++;
    }
}
