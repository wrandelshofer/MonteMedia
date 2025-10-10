/*
 * @(#)SynchronizedMovieReader.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

import org.monte.media.math.Rational;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class SynchronizedMovieReader implements MovieReader {
    private final MovieReader target;

    public SynchronizedMovieReader(MovieReader target) {
        this.target = target;
    }

    @Override
    public synchronized int getSampleCount(int track) throws IOException {
        return target.getSampleCount(track);
    }

    @Override
    public synchronized int getTrackCount() throws IOException {
        return target.getTrackCount();
    }

    @Override
    public synchronized int findTrack(int fromTrack, Format format) throws IOException {
        return target.findTrack(fromTrack, format);
    }

    @Override
    public synchronized Rational getMovieDuration() throws IOException {
        return target.getMovieDuration();
    }

    @Override
    public synchronized Rational getTrackDuration(int track) throws IOException {
        return target.getTrackDuration(track);
    }

    @Override
    public synchronized long findSampleAtTime(int track, Rational seconds) throws IOException {
        return target.findSampleAtTime(track, seconds);
    }

    @Override
    public synchronized Rational getSampleTime(int track, long sample) throws IOException {
        return target.getSampleTime(track, sample);
    }

    @Override
    public synchronized Rational getSampleDuration(int track, long sample) throws IOException {
        return target.getSampleDuration(track, sample);
    }

    @Override
    public synchronized Format getFileFormat() throws IOException {
        return target.getFileFormat();
    }

    @Override
    public synchronized Format getFormat(int track) throws IOException {
        return target.getFormat(track);
    }

    @Override
    public synchronized long getChunkCount(int track) throws IOException {
        return target.getChunkCount(track);
    }

    @Override
    public synchronized void read(int track, Buffer buffer) throws IOException {
        target.read(track, buffer);
    }

    @Override
    public synchronized BufferedImage read(int track, BufferedImage img) throws IOException {
        return target.read(track, img);
    }

    @Override
    public synchronized int nextTrack() throws IOException {
        return target.nextTrack();
    }

    @Override
    public synchronized void close() throws IOException {
        target.close();
    }

    @Override
    public synchronized void setMovieReadTime(Rational newValue) throws IOException {
        target.setMovieReadTime(newValue);
    }

    @Override
    public synchronized void setTrackReadTime(int track, Rational newValue) throws IOException {
        target.setTrackReadTime(track, newValue);
    }

    @Override
    public synchronized Rational getReadTime(int track) throws IOException {
        return target.getReadTime(track);
    }
}
