/* @(#)MovieReader.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

import java.io.IOException;
import ru.sbtqa.monte.media.math.Rational;

/**
 * A simple API for reading movie data (audio and video) from a file.
 *
 * 
 * FIXME - MovieReader should extend Demultiplexer
 *
 * @author Werner Randelshofer
 * @version $Id: MovieReader.java 364 2016-11-09 19:54:25Z werner $
 */
public interface MovieReader {

    /**
     * Returns the number of tracks.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public int getTrackCount() throws IOException;

    /**
     * Finds a track with the specified format.
     *
     * @param fromTrack the start track number.
     * @param format A format specification.
     * @return The track number &gt;= fromTrack or -1 if no track has been
     * found.
     * @throws java.io.IOException TODO
     */
    public int findTrack(int fromTrack, Format format) throws IOException;

    /**
     * Returns the total duration of the movie .
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Rational getDuration() throws IOException;

    /**
     * Returns the duration of the specified track.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Rational getDuration(int track) throws IOException;

    /**
     * Returns the sample number for the specified time.
     *
     * @param track TODO
     * @param seconds TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long timeToSample(int track, Rational seconds) throws IOException;

    /**
     * Returns the time for the specified sample number.
     *
     * @param track TODO
     * @param sample TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Rational sampleToTime(int track, long sample) throws IOException;

    /**
     * Returns the file format.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Format getFileFormat() throws IOException;

    /**
     * Returns the media format of the specified track.
     *
     * @param track Track number.
     * @return The media format of the track.
     * @throws java.io.IOException TODO
     */
    public Format getFormat(int track) throws IOException;

    /**
     * Returns the number of media data chunks in the specified track. A chunk
     * contains one or more samples.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getChunkCount(int track) throws IOException;

    /**
     * Reads the next sample chunk from the specified track.
     *
     * @param track Track number.
     * @param buffer The buffer into which to store the sample data.
     * @throws java.io.IOException TODO
     */
    public void read(int track, Buffer buffer) throws IOException;

    /**
     * Reads the next sample chunk from the next track in playback sequence. The
     * variable buffer.track contains the track number.
     *
     * @param buf The buffer into which to store the sample data.
     */
    //public void read(Buffer buffer) throws IOException;    

    /**
     * Reads the next sample chunk from the next track in playback sequence. The
     * variable buffer.track contains the track number.
     *
     * @param buf The buffer into which to store the sample data.
     */
    //public void read(Buffer buffer) throws IOException;
    /**
     * Returns the index of the next track in playback sequence.
     *
     * @return Index of next track or -1 if end of media reached.
     * @throws java.io.IOException TODO
     */
    public int nextTrack() throws IOException;

    public void close() throws IOException;

    /**
     * Sets the read time of all tracks to the closest sync sample before or at
     * the specified time.
     *
     * @param newValue Time in seconds.
     * @throws java.io.IOException TODO
     */
    public void setMovieReadTime(Rational newValue) throws IOException;

    /**
     * Returns the current time of the track.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public Rational getReadTime(int track) throws IOException;

}
