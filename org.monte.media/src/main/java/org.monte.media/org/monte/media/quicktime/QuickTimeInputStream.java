/* @(#)QuickTimeInputStream.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Date;

/**
 * Provides low-level support for reading encoded audio and video samples from a
 * QuickTime file.
 *
 * @author Werner Randelshofer
 * @version $Id$
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
     */
    public QuickTimeInputStream(File file) throws IOException {

        this.in = new FileImageInputStream(file);
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        this.streamOffset = 0;
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     */
    public QuickTimeInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        this.streamOffset = in.getStreamPosition();
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
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
            duration = Math.max(duration, t.getTrackDuration(movieTimeScale));
        }
        return duration;
    }

    /**
     * Gets the creation time of the movie.
     */
    public Date getCreationTime() throws IOException {
        ensureRealized();
        return meta.getCreationTime();
    }

    /**
     * Gets the modification time of the movie.
     */
    public Date getModificationTime() throws IOException {
        ensureRealized();
        return meta.getModificationTime();
    }

    /**
     * Gets the preferred rate at which to play this movie. A value of 1.0
     * indicates normal rate.
     */
    public double getPreferredRate() throws IOException {
        ensureRealized();
        return meta.getPreferredRate();
    }

    /**
     * Gets the preferred volume of this movie’s sound. A value of 1.0 indicates
     * full volume.
     */
    public double getPreferredVolume() throws IOException {
        ensureRealized();
        return meta.getPreferredVolume();
    }

    /**
     * Gets the time value for current time position within the movie.
     */
    public long getCurrentTime() throws IOException {
        ensureRealized();
        return currentTime;
    }

    /**
     * Gets the time value of the time of the movie poster.
     */
    public long getPosterTime() throws IOException {
        ensureRealized();
        return meta.getPosterTime();
    }

    /**
     * Gets the duration of the movie preview in movie time scale units.
     */
    public long getPreviewDuration() throws IOException {
        ensureRealized();
        return meta.getPreviewDuration();
    }

    /**
     * Gets the time value in the movie at which the preview begins.
     */
    public long getPreviewTime() throws IOException {
        ensureRealized();
        return meta.getPreviewTime();
    }

    /**
     * Gets the transformation matrix of the entire movie.
     *
     * @return The transformation matrix.
     */
    public double[] getMovieTransformationMatrix() throws IOException {
        ensureRealized();
        return meta.getTransformationMatrix();
    }

    /**
     * Returns the time scale of the movie. <p> The movie time scale is used for
     * editing tracks. Such as for specifying the start time of a track.
     *
     * @return time scale
     */
    public long getMovieTimeScale() throws IOException {
        ensureRealized();
        return meta.getTimeScale();
    }

    /**
     * Returns the time scale of the media in a track. <p> The media time scale
     * is used for specifying the duration of samples in a track.
     *
     * @param track Track index.
     * @return time scale
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
     */
    public double[] getTransformationMatrix(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).matrix.clone();
    }

    /**
     * Ensures that all meta-data has been read from the file.
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
