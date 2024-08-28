/*
 * @(#)ByteArrayImageInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * A {@code ByteArrayImageInputStream} contains
 * an internal buffer that contains bytes that
 * may be read from the stream. An internal
 * counter keeps track of the next byte to
 * be supplied by the {@code read} method.
 * <p>
 * Closing a {@code ByteArrayImageInputStream} has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an {@code IOException}.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau
 */
public class ByteArrayImageInputStream extends ImageInputStreamImpl2 {
    /**
     * An array of bytes that was provided
     * by the creator of the stream. Elements <code>buf[0]</code>
     * through <code>buf[count-1]</code> are the
     * only bytes that can ever be read from the
     * stream;  element <code>buf[streamPos]</code> is
     * the next byte to be read.
     */
    protected byte[] buf;

    /**
     * The length of the buffer.
     */
    protected int length;



    /**
     * The offset to the start index of the buffer.
     */
    private final int offset;

    public ByteArrayImageInputStream(byte[] buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    public ByteArrayImageInputStream(byte[] buf, ByteOrder byteOrder) {
        this(buf, 0, buf.length, byteOrder);
    }

    public ByteArrayImageInputStream(byte[] buf, int offset, int length, ByteOrder byteOrder) {
        this.buf = buf;
        this.length = Math.min(length, buf.length - offset);
        this.offset = offset;
        this.byteOrder = byteOrder;
    }

    public ByteArrayImageInputStream(byte[] buf, int off, int length) {
        this(buf, off, length, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method
     * cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    @Override
    public int read() {
        flushBits();
        return (streamPos < length) ? (buf[offset + (int) (streamPos++)] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes
     * from this input stream.
     * If <code>streamPos</code> equals <code>count</code>,
     * then <code>-1</code> is returned to indicate
     * end of file. Otherwise, the  number <code>k</code>
     * of bytes read is equal to the smaller of
     * <code>len</code> and <code>count-streamPos</code>.
     * If <code>k</code> is positive, then bytes
     * <code>buf[streamPos]</code> through <code>buf[streamPos+k-1]</code>
     * are copied into <code>b[off]</code>  through
     * <code>b[off+k-1]</code> in the manner performed
     * by <code>System.arraycopy</code>. The
     * value <code>k</code> is added into <code>streamPos</code>
     * and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the stream has been reached.
     * @throws NullPointerException      If <code>b</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative,
     *                                   <code>len</code> is negative, or <code>len</code> is greater than
     *                                   <code>b.length - off</code>
     */
    @Override
    public int read(byte b[], int off, int len) {
        flushBits();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (streamPos >= length) {
            return -1;
        }
        if (streamPos + len > length) {
            len = (int) (length - streamPos);
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, offset + (int) streamPos, b, off, len);
        streamPos += len;
        return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code>
     * of bytes to be skipped is equal to the smaller
     * of <code>n</code> and  <code>count-streamPos</code>.
     * The value <code>k</code> is added into <code>streamPos</code>
     * and <code>k</code> is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    public long skip(long n) {
        if (streamPos + n > length) {
            n = length - streamPos;
        }
        if (n < 0) {
            return 0;
        }
        streamPos += n;
        return n;
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over)
     * from this input stream.
     * <p>
     * The value returned is <code>count&nbsp;- streamPos</code>,
     * which is the number of bytes remaining to be read from the input buffer.
     *
     * @return the number of remaining bytes that can be read (or skipped
     * over) from this input stream without blocking.
     */
    public int available() {
        return (int) (length - streamPos);
    }


    /**
     * Closing a {@code ByteArrayInputStream} has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an {@code IOException}.
     * <p>
     */
    @Override
    public void close() {
        // does nothing!!
    }



    private void flushBits() {
        bitOffset = 0;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public int readInt() throws IOException {
        if (streamPos > length - 4) {
            throw new EOFException();
        }
        int v = (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArray.getIntBE(buf, (int) streamPos)
                : ByteArray.getIntLE(buf, (int) streamPos);
        streamPos += 4;
        return v;
    }

    @Override
    public long readLong() throws IOException {
        if (streamPos > length - 8) {
            throw new EOFException();
        }
        long v = (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArray.getLongBE(buf, (int) streamPos)
                : ByteArray.getLongLE(buf, (int) streamPos);
        streamPos += 8;
        return v;
    }

    @Override
    public short readShort() throws IOException {
        if (streamPos > length - 2) {
            throw new EOFException();
        }
        short v = (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArray.getShortBE(buf, (int) streamPos)
                : ByteArray.getShortLE(buf, (int) streamPos);
        streamPos += 2;
        return v;
    }
}
