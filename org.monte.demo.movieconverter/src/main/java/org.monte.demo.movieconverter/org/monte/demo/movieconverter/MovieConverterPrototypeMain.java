/* @(#)MovieConverterPrototypeMain.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.movieconverter;

import org.monte.media.av.Buffer;

/**
 * {@code MovieConverterPrototypeMain}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MovieConverterPrototypeMain {
    private static class MovieReader {

        // Direct access to samples
        // Is this consistent with the write methods?
        public long getSampleCount(int track) {
            return 1;
        }

        public void readSample(int track, long sampleIndex, Buffer buf) {
        }

        public void readSamples(int track, long sampleIndex, int sampleCount, Buffer buf) {
        }

        public long movieTimeToSample(long time) {
            return 1;
        }

        public long timeToSample(int track, long time) {
            return 1;
        }

        public long sampleToTime(int track, long sample) {
            return 1;
        }

        public long sampleToMovieTime(long sample) {
            return 1;
        }


        // Timed access to samples
        public int getTrackCount() {
            return 1;
        }

        public long getMovieDuration() {
            return 1;
        }

        public long getMovieTimeScale() {
            return 1;
        }

        public void setMovieStartTime(long time) {
        }

        public void setMovieEndTime(long time) {
        }

        public void setMovieTime(long time) {
        }

        public long getTimeScale(int track) {
            return 1;
        }

        public long getDuration(int track) {
            return 1;
        }

        public long getStartTime(int track) {
            return 1;
        }

        public void read(int track, Buffer buf) {
        }
    }
}
