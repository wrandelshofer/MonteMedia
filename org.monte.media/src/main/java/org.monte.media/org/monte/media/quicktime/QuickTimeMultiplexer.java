/*
 * @(#)QuickTimeMultiplexer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.Multiplexer;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * {@code QuickTimeMultiplexer}.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeMultiplexer implements Multiplexer {
    private final QuickTimeWriter out;
    public QuickTimeMultiplexer(File file) throws IOException {
        this.out = new QuickTimeWriter(file);
    }

    /**
     * Creates a new QuickTime writer.
     *
     * @param out the underlying output stream.
     */
    public QuickTimeMultiplexer(ImageOutputStream out) throws IOException {
        this.out = new QuickTimeWriter(out);
    }

    /**
     * Adds a track.
     *
     * @param fmt The format of the track.
     * @return The track number.
     */
    @Override
    public int addTrack(Format fmt) throws IOException {
        return out.addTrack(fmt);
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        out.write(track, buf);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Sets the time scale for this movie, that is, the number of time units
     * that pass per second in its time coordinate system. <p> The default value
     * is 600.
     *
     * @param timeScale
     */
    public void setMovieTimeScale(long timeScale) {
        out.setMovieTimeScale(timeScale);
    }
}
