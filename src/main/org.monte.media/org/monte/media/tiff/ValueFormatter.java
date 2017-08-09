/* @(#)ValueFormatter.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

package org.monte.media.tiff;

/**
 * ValueFormatter.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-03-22 Created.
 */
public interface ValueFormatter {
    /** Formats the specified value.
     * If the value is of the desired type, it is replaced by an object
     * which can be handled easier. For example, an integer value by a descriptive
     * String.
     */
    public Object format(Object value);
    /** Formats the specified value in a human readable format. */
    public Object prettyFormat(Object value);
    /** Describes the data. Returns null if no description is available. */
    public String descriptionFormat(Object data);
}
