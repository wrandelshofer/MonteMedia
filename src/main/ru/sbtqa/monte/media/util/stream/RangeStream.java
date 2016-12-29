/* @(#)RangeStream.java
 * Copyright © 2016 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.util.stream;

import java.util.function.IntConsumer;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.getSurplusQueuedTaskCount;

/**
 * RangeStream processes a range of integer sequentially or in parallel
 * sub-ranges.
 *
 * @author Werner Randelshofer
 * @version $$Id: RangeStream.java 366 2016-11-09 20:08:18Z werner $$
 */
public class RangeStream {

    private final int startInclusive;
    private final int endExclusive;
    /**
     * Values larger than 0 mean paralle, values less or equal 0 mean serial.
     */
    int threshold;

    private RangeStream(int startInclusive, int endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    /**
     * Converts this stream to a parallel stream which will be recursively split
     * up into ranges down to a threshold of 128.
     *
     * @return this stream
     */
    public RangeStream parallel() {
        return parallel(128);
    }

    /**
     * Converts this stream to a parallel stream which will be recursively split
     * up into ranges down to the specified treshold.
     *
     * @param threshold for recursive splitting, a value smaller or equal 0
     * convert this stream back into a serial stream
     * @return this stream
     */
    public RangeStream parallel(int threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * Converts this stream back to a serial stream.
     *
     * @return this stream
     */
    public RangeStream serial() {
        return parallel(0);
    }

    /**
     * Invokes the consumer to process an integer from the range until the
     * entire reange is consumed.
     * 
     * If this stream is parallel, the consumer is invoked in parallel.
     * 
     * If this stream is sequential, the consumer is invoked sequentially.
     *
     * @param consumer TODO
     */
    public void forEach(IntConsumer consumer) {
        forEach((lo, hi) -> {
            for (int i = lo; i < hi; i++) {
                consumer.accept(i);
            }
        });
    }

    /**
     * Invokes the consumer to process sub-ranges until the entire range has
     * been processed.
     * 
     * If this stream is parallel, the range is split up into sub-ranges and the
     * consumer is invoked for each sub-range in parallel.
     * 
     * If this stream is sequential, the consumer is invoked with the entire
     * range.
     *
     * @param consumer TODO
     */
    public void forEach(BiIntConsumer consumer) {
        if (threshold > 0) {
            doParallel(consumer);
        } else {
            doSequential(consumer);
        }
    }

    private void doSequential(BiIntConsumer consumer) {
        consumer.accept(startInclusive, endExclusive);
    }

    private void doParallel(BiIntConsumer consumer) {
        new Applier(consumer, threshold, startInclusive, endExclusive, null).invoke();
    }

    /**
     * Returns a range stream which will execute the the integers from
     * startInclusive to endExclusive.
     *
     * @param startInclusive TODO
     * @param endExclusive TODO
     * @return RangeStream
     */
    public static RangeStream range(int startInclusive, int endExclusive) {
        return new RangeStream(startInclusive, endExclusive);
    }

    private static class Applier extends RecursiveAction {

        private final static long serialVersionUID = 0L;
        final BiIntConsumer consumer;
        final int thresh, lo, hi;
        Applier next; // keeps track of right-hand-side tasks

        Applier(BiIntConsumer consumer, int thresh, int lo, int hi, Applier next) {
            this.consumer = consumer;
            this.thresh = thresh;
            this.lo = lo;
            this.hi = hi;
            this.next = next;
        }

        void atLeaf(int l, int h) {
            consumer.accept(l, h);
        }

        protected void compute() {
            int l = lo;
            int h = hi;
            Applier right = null;
            while (h - l > thresh && getSurplusQueuedTaskCount() <= 3) {
                int mid = (l + h) >>> 1;
                right = new Applier(consumer, thresh, mid, h, right);
                right.fork();
                h = mid;
            }
            atLeaf(l, h);
            while (right != null) {
                if (right.tryUnfork()) // directly calculate if not stolen
                {
                    right.atLeaf(right.lo, right.hi);
                } else {
                    right.join();
                }
                right = right.next;
            }
        }
    }
}
