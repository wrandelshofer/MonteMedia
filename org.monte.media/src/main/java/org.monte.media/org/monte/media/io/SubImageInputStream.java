/*
 * @(#)SubImageInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * SubImageInputStream.
 *
 * @author Werner Randelshofer
 */
public class SubImageInputStream extends ImageInputStreamImpl2 {

    private final ImageInputStream in;
    private final long offset;
    private final long length;

    public SubImageInputStream(ImageInputStream in, long offset, long length) throws IOException {
        this.in = in;
        this.offset = offset;
        this.length = length;
        if (in.length() != -1 && offset + length > in.length()) {
            throw new IllegalArgumentException("Offset too large. offset=" + offset + " length=" + length + " in.length=" + in.length());
        }
        // setByteOrder(in.getByteOrder());
        in.seek(offset);
    }

    private long available() throws IOException {
        checkClosed();
        long pos = in.getStreamPosition();
        if (pos < offset) {
            in.seek(offset);
            pos = offset;
        }
        return offset + length - pos;
    }

    @Override
    public int read() throws IOException {
        if (available() <= 0) {
            return -1;
        } else {
            return in.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long av = available();
        if (av <= 0) {
            return -1;
        } else {
            return in.read(b, off, (int) Math.min(len, av));
        }
    }

    @Override
    public long getStreamPosition() throws IOException {
        return in.getStreamPosition() - offset;
    }

    @Override
    public void seek(long pos) throws IOException {
        in.seek(pos + offset);
    }

    @Override
    public void flush() throws IOException {
        in.flush();
    }

    @Override
    public long getFlushedPosition() {
        return in.getFlushedPosition() - offset;
    }

    /**
     * Default implementation returns false.  Subclasses should
     * override this if they cache data.
     */
    @Override
    public boolean isCached() {
        return in.isCached();
    }

    /**
     * Default implementation returns false.  Subclasses should
     * override this if they cache data in main memory.
     */
    @Override
    public boolean isCachedMemory() {
        return in.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return in.isCachedFile();
    }

    @Override
    public long length() {
        return length;
    }
}
