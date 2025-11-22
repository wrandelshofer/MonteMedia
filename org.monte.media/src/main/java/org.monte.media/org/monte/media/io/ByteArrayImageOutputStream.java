/*
 * @(#)ByteArrayImageOutputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;


import org.monte.media.util.ByteArrays;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.lang.Math.max;

/// This class implements an image output stream in which the data is
/// written into a byte array. The buffer automatically grows as data
/// is written to it.
/// The data can be retrieved using `toByteArray()`, `toImageOutputStream()`
/// and `toOutputStream()`.
///
/// Closing a `ByteArrayImageOutputStream` has no effect. The methods in
/// this class can be called after the stream has been closed without
/// generating an `IOException`.
///
/// @author Werner Randelshofer
public class ByteArrayImageOutputStream extends ImageOutputStreamImpl {


    /// An array of bytes that was provided
    /// by the creator of the stream. Elements `buf[0]`
    /// through `buf[count-1]` are the
    /// only bytes that can ever be read from the
    /// stream;  element `buf[streamPos]` is
    /// the next byte to be read.
    protected byte[] buf;
    /// The index one greater than the last valid byte in the input
    /// stream buffer.
    /// This value should always be nonnegative
    /// and not larger than the length of `buf`.
    /// It  is one greater than the position of
    /// the last byte within `buf` that
    /// can ever be read  from the input stream buffer.
    protected int count;
    /// The offset to the start of the array.
    private final int arrayOffset;

    public ByteArrayImageOutputStream() {
        this(16);
    }

    public ByteArrayImageOutputStream(int initialCapacity) {
        this(new byte[initialCapacity], 0, 0, ByteOrder.BIG_ENDIAN);
    }

    public ByteArrayImageOutputStream(byte[] buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    public ByteArrayImageOutputStream(byte[] buf, ByteOrder byteOrder) {
        this(buf, 0, buf.length, byteOrder);
    }

    public ByteArrayImageOutputStream(byte[] buf, int offset, int length, ByteOrder byteOrder) {
        this.buf = buf;
        this.streamPos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.arrayOffset = offset;
        this.byteOrder = byteOrder;
    }

    public ByteArrayImageOutputStream(ByteOrder byteOrder) {
        this(new byte[16], byteOrder);
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
    public int read() throws IOException {
        flushBits();
        return (streamPos < count) ? (buf[(int) (streamPos++)] & 0xff) : -1;
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
    public int read(byte b[], int off, int len) throws IOException {
        flushBits();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (streamPos >= count) {
            return -1;
        }
        if (streamPos + len > count) {
            len = (int) (count - streamPos);
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, (int) streamPos, b, off, len);
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
        if (streamPos + n > count) {
            n = count - streamPos;
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
        return (int) (count - streamPos);
    }

    /// Closing a `ByteArrayInputStream` has no effect. The methods in
    /// this class can be called after the stream has been closed without
    /// generating an `IOException`.
    ///
    @Override
    public void close() {
        // does nothing!!
    }

    @Override
    public long getStreamPosition() throws IOException {
        checkClosed();
        return streamPos - arrayOffset;
    }

    @Override
    public void seek(long pos) throws IOException {
        checkClosed();
        flushBits();

        // This test also covers pos < 0
        if (pos < 0) {
            throw new IndexOutOfBoundsException("pos < 0!");
        }

        this.streamPos = pos + arrayOffset;
    }

    /// Writes the specified byte to this output stream.
    ///
    /// @param b the byte to be written.
    @Override
    public void write(int b) throws IOException {
        flushBits();
        long newcount = max(streamPos + 1, count);
        if (newcount > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException(newcount + " > max array size");
        }
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, max(buf.length << 1, (int) newcount));
        }
        buf[(int) streamPos++] = (byte) b;
        count = (int) newcount;
    }

    /// Writes the specified byte array to this output stream.
    ///
    /// @param b the data.
    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /// Writes `len` bytes from the specified byte array
    /// starting at offset `off` to this output stream.
    ///
    /// @param b   the data.
    /// @param off the start offset in the data.
    /// @param len the number of bytes to write.
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        flushBits();
        if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException("off=" + off + ", len=" + len + ", b.length=" + b.length);
        } else if (len == 0) {
            return;
        }
        int newcount = max((int) streamPos + len, count);
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(b, off, buf, (int) streamPos, len);
        streamPos += len;
        count = newcount;
    }

    /// Writes the contents of the byte array into the specified output
    /// stream.
    ///
    /// @param out
    public void toOutputStream(OutputStream out) throws IOException {
        out.write(buf, arrayOffset, count);
    }

    /// Writes the contents of the byte array into the specified image output
    /// stream.
    ///
    /// @param out
    public void toImageOutputStream(ImageOutputStream out) throws IOException {
        out.write(buf, arrayOffset, count);
    }

    /// Creates a newly allocated byte array. Its size is the current
    /// size of this output stream and the valid contents of the buffer
    /// have been copied into it.
    ///
    /// @return the current contents of this output stream, as a byte array.
    /// @see java.io.ByteArrayOutputStream#size()
    public byte[] toByteArray() {
        byte[] copy = new byte[count - arrayOffset];
        System.arraycopy(buf, arrayOffset, copy, 0, count);
        return copy;
    }

    /// Returns the internally used byte buffer.
    public byte[] getBuffer() {
        return buf;
    }

    @Override
    public long length() {
        return count - arrayOffset;
    }

    public int size() {
        return (int) length();
    }

    /// Resets the `count` field of this byte array output
    /// stream to zero, so that all currently accumulated output in the
    /// output stream is discarded. The output stream can be used again,
    /// reusing the already allocated buffer space.
    public void clear() {
        count = arrayOffset;
        streamPos = arrayOffset;
        flushedPos = 0;
        bitOffset = 0;
    }

    @Override
    public void writeShort(int v) throws IOException {
        flushBits();
        growBy(2);
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArrays.setUShortBE(buf, (int) streamPos, v);
        } else {
            ByteArrays.setUShortLE(buf, (int) streamPos, v);
        }
        streamPos += 2;
    }

    /// Grows capacity if necessary, so that it can hold the given number of additional bytes.
    ///
    /// @param len the number of additional bytes
    private void growBy(int len) {
        int newcount = max((int) streamPos + len, count);
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        count = newcount;
    }

    /// Grows capacity if necessary, so that it can hold the given number of additional bytes.
    ///
    /// @param len the number of additional bytes
    public void growSizeBy(int len) {
        int newcount = max((int) streamPos + len, count);
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        count = newcount;
        streamPos += len;
    }


    @Override
    public void writeInt(int v) throws IOException {
        flushBits();
        growBy(4);
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArrays.setIntBE(buf, (int) streamPos, v);
        } else {
            ByteArrays.setIntLE(buf, (int) streamPos, v);
        }
        streamPos += 4;
    }

    @Override
    public void writeLong(long v) throws IOException {
        flushBits();
        growBy(8);
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArrays.setLongBE(buf, (int) streamPos, v);
        } else {
            ByteArrays.setLongLE(buf, (int) streamPos, v);
        }
        streamPos += 8;
    }

}
