/*
 * @(#)PlayerEngine.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import org.monte.media.av.Player;
import org.monte.media.math.Rational;

public interface PlayerEngine extends Player {
    void seek(Rational seconds);

    Rational getFrameAfter(Rational seconds);

    Rational getFrameBefore(Rational seconds);

    void setRate(float rate);
}
