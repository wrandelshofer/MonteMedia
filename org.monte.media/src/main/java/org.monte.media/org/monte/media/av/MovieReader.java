/*
 * @(#)MovieReader.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A simple API for reading movie data (audio and video) from a file.
 *
 * @author Werner Randelshofer
 */
public interface MovieReader extends AutoCloseable {
    /**
     * Gets the number of a samples of the specified track.
     *
     * @param track The track index.
     * @return the number of samples in the track
     * @throws IOException if reading the sample data failed.
     */
    int getSampleCount(int track) throws IOException;

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
    Rational getMovieDuration() throws IOException;

    /**
     * Returns the duration of the specified track.
     */
    Rational getTrackDuration(int track) throws IOException;

    /**
     * Returns the sample number for a given time.
     * <p>
     * The following cases can occur:
     * <dl>
     *     <dt>The track starts after the specified time.</dt>
     *     <dd>In this case the method returns the index of the first sample in the track.</dd>
     *
     *     <dt>The track ends before the specified time.</dt>
     *     <dd>In this case the method returns the index of the last sample in the track.</dd>
     *
     *     <dt>The track starts before the specified time and ends after the specified time.</dt>
     *     <dd>In this case the method returns the index of the first sample that
     *     intersects with the time.</dd>
     * </dl>
     *
     * @param track the track number
     * @param seconds the time in seconds
     * @return the sample number
     * @throws IOException on IO failure
     */
    long findSampleAtTime(int track, Rational seconds) throws IOException;

    /**
     * Returns the movie time for the specified sample number.
     * <p>
     * There can be multiple samples at the same time.
     * In this case, all except the last sample at this time are used
     * for prefetching.
     *
     * @param track the track number
     * @param sample the sample number
     * @return the sample time
     * @throws IOException on IO failure
     */
    Rational getSampleTime(int track, long sample) throws IOException;

    /**
     * Returns the duration of the specified sample.
     *
     * @param track the track number
     * @param sample the sample number
     * @return the duration of the specified sample
     * @throws IOException on IO failure
     */
    Rational getSampleDuration(int track, long sample) throws IOException;

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
     * Reads an image.
     *
     * @param track The track number
     * @param img   An image that can be reused if it fits the media format of the
     *              track. Pass null to create a new image on each read.
     * @return An image or null if the end of the media has been reached.
     * @throws IOException on IO failure
     */
    BufferedImage read(int track, BufferedImage img) throws IOException;
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
