/*
 * @(#)Point2D.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

public record Point2D(double x, double y) {
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
