/*
 * @(#)DataChunkOutputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi;

import org.monte.media.util.ByteArrays;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * This output stream filter supports common data types used inside
 * of an AVI RIFF Data Chunk.
 *
 * @author Werner Randelshofer
 */
public class DataChunkOutputStream extends FilterOutputStream {

    /**
     * The number of bytes written to the data output stream so far.
     * If this counter overflows, it will be wrapped to Integer.MAX_VALUE.
     */
    protected long written;
    /**
     * Whether flush and close request shall be forwarded to underlying stream.
     */
    private boolean forwardFlushAndClose;
    private byte byteBuffer[] = new byte[8];
    public DataChunkOutputStream(OutputStream out) {
        this(out, true);
    }

    public DataChunkOutputStream(OutputStream out, boolean forwardFlushAndClose) {
        super(out);
        this.forwardFlushAndClose = forwardFlushAndClose;
    }

    /**
     * Writes an chunk type identifier (4 bytes).
     *
     * @param s A string with a length of 4 characters.
     */
    public void writeType(String s) throws IOException {
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            out.write(s.getBytes(StandardCharsets.US_ASCII), 0, 4);
            incCount(4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>1</code>.
     *
     * @param v a <code>byte</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    public final void writeByte(int v) throws IOException {
        out.write(v);
        incCount(1);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the underlying output stream.
     * If no exception is thrown, the counter <code>written</code> is
     * incremented by <code>len</code>.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    @Override
    public synchronized void write(byte b[], int off, int len)
            throws IOException {
        out.write(b, off, len);
        incCount(len);
    }

    /**
     * Writes the specified byte (the low eight bits of the argument
     * <code>b</code>) to the underlying output stream. If no exception
     * is thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param b the <code>byte</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    @Override
    public synchronized void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }

    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, low byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>4</code>.
     *
     * @param v an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    public void writeInt(int v) throws IOException {
        ByteArrays.setIntLE(byteBuffer, 0, v);
        out.write(byteBuffer, 0, 4);
        incCount(4);
    }

    /**
     * Writes an unsigned 32 bit integer value.
     *
     * @param v The value
     * @throws java.io.IOException
     */
    public void writeUInt(long v) throws IOException {
        ByteArrays.setIntLE(byteBuffer, 0, (int) v);
        out.write(byteBuffer, 0, 4);
        incCount(4);
    }

    /**
     * Writes a signed 16 bit integer value.
     *
     * @param v The value
     * @throws java.io.IOException
     */
    public void writeShort(int v) throws IOException {
        ByteArrays.setShortLE(byteBuffer, 0, (short) v);
        out.write(byteBuffer, 0, 2);
        incCount(2);
    }

    /**
     * Writes a signed 16 bit integer value.
     *
     * @param v The value
     * @throws java.io.IOException
     */
    public void writeShorts(short[] v, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            out.write((v[i] >>> 0) & 0xff);
            out.write((v[i] >> 8) & 0xff);
        }
        incCount(len * 2);
    }

    /**
     * Writes unsigned 24 bit integer values.
     *
     * @param v The value
     * @throws java.io.IOException
     */
    public void writeInts24(int[] v, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            out.write((v[i] >>> 0) & 0xff);
            out.write((v[i] >> 8) & 0xff);
            out.write((v[i] >> 16) & 0xff);
        }
        incCount(len * 3);
    }

    public void writeLong(long v) throws IOException {
        ByteArrays.setLongLE(byteBuffer, 0, v);
        out.write(byteBuffer, 0, 8);
        incCount(8);
    }

    public void writeUShort(int v) throws IOException {
        ByteArrays.setShortLE(byteBuffer, 0, (short) v);
        out.write(byteBuffer, 0, 2);
        incCount(2);
    }

    /**
     * Increases the written counter by the specified value
     * until it reaches Long.MAX_VALUE.
     */
    protected void incCount(int value) {
        long temp = written + value;
        if (temp < 0) {
            temp = Long.MAX_VALUE;
        }
        written = temp;
    }

    /**
     * Returns the current value of the counter <code>written</code>,
     * the number of bytes written to this data output stream so far.
     * If the counter overflows, it will be wrapped to Integer.MAX_VALUE.
     *
     * @return the value of the <code>written</code> field.
     */
    public final long size() {
        return written;
    }

    /**
     * Sets the value of the counter <code>written</code> to 0.
     */
    public void clearCount() {
        written = 0;
    }

    @Override
    public void close() throws IOException {
        if (forwardFlushAndClose) {
            super.close();
        }
    }

    @Override
    public void flush() throws IOException {
        if (forwardFlushAndClose) {
            super.flush();
        }
    }
}
