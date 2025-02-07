/*
 * @(#)QTFFParser.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff;

public class QTFFParser {
    /**
     * Convert an integer QTFF type to String.
     *
     * @param type type to be converted.
     * @return String representation of the ID.
     */
    public static String typeToString(int type) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (type >>> 24);
        bytes[1] = (byte) (type >>> 16);
        bytes[2] = (byte) (type >>> 8);
        bytes[3] = (byte) (type);

        return new String(bytes);
    }

    /**
     * Converts the first four letters of the String into a QTFF type.
     *
     * @param aString String to be converted.
     * @return ID representation of the String.
     */
    public static int stringToType(String aString) {
        byte[] bytes = aString.getBytes();

        return ((int) bytes[0]) << 24
                | ((int) bytes[1]) << 16
                | ((int) bytes[2]) << 8
                | ((int) bytes[3]);
    }
}
