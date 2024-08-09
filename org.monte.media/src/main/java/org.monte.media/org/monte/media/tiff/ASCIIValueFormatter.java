/*
 * @(#)ASCIIValueFormatter.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.nio.charset.StandardCharsets;

/**
 * Formats byte arrays as string.
 *
 * @author Werner Randelshofer
 */
public class ASCIIValueFormatter implements ValueFormatter {

    /**
     * Creates a new enumeration.
     * The enumeration consists of a list of String=Integer pairs.
     */
    public ASCIIValueFormatter() {
    }

    @Override
    public Object format(Object value) {
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.US_ASCII);
        }
        return value;
    }

    @Override
    public Object prettyFormat(Object value) {
        return format(value);
    }

    @Override
    public String descriptionFormat(Object data) {
        return null;
    }
}
