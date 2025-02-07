/*
 * @(#)Sampler.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.math.Rational;

/**
 * Samples an input media in constant time intervals.
 */
public interface Sampler extends AutoCloseable {
    @Override
    void close();

    /**
     * Samples the input media.
     *
     * @return the sample
     */
    Buffer sample();

    /**
     * The time interval of the sampler.
     *
     * @return time interval in seconds
     */
    Rational getInterval();
}
