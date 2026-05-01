/*
 * @(#)PiecewiseToneCurve.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

/// Piecewise tone mapping curve
public record PiecewiseToneCurve(char[] points) implements ToneCurve {
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
