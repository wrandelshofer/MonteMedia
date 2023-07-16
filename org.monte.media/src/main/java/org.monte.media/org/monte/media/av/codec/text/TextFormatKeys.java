/*
 * @(#)VideoFormatKeys.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.text;

import org.monte.media.av.FormatKeys;

/**
 * Defines common format keys for video media.
 *
 * @author Werner Randelshofer
 */
public class TextFormatKeys extends FormatKeys {
    // Standard text ENCODING strings for use with FormatKey.Encoding.
    public static final String ENCODING_STRING = "String";
    /**
     * Apple Closed Caption format.
     */
    public static final String ENCODING_CLOSED_CAPTION = "clcp";
}
