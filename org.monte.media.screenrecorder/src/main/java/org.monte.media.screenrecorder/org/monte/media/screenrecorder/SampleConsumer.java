/*
 * @(#)SampleConsumer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

public interface SampleConsumer extends AutoCloseable {
    void start();

    @Override
    void close();
}
