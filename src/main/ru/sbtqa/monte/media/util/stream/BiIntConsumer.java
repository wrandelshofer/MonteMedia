/* @(#)BiIntConsumer.java
 * Copyright © 2016 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package ru.sbtqa.monte.media.util.stream;

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
