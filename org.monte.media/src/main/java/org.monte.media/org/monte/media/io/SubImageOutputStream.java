/*
 * @(#)SubImageOutputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * {@code SubImageOutputStream}.
 *
 * @author Werner Randelshofer
 */
public class SubImageOutputStream extends ImageOutputStreamImpl {

    private ImageOutputStream out;
    private final long offset;
    private long length;

    /**
     * Whether flush and close request shall be forwarded to underlying stream.
     */
    private final boolean forwardFlushAndClose;

    public SubImageOutputStream(ImageOutputStream out, ByteOrder bo, boolean forwardFlushAndClose) throws IOException {
        this(out, out.getStreamPosition(), bo, forwardFlushAndClose);
    }

    public SubImageOutputStream(ImageOutputStream out, long offset, ByteOrder bo, boolean forwardFlushAndClose) throws IOException {
        this.out = out;
        this.offset = offset;
        this.forwardFlushAndClose = forwardFlushAndClose;
        setByteOrder(bo);
        out.seek(offset);
    }

    private long available() throws IOException {
        checkClosed();
        long pos = out.getStreamPosition();
        if (pos < offset) {
            out.seek(offset);
            pos = offset;
        }
        return offset + out.length() - pos;
    }

    @Override
    public int read() throws IOException {
        if (available() <= 0) {
            return -1;
        } else {
            return out.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long av = available();
        if (av <= 0) {
            return -1;
        } else {
            return out.read(b, off, (int) Math.min(len, av));
        }
    }

    @Override
    public long getStreamPosition() throws IOException {
        return out.getStreamPosition() - offset;
    }

    @Override
    public void seek(long pos) throws IOException {
        out.seek(pos + offset);
        length = Math.max(pos - offset + 1, length);
    }

    @Override
    public void flush() throws IOException {
        if (forwardFlushAndClose) {
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (forwardFlushAndClose) {
            super.close();
        }
    }

    @Override
    public long getFlushedPosition() {
        return out.getFlushedPosition() - offset;
    }

    /**
     * Default implementation returns false.  Subclasses should
     * override this if they cache data.
     */
    @Override
    public boolean isCached() {
        return out.isCached();
    }

    /**
     * Default implementation returns false.  Subclasses should
     * override this if they cache data in main memory.
     */
    @Override
    public boolean isCachedMemory() {
        return out.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return out.isCachedFile();
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        length = Math.max(out.getStreamPosition() - offset, length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        length = Math.max(out.getStreamPosition() - offset, length);
    }

    public void dispose() throws IOException {
        if (forwardFlushAndClose) {
            checkClosed();
        }
        out = null;
    }
}
