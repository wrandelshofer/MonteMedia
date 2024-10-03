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
        return (obj instanceof byte[] && ((byte[]) obj).length >= minLength) ? (byte[]) obj : new byte[minLength];
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
        return (obj instanceof short[] && ((short[]) obj).length >= minLength) ? (short[]) obj : new short[minLength];
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
        return (obj instanceof int[] && ((int[]) obj).length >= minLength) ? (int[]) obj : new int[minLength];
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.
     *
     * @param original  the array to be copied
     * @param offset    the offset in the original array
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     * to obtain the specified length
     * @throws NegativeArraySizeException if {@code newLength} is negative
     * @throws NullPointerException       if {@code original} is null
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int offset, int newLength) {
        if (offset == 0 && newLength == original.length) {
            return original.clone();
        }
        byte[] copy = new byte[newLength];
        System.arraycopy(original, offset, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }
}
