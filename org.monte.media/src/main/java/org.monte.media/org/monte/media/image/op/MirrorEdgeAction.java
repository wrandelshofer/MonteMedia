/*
 * @(#)MirrorEdgeAction.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

public class MirrorEdgeAction implements EdgeAction {
    @Override
    public int map(int index, int size) {
        var m = index < 0 ? -index : index >= size ? 2 * (size - 1) - index : index;
        return Math.clamp(m, 0, size - 1);
    }
}
