/*
 * @(#)ByteArrays.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * Utility methods for reading/writing primitive values into byte arrays.
 */
public class ByteArrays {
    private static final VarHandle SHORT_LE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle SHORT_BE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle INT_LE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle INT_BE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);

    private static final VarHandle LONG_LE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle LONG_BE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);


    /**
     * Don't let anyone instantiate this class.
     */
    private ByteArrays() {

    }


    /**
     * Reads a short in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static short getShortBE(byte[] array, int offset) {
        return (short) SHORT_BE.get(array, offset);
    }


    /**
     * Reads a short in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static short getShortLE(byte[] array, int offset) {
        return (short) SHORT_LE.get(array, offset);
    }

    /**
     * Reads a short in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static int getUShortBE(byte[] array, int offset) {
        return Short.toUnsignedInt((short) SHORT_BE.get(array, offset));
    }


    /**
     * Reads a short in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static int getUShortLE(byte[] array, int offset) {
        return Short.toUnsignedInt((short) SHORT_LE.get(array, offset));
    }

    /**
     * Reads an int in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static int getIntBE(byte[] array, int offset) {
        return (int) INT_BE.get(array, offset);
    }

    /**
     * Reads an int in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static int getIntLE(byte[] array, int offset) {
        return (int) INT_LE.get(array, offset);
    }

    /**
     * Reads a long in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static long getLongBE(byte[] array, int offset) {
        return (long) LONG_BE.get(array, offset);
    }

    /**
     * Reads a long in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @return the value
     */
    public static long getLongLE(byte[] array, int offset) {
        return (long) LONG_LE.get(array, offset);
    }

    /**
     * Writes a short in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setShortBE(byte[] array, int offset, short value) {
        SHORT_BE.set(array, offset, value);
    }

    /**
     * Writes a short in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setShortLE(byte[] array, int offset, short value) {
        SHORT_LE.set(array, offset, value);
    }

    /**
     * Writes a short in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setUShortBE(byte[] array, int offset, int value) {
        SHORT_BE.set(array, offset, (short) (char) value);
    }

    /**
     * Writes a short in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setUShortLE(byte[] array, int offset, int value) {
        SHORT_LE.set(array, offset, (short) (char) value);
    }

    /**
     * Writes an int in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setIntBE(byte[] array, int offset, int value) {
        INT_BE.set(array, offset, value);
    }

    /**
     * Writes an int in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setIntLE(byte[] array, int offset, int value) {
        INT_LE.set(array, offset, value);
    }

    /**
     * Writes a long in big endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setLongBE(byte[] array, int offset, long value) {
        LONG_BE.set(array, offset, value);
    }

    /**
     * Writes a long in little endian order at the specified array offset.
     *
     * @param array  an array
     * @param offset the offset
     * @param value  the value
     */
    public static void setLongLE(byte[] array, int offset, long value) {
        LONG_LE.set(array, offset, value);
    }

}
