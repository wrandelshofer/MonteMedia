/*
 * @(#)MovieReader.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;

import java.io.IOException;

/**
 * A simple API for reading movie data (audio and video) from a file.
 *
 * <p>
 * FIXME - MovieReader should extend Demultiplexer
 *
 * @author Werner Randelshofer
 */
public interface MovieReader extends AutoCloseable {
    /**
     * Returns the number of tracks.
     */
    int getTrackCount() throws IOException;

    /**
     * Finds a track with the specified format.
     *
     * @param fromTrack the start track number.
     * @param format    A format specification.
     * @return The track number &gt;= fromTrack or -1 if no track has been found.
     */
    int findTrack(int fromTrack, Format format) throws IOException;

    /**
     * Returns the total duration of the movie .
     */
    Rational getDuration() throws IOException;

    /**
     * Returns the duration of the specified track.
     */
    Rational getDuration(int track) throws IOException;

    /**
     * Returns the sample number for the specified time.
     */
    long timeToSample(int track, Rational seconds) throws IOException;

    /**
     * Returns the time for the specified sample number.
     */
    Rational sampleToTime(int track, long sample) throws IOException;

    /**
     * Returns the file format.
     */
    Format getFileFormat() throws IOException;

    /**
     * Returns the media format of the specified track.
     *
     * @param track Track number.
     * @return The media format of the track.
     */
    Format getFormat(int track) throws IOException;

    /**
     * Returns the number of media data chunks in the specified track.
     * A chunk contains one or more samples.
     */
    long getChunkCount(int track) throws IOException;

    /**
     * Reads the next sample chunk from the specified track.
     *
     * @param track  Track number.
     * @param buffer The buffer into which to store the sample data.
     */
    void read(int track, Buffer buffer) throws IOException;

    /**
     * Returns the index of the next track in playback sequence.
     *
     * @return Index of next track or -1 if end of media reached.
     */
    int nextTrack() throws IOException;

    void close() throws IOException;

    /**
     * Sets the read time of all tracks to the closest sync sample before or
     * at the specified time.
     *
     * @param newValue Time in seconds.
     */
    void setMovieReadTime(Rational newValue) throws IOException;

    /**
     * Returns the current time of the track.
     */
    Rational getReadTime(int track) throws IOException;

}
