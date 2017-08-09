/* @(#)UncachedImageInputStream.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * An implementation of {@code ImageInputStream} that gets its input from a
 * regular {@code InputStream}. No caching is used and thus backward seeking is 
 * not supported.
 *
 * @author Werner Randelshofer
 * @version $Id: UncachedImageInputStream.java 364 2016-11-09 19:54:25Z werner $
 */
public class UncachedImageInputStream extends ImageInputStreamImpl2 {

    private InputStream in;

    public UncachedImageInputStream(InputStream in) {
        this(in, ByteOrder.BIG_ENDIAN);
    }
    public UncachedImageInputStream(InputStream in, ByteOrder bo) {
        this.in = in;
        this.byteOrder=bo;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b >= 0) {
            streamPos++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = in.read(b, off, len);
        if (count > 0) {
            streamPos += count;
        }
        return count;
    }

    @Override
    public void seek(long pos) throws IOException {
        checkClosed();

        // This test also covers pos < 0
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        if (pos < streamPos) {
            throw new IndexOutOfBoundsException("pos < streamPos!");
        }

        this.bitOffset = 0;

        while (streamPos < pos) {
            long skipped = in.skip(pos - streamPos);
            if (skipped < 0) {
                throw new EOFException("EOF reached while trying to seek to " + pos);
            }
            streamPos += skipped;
        }


    }
}
