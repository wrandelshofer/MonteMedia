/*
 * @(#)ImageUtil.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

public class ImageUtil {
    /**
     * Converts the provided object to {@code String}
     */
    public static String convertObjectToString(Object obj) {
        if (obj == null)
            return "";

        String s = "";
        if (obj instanceof byte[]) {
            byte[] bArray = (byte[]) obj;
            for (int i = 0; i < bArray.length; i++)
                s += bArray[i] + " ";
            return s;
        }

        if (obj instanceof int[]) {
            int[] iArray = (int[]) obj;
            for (int i = 0; i < iArray.length; i++)
                s += iArray[i] + " ";
            return s;
        }

        if (obj instanceof short[]) {
            short[] sArray = (short[]) obj;
            for (int i = 0; i < sArray.length; i++)
                s += sArray[i] + " ";
            return s;
        }

        return obj.toString();

    }
}
