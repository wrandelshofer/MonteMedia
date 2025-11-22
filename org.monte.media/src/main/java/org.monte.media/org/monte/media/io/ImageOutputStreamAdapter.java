/*
 * @(#)ImageOutputStreamAdapter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/// Adapts an `ImageOutputStream` for classes requiring an
/// `OutputStream`.
///
/// @author Werner Randelshofer
public class ImageOutputStreamAdapter extends OutputStream {

    /// The underlying output stream to be filtered.
    protected ImageOutputStream out;

    /// Creates an output stream filter built on top of the specified
    /// underlying output stream.
    ///
    /// @param out the underlying output stream to be assigned to
    ///                                             the field `this.out` for later use, or
    ///                                             `null` if this instance is to be
    ///                                             created without an underlying stream.
    public ImageOutputStreamAdapter(ImageOutputStream out) {
        this.out = out;
    }

    /// Writes the specified `byte` to this output stream.
    ///
    /// The `write` method of `FilterOutputStream`
    /// calls the `write` method of its underlying output stream,
    /// that is, it performs `out.write(b)`.
    ///
    /// Implements the abstract `write` method of `OutputStream`.
    ///
    /// @param b the `byte`.
    /// @throws IOException if an I/O error occurs.
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /// Writes `b.length` bytes to this output stream.
    ///
    /// The `write` method of `FilterOutputStream`
    /// calls its `write` method of three arguments with the
    /// arguments `b`, `0`, and
    /// `b.length`.
    ///
    /// Note that this method does not call the one-argument
    /// `write` method of its underlying stream with the single
    /// argument `b`.
    ///
    /// @param b the data to be written.
    /// @throws IOException if an I/O error occurs.
    /// @see java.io.FilterOutputStream#write(byte[], int, int)
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /// Writes `len` bytes from the specified
    /// `byte` array starting at offset `off` to
    /// this output stream.
    ///
    /// The `write` method of `FilterOutputStream`
    /// calls the `write` method of one argument on each
    /// `byte` to output.
    ///
    /// Note that this method does not call the `write` method
    /// of its underlying input stream with the same arguments. Subclasses
    /// of `FilterOutputStream` should provide a more efficient
    /// implementation of this method.
    ///
    /// @param b   the data.
    /// @param off the start offset in the data.
    /// @param len the number of bytes to write.
    /// @throws IOException if an I/O error occurs.
    /// @see java.io.FilterOutputStream#write(int)
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /// Flushes this output stream and forces any buffered output bytes
    /// to be written out to the stream.
    ///
    /// The `flush` method of `FilterOutputStream`
    /// calls the `flush` method of its underlying output stream.
    ///
    /// @throws IOException if an I/O error occurs.
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /// Closes this output stream and releases any system resources
    /// associated with the stream.
    ///
    /// The `close` method of `FilterOutputStream`
    /// calls its `flush` method, and then calls the
    /// `close` method of its underlying output stream.
    ///
    /// @throws IOException if an I/O error occurs.
    /// @see java.io.FilterOutputStream#flush()
    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            out.close();
        }
    }
}
