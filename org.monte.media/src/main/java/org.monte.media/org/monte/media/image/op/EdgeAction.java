/*
 * @(#)EdgeAction.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

public interface EdgeAction {
    /**
     * Maps index to a save value between {@code 0} and {@code size -1}.
     *
     * @param index an index, in the range [-size,2*(size-1)]
     * @param size  the size
     * @return the save value
     */
    int map(int index, int size);
}
