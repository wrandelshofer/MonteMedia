/*
 * @(#)SimpleSampleConsumer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.Multiplexer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleSampleConsumer implements SampleConsumer {
    private final BlockingQueue<Buffer> queue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Multiplexer mux;
    private final AtomicBoolean closed = new AtomicBoolean();

    public SimpleSampleConsumer(BlockingQueue<Buffer> queue, Multiplexer mux) {
        this.queue = queue;
        this.mux = mux;
    }

    @Override
    public void start() {
        executor.execute(() -> {
            while (!closed.get()) {
                try {
                    Buffer b = queue.poll(1, TimeUnit.SECONDS);
                    if (b != null) {
                        mux.write(b.track, b);
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    @Override
    public void close() {
        closed.set(true);
        executor.close();
        executor.shutdown();
        try {
            boolean success = executor.awaitTermination(1, TimeUnit.SECONDS);
            if (!success) {
                System.err.println("failed to await termination!");
            }
        } catch (InterruptedException e) {
            // bail
        }
        try {
            mux.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
