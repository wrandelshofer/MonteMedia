/*
 * @(#)SimpleSampleConsumer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;

import java.util.concurrent.BlockingQueue;

public class SimpleSampleConsumer implements SampleConsumer {
    private final BlockingQueue<Buffer> queue;

    public SimpleSampleConsumer(BlockingQueue<Buffer> queue) {
        this.queue = queue;
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }
}
