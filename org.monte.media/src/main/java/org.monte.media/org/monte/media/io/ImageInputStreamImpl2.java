/*
 * @(#)ImageInputStreamImpl2.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * {@code ImageInputStreamImpl2} fixes bugs in ImageInputStreamImpl.
 * <p>
 * ImageInputStreamImpl uses read(byte[]) instead of readFully(byte[]) inside of
 * readShort. This results in corrupt data input if the underlying stream can
 * not fulfill the read operation in a single step.
 *
 * @author Werner Randelshofer
 */
public abstract class ImageInputStreamImpl2 extends ImageInputStreamImpl {
    // Length of the buffer used for readFully(type[], int, int)
    private static final int BYTE_BUF_LENGTH = 8192;
    /**
     * Byte buffer used for readFully(type[], int, int).  Note that this
     * array is also used for bulk reads in readShort(), readInt(), etc, so
     * it should be large enough to hold a primitive value (i.e. &gt;= 8 bytes).
     * Also note that this array is package protected, so that it can be
     * used by ImageOutputStreamImpl in a similar manner.
     */
    protected byte[] byteBuf = new byte[BYTE_BUF_LENGTH];

    @Override
    public short readShort() throws IOException {
        readFully(byteBuf, 0, 2);
        return (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArray.getShortBE(byteBuf, 0)
                : ByteArray.getShortLE(byteBuf, 0);
    }

    public int readInt() throws IOException {
        readFully(byteBuf, 0, 4);
        return (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArray.getIntBE(byteBuf, 0)
                : ByteArray.getIntLE(byteBuf, 0);
    }
}
