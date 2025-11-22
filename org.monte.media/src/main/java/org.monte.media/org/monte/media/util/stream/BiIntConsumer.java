/*
 * @(#)BiIntConsumer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util.stream;

/// BiIntConsumer.
///
/// @author Werner Randelshofer
@FunctionalInterface
public interface BiIntConsumer {
    void accept(int left, int right);
}
