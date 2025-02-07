/*
 * @(#)SampleProducer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;

import java.util.concurrent.BlockingQueue;

/**
 * Runs the given samplers until it is closed.
 */
public interface SampleProducer extends AutoCloseable {
    /**
     * Starts the sampler.
     */
    public void start();

    @Override
    void close();

    /**
     * Gets the samples.
     *
     * @return the samples.
     */
    BlockingQueue<Buffer> getSamples();
}
