/*
 * @(#)ArrayUtil.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

/**
 * Provides utility methods for byte arrays.
 */
public class ArrayUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private ArrayUtil() {
    }

    /**
     * Reuses the provided object if it is a {@code byte} array with a length that is greater or equal the specified
     * minimal length.
     *
     * @param obj       an object
     * @param minLength the minimal array length
     * @return the object if it is an array of the desired type with the specified minimal length, or a new array
     */
    public static byte[] reuseByteArray(Object obj, int minLength) {
        return (obj instanceof byte[] b && b.length >= minLength) ? b : new byte[minLength];
    }

    /**
     * Reuses the provided object if it is a {@code short} array with a length that is greater or equal the specified
     * minimal length.
     *
     * @param obj       an object
     * @param minLength the minimal array length
     * @return the object if it is an array of the desired type with the specified minimal length, or a new array
     */
    public static short[] reuseShortArray(Object obj, int minLength) {
        return (obj instanceof short[] b && b.length >= minLength) ? b : new short[minLength];
    }

    /**
     * Reuses the provided object if it is a {@code int} array with a length that is greater or equal the specified
     * minimal length.
     *
     * @param obj       an object
     * @param minLength the minimal array length
     * @return the object if it is an array of the desired type with the specified minimal length, or a new array
     */
    public static int[] reuseIntArray(Object obj, int minLength) {
        return (obj instanceof int[] b && b.length >= minLength) ? b : new int[minLength];
    }
}
