/* @(#)BiIntConsumer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util.stream;

/**
 * BiIntConsumer.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
@FunctionalInterface
public interface BiIntConsumer {
    void accept(int left, int right);
}
