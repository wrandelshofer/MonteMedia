/* @(#)QuickTimeInputStream.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.quicktime;

import java.io.File;
import java.io.IOException;
import static java.lang.Math.max;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.util.Date;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * Provides low-level support for reading encoded audio and video samples from a
 * QuickTime file.
 *
 * @author Werner Randelshofer
 * @version $Id: QuickTimeInputStream.java 364 2016-11-09 19:54:25Z werner $
 */
public class QuickTimeInputStream {

    /**
     * The image input stream.
     */
    protected ImageInputStream in;
    /**
     * This variable is set to non-null, when the movie meta data has been read.
     */
    protected QuickTimeMeta meta = null;

    protected long streamOffset = 0;

    protected long currentTime = 0;

    /**
     * Creates a new instance.
     *
     * @param file the input file
     * @throws java.io.IOException TODO
     */
    public QuickTimeInputStream(File file) throws IOException {

        this.in = new FileImageInputStream(file);
        in.setByteOrder(BIG_ENDIAN);
        this.streamOffset = 0;
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     * @throws java.io.IOException TODO
     */
    public QuickTimeInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        this.streamOffset = in.getStreamPosition();
        in.setByteOrder(BIG_ENDIAN);
    }

    public int getTrackCount() throws IOException {
        ensureRealized();
        return meta.getTrackCount();
    }

    public long getMovieDuration() throws IOException {
        ensureRealized();
        long duration = 0;
        long movieTimeScale = meta.getTimeScale();
        for (QuickTimeMeta.Track t : meta.tracks) {
            duration = max(duration, t.getTrackDuration(movieTimeScale));
        }
        return duration;
    }

    /**
     * Gets the creation time of the movie.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Date getCreationTime() throws IOException {
        ensureRealized();
        return meta.getCreationTime();
    }

    /**
     * Gets the modification time of the movie.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Date getModificationTime() throws IOException {
        ensureRealized();
        return meta.getModificationTime();
    }

    /**
     * Gets the preferred rate at which to play this movie. A value of 1.0
     * indicates normal rate.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public double getPreferredRate() throws IOException {
        ensureRealized();
        return meta.getPreferredRate();
    }

    /**
     * Gets the preferred volume of this movie’s sound. A value of 1.0 indicates
     * full volume.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public double getPreferredVolume() throws IOException {
        ensureRealized();
        return meta.getPreferredVolume();
    }

    /**
     * Gets the time value for current time position within the movie.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getCurrentTime() throws IOException {
        ensureRealized();
        return currentTime;
    }

    /**
     * Gets the time value of the time of the movie poster.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getPosterTime() throws IOException {
        ensureRealized();
        return meta.getPosterTime();
    }

    /**
     * Gets the duration of the movie preview in movie time scale units.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getPreviewDuration() throws IOException {
        ensureRealized();
        return meta.getPreviewDuration();
    }

    /**
     * Gets the time value in the movie at which the preview begins.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getPreviewTime() throws IOException {
        ensureRealized();
        return meta.getPreviewTime();
    }

    /**
     * Gets the transformation matrix of the entire movie.
     *
     * @return The transformation matrix.
     * @throws java.io.IOException TODO
     */
    public double[] getMovieTransformationMatrix() throws IOException {
        ensureRealized();
        return meta.getTransformationMatrix();
    }

    /**
     * Returns the time scale of the movie.
     * 
     * The movie time scale is used for editing tracks. Such as for specifying
     * the start time of a track.
     *
     * @return time scale
     * @throws java.io.IOException TODO
     */
    public long getMovieTimeScale() throws IOException {
        ensureRealized();
        return meta.getTimeScale();
    }

    /**
     * Returns the time scale of the media in a track.
     * 
     * The media time scale is used for specifying the duration of samples in a
     * track.
     *
     * @param track Track index.
     * @return time scale
     * @throws java.io.IOException TODO
     */
    public long getMediaTimeScale(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).mediaList.get(0).mediaTimeScale;
    }

    /**
     * Returns the media duration of a track in the media's time scale.
     *
     * @param track Track index.
     * @return media duration
     * @throws java.io.IOException TODO
     */
    public long getMediaDuration(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).mediaList.get(0).mediaDuration;
    }

    /**
     * Gets the transformation matrix of the specified track.
     *
     * @param track The track number.
     * @return The transformation matrix.
     * @throws java.io.IOException TODO
     */
    public double[] getTransformationMatrix(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).matrix.clone();
    }

    /**
     * Ensures that all meta-data has been read from the file.
     *
     * @throws java.io.IOException TODO
     */
    protected void ensureRealized() throws IOException {
        if (in == null) {
            throw new IOException("Stream is closed.");
        }
        if (meta == null) {
            meta = new QuickTimeMeta();
            readAllMetadata();
        }
    }

    private void readAllMetadata() throws IOException {
        in.seek(streamOffset);
        QuickTimeDeserializer d = new QuickTimeDeserializer();
        d.parse(new QTFFImageInputStream(in), meta);
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
        if (meta != null) {
            meta = null;
        }
    }
}
