/*
 * @(#)WrapEdgeAction.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

public class WrapEdgeAction implements EdgeAction {
    @Override
    public int map(int index, int size) {
        return index < 0 ? size + index : index >= size ? index - size : index;
    }
}
