/* @(#)BiIntConsumer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

package org.monte.media.util.stream;

/**
 * BiIntConsumer.
 *
 * @author Werner Randelshofer
 * @version $$Id: BiIntConsumer.java 364 2016-11-09 19:54:25Z werner $$
 */
@FunctionalInterface
public interface BiIntConsumer {
void accept(int left, int right);
}
