/*
 * @(#)ImageInputStreamAdapter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.io;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;

/// ImageInputStreamAdapter.
///
/// @author Werner Randelshofer
public class ImageInputStreamAdapter extends InputStream {
    private final ImageInputStream iis;

    public ImageInputStreamAdapter(ImageInputStream iis) {
        this.iis = iis;
    }

    /// Reads the next byte of data from this input stream. The value
    /// byte is returned as an `int` in the range
    /// `0` to `255`. If no byte is available
    /// because the end of the stream has been reached, the value
    /// `-1` is returned. This method blocks until input data
    /// is available, the end of the stream is detected, or an exception
    /// is thrown.
    ///
    /// This method
    /// simply performs `in.read()` and returns the result.
    ///
    /// @return the next byte of data, or `-1` if the end of the
    /// stream is reached.
    /// @throws IOException if an I/O error occurs.
    @Override
    public int read() throws IOException {
        return iis.read();
    }

    /// Reads up to `len` bytes of data from this input stream
    /// into an array of bytes. If `len` is not zero, the method
    /// blocks until some input is available; otherwise, no
    /// bytes are read and `0` is returned.
    ///
    /// This method simply performs `in.read(b, off, len)`
    /// and returns the result.
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
    /// @throws IOException               if an I/O error occurs.
    /// @see java.io.FilterInputStream
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return iis.read(b, off, len);
    }

    /// {@inheritDoc}
    ///
    /// This method simply performs `in.skip(n)`.
    @Override
    public long skip(long n) throws IOException {
        return iis.skipBytes(n);
    }

    /// Returns an estimate of the number of bytes that can be read (or
    /// skipped over) from this input stream without blocking by the next
    /// caller of a method for this input stream. The next caller might be
    /// the same thread or another thread.  A single read or skip of this
    /// many bytes will not block, but may read or skip fewer bytes.
    ///
    /// @return an estimate of the number of bytes that can be read (or skipped
    /// over) from this input stream without blocking.
    /// @throws IOException if an I/O error occurs.
    @Override
    public int available() throws IOException {
        return (iis.isCached()) ? //
                (int) Math.min(Integer.MAX_VALUE, iis.length() - iis.getStreamPosition()) :
                0;
    }

    /// Closes this input stream and releases any system resources
    /// associated with the stream.
    /// This
    /// method simply performs `in.close()`.
    ///
    /// @throws IOException if an I/O error occurs.
    /// @see java.io.FilterInputStream
    @Override
    public void close() throws IOException {
        iis.close();
    }

    /// Marks the current position in this input stream. A subsequent
    /// call to the `reset` method repositions this stream at
    /// the last marked position so that subsequent reads re-read the same bytes.
    ///
    /// The `readlimit` argument tells this input stream to
    /// allow that many bytes to be read before the mark position gets
    /// invalidated.
    ///
    /// This method simply performs `in.mark(readlimit)`.
    ///
    /// @param readlimit the maximum limit of bytes that can be read before
    ///                                                    the mark position becomes invalid.
    /// @see java.io.FilterInputStream#reset()
    @Override
    public synchronized void mark(int readlimit) {
        iis.mark();
    }

    /// Repositions this stream to the position at the time the
    /// `mark` method was last called on this input stream.
    ///
    /// This method
    /// simply performs `in.reset()`.
    ///
    /// Stream marks are intended to be used in
    /// situations where you need to read ahead a little to see what's in
    /// the stream. Often this is most easily done by invoking some
    /// general parser. If the stream is of the type handled by the
    /// parse, it just chugs along happily. If the stream is not of
    /// that type, the parser should toss an exception when it fails.
    /// If this happens within readlimit bytes, it allows the outer
    /// code to reset the stream and try another parser.
    ///
    /// @throws IOException if the stream has not been marked or if the
    ///                                                             mark has been invalidated.
    /// @see java.io.FilterInputStream#mark(int)
    @Override
    public synchronized void reset() throws IOException {
        iis.reset();
    }

    /// Tests if this input stream supports the `mark`
    /// and `reset` methods.
    /// This method
    /// simply performs `in.markSupported()`.
    ///
    /// @return `true` if this stream type supports the
    /// `mark` and `reset` method;
    /// `false` otherwise.
    /// @see java.io.InputStream#mark(int)
    /// @see java.io.InputStream#reset()
    @Override
    public boolean markSupported() {
        return true;
    }

}
