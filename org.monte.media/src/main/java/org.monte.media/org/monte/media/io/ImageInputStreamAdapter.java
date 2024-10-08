/*
 * @(#)ImageInputStreamAdapter.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.io;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ImageInputStreamAdapter.
 *
 * @author Werner Randelshofer
 */
public class ImageInputStreamAdapter extends InputStream {
    private final ImageInputStream iis;

    public ImageInputStreamAdapter(ImageInputStream iis) {
        this.iis = iis;
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return iis.read();
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
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
     * @throws IOException               if an I/O error occurs.
     * @see java.io.FilterInputStream
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return iis.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method simply performs <code>in.skip(n)</code>.
     */
    @Override
    public long skip(long n) throws IOException {
        return iis.skipBytes(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * caller of a method for this input stream. The next caller might be
     * the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * @return an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        return (iis.isCached()) ? //
                (int) Math.min(Integer.MAX_VALUE, iis.length() - iis.getStreamPosition()) :
                0;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterInputStream
     */
    @Override
    public void close() throws IOException {
        iis.close();
    }

    /**
     * Marks the current position in this input stream. A subsequent
     * call to the <code>reset</code> method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     * <p>
     * The <code>readlimit</code> argument tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     * <p>
     * This method simply performs <code>in.mark(readlimit)</code>.
     *
     * @param readlimit the maximum limit of bytes that can be read before
     *                  the mark position becomes invalid.
     * @see java.io.FilterInputStream#reset()
     */
    @Override
    public synchronized void mark(int readlimit) {
        iis.mark();
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     * <p>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit bytes, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @throws IOException if the stream has not been marked or if the
     *                     mark has been invalidated.
     * @see java.io.FilterInputStream#mark(int)
     */
    @Override
    public synchronized void reset() throws IOException {
        iis.reset();
    }

    /**
     * Tests if this input stream supports the <code>mark</code>
     * and <code>reset</code> methods.
     * This method
     * simply performs <code>in.markSupported()</code>.
     *
     * @return <code>true</code> if this stream type supports the
     * <code>mark</code> and <code>reset</code> method;
     * <code>false</code> otherwise.
     * @see java.io.InputStream#mark(int)
     * @see java.io.InputStream#reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

}
