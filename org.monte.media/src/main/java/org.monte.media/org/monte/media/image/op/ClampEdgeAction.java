/*
 * @(#)ClampEdgeAction.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

public class ClampEdgeAction implements EdgeAction {
    @Override
    public int map(int index, int size) {
        return Math.clamp(index, 0, size - 1);
    }
}
