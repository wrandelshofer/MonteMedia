/*
 * @(#)PiecewiseToneCurve.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

/// Piecewise tone mapping curve
/// Uses unsigned short (`char`) array for mapping from linear to curved.
public record PiecewiseToneCurve(char[] points) implements ToneCurve {
    public PiecewiseToneCurve(char[] points) {
        this.points = points;
    }

    public PiecewiseToneCurve(short[] points) {
        this(new char[points.length]);
        for (int i = 0; i < points.length; i++) {
            this.points[i] = (char) points[i];
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("PiecewiseToneCurve[points=");
        for (int i = 0; i < points.length; i++) {
            if (i != 0) b.append(", ");
            b.append((int) points[i]);
        }
        b.append(']');
        return b.toString();
    }
}
