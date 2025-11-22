/*
 * @(#)ByteArrayImageInputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import org.monte.media.util.ByteArrays;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;

/// A `ByteArrayImageInputStream` contains
/// an internal buffer that contains bytes that
/// may be read from the stream. An internal
/// counter keeps track of the next byte to
/// be supplied by the `read` method.
///
/// Closing a `ByteArrayImageInputStream` has no effect. The methods in
/// this class can be called after the stream has been closed without
/// generating an `IOException`.
///
/// @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau
public class ByteArrayImageInputStream extends ImageInputStreamImpl2 {
    /// An array of bytes that was provided
    /// by the creator of the stream. Elements `buf[0]`
    /// through `buf[count-1]` are the
    /// only bytes that can ever be read from the
    /// stream;  element `buf[streamPos]` is
    /// the next byte to be read.
    protected byte[] buf;

    /// The length of the buffer.
    protected int length;


    /// The offset to the start index of the buffer.
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

    /// Reads the next byte of data from this input stream. The value
    /// byte is returned as an `int` in the range
    /// `0` to `255`. If no byte is available
    /// because the end of the stream has been reached, the value
    /// `-1` is returned.
    ///
    /// This `read` method
    /// cannot block.
    ///
    /// @return the next byte of data, or `-1` if the end of the
    /// stream has been reached.
    @Override
    public int read() {
        flushBits();
        return (streamPos < length) ? (buf[offset + (int) (streamPos++)] & 0xff) : -1;
    }

    /// Reads up to `len` bytes of data into an array of bytes
    /// from this input stream.
    /// If `streamPos` equals `count`,
    /// then `-1` is returned to indicate
    /// end of file. Otherwise, the  number `k`
    /// of bytes read is equal to the smaller of
    /// `len` and `count-streamPos`.
    /// If `k` is positive, then bytes
    /// `buf[streamPos]` through `buf[streamPos+k-1]`
    /// are copied into `b[off]`  through
    /// `b[off+k-1]` in the manner performed
    /// by `System.arraycopy`. The
    /// value `k` is added into `streamPos`
    /// and `k` is returned.
    ///
    /// This `read` method cannot block.
    ///
    /// @param b   the buffer into which the data is read.
    /// @param off the start offset in the destination array `b`
    /// @param len the maximum number of bytes read.
    /// @return the total number of bytes read into the buffer, or
    /// `-1` if there is no more data because the end of
    /// the stream has been reached.
    /// @throws NullPointerException      If `b` is `null`.
    /// @throws IndexOutOfBoundsException If `off` is negative,
    ///                                                                                                                                         `len` is negative, or `len` is greater than
    ///                                                                                                                                         `b.length - off`
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

    /// Skips `n` bytes of input from this input stream. Fewer
    /// bytes might be skipped if the end of the input stream is reached.
    /// The actual number `k`
    /// of bytes to be skipped is equal to the smaller
    /// of `n` and  `count-streamPos`.
    /// The value `k` is added into `streamPos`
    /// and `k` is returned.
    ///
    /// @param n the number of bytes to be skipped.
    /// @return the actual number of bytes skipped.
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

    /// Returns the number of remaining bytes that can be read (or skipped over)
    /// from this input stream.
    ///
    /// The value returned is `count&nbsp;- streamPos`,
    /// which is the number of bytes remaining to be read from the input buffer.
    ///
    /// @return the number of remaining bytes that can be read (or skipped
    /// over) from this input stream without blocking.
    public int available() {
        return (int) (length - streamPos);
    }


    /// Closing a `ByteArrayInputStream` has no effect. The methods in
    /// this class can be called after the stream has been closed without
    /// generating an `IOException`.
    ///
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
                ? ByteArrays.getIntBE(buf, (int) streamPos)
                : ByteArrays.getIntLE(buf, (int) streamPos);
        streamPos += 4;
        return v;
    }

    @Override
    public long readLong() throws IOException {
        if (streamPos > length - 8) {
            throw new EOFException();
        }
        long v = (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArrays.getLongBE(buf, (int) streamPos)
                : ByteArrays.getLongLE(buf, (int) streamPos);
        streamPos += 8;
        return v;
    }

    @Override
    public short readShort() throws IOException {
        if (streamPos > length - 2) {
            throw new EOFException();
        }
        short v = (byteOrder == ByteOrder.BIG_ENDIAN)
                ? ByteArrays.getShortBE(buf, (int) streamPos)
                : ByteArrays.getShortLE(buf, (int) streamPos);
        streamPos += 2;
        return v;
    }
}
