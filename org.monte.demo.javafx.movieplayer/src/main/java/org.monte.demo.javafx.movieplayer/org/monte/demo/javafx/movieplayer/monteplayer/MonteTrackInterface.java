/*
 * @(#)MonteTrackInterface.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import org.monte.demo.javafx.movieplayer.model.TrackInterface;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

import java.util.concurrent.atomic.AtomicReference;

public interface MonteTrackInterface extends TrackInterface {
    Buffer getInBuffer();

    Buffer getOutBufferA();

    Buffer getOutBufferB();

    Format getFormat();

    /// Swaps the output buffers and returns outBufferA.
    ///
    /// @return outBufferA
    Buffer swapOutBuffers();

    Codec getCodec();

    void setCodec(Codec newValue);

    public Rational getRenderedEndTime();

    public void setRenderedEndTime(Rational seconds);

    public Rational getRenderedStartTime();

    public void setRenderedStartTime(Rational seconds);

    void dispose();

    AtomicReference<Rational> seekTime();

    void interruptWorker();
}
