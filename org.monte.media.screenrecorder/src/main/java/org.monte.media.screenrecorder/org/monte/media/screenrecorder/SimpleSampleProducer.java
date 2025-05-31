/*
 * @(#)SimpleSampleProducer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleSampleProducer implements SampleProducer {
    private final SequencedSet<Sampler> samplers;
    private ScheduledExecutorService executor;
    private BlockingQueue<Buffer> queue = new LinkedBlockingQueue<>();

    public SimpleSampleProducer(Collection<Sampler> samplers) {
        this.samplers = new LinkedHashSet<>(samplers);
    }

    @Override
    public void close() {
        if (executor == null) {
            return;
        }
        executor.close();
        executor.shutdown();
        try {
            boolean success = executor.awaitTermination(2, TimeUnit.SECONDS);
            if (!success) {
                System.err.println("failed to await termination");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            //bail
        }
        for (var s : samplers) {
            s.close();
        }
        executor = null;
    }

    @Override
    public void start() {
        close();
        executor = Executors.newScheduledThreadPool(samplers.size());
        for (var s : samplers) {
            executor.scheduleAtFixedRate(() -> {
                var b = s.sample();
                queue.add(b);
            }, s.getInitialDelay().multiply(1_000).intValue(), s.getInterval().multiply(1_000).intValue(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public BlockingQueue<Buffer> getSamples() {
        return queue;
    }
}
