/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.tiff;

/**
 * ValueFormatter.
 *
 * @author Werner Randelshofer
 */
public interface ValueFormatter {
    /**
     * Formats the specified value.
     * If the value is of the desired type, it is replaced by an object
     * which can be handled easier. For example, an integer value by a descriptive
     * String.
     */
    public Object format(Object value);

    /**
     * Formats the specified value in a human readable format.
     */
    public Object prettyFormat(Object value);

    /**
     * Describes the data. Returns null if no description is available.
     */
    public String descriptionFormat(Object data);
}
