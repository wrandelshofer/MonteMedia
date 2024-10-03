/*
 * @(#)ByteArray.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

import java.util.Arrays;

/**
 * Wrapper for a byte array.
 */
public class ByteArray {
    private final byte[] array;

    public ByteArray(byte[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArray byteArray = (ByteArray) o;
        return Arrays.equals(array, byteArray.array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    /**
     * Gets the underlying array.
     */
    public byte[] getArray() {
        return array;
    }
}
