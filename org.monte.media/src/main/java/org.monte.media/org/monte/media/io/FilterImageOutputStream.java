/*
 * @(#)FilterImageOutputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.io;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * {@code FilterImageOutputStream}.
 *
 * @author Werner Randelshofer
 */
public class FilterImageOutputStream extends ImageOutputStreamImpl {

    private final ImageOutputStream out;
    private long maxStreamPos;
    private final long offset;

    /**
     * Whether flush and close request shall be forwarded to underlying stream.
     */
    private final boolean forwardFlushAndClose;

    /**
     * Creates a new instance that does not close the underlying stream when this instance is closed.
     * <p>
     * The stream position of this instance is relative to the stream position of the underlying stream
     * when the instance was created.
     *
     * @param out the underlying stream.
     * @throws IOException on IO failure
     */
    public FilterImageOutputStream(ImageOutputStream out) throws IOException {
        this(out, out.getStreamPosition(), out.getByteOrder(), false);
    }

    /**
     * Creates a new instance that optionally closes the underlying stream when this instance is closed.
     * <p>
     * The stream position of this instance is relative to the specified offset.
     *
     * @param out                  the underlying stream
     * @param offset               the offset into the underlying stream.
     * @param bo                   the byte order (will be set on the underlying stream)
     * @param forwardFlushAndClose whether to forward flush and close to the underlying stream
     * @throws IOException on IO failure
     */
    public FilterImageOutputStream(ImageOutputStream out, long offset, ByteOrder bo, boolean forwardFlushAndClose) throws IOException {
        this.out = out;
        this.offset = offset;
        this.maxStreamPos=offset;
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
        return maxStreamPos - offset;
    }

    @Override
    public final void write(int b) throws IOException {
        out.write(b);
        maxStreamPos=Math.max(maxStreamPos, out.getStreamPosition());
    }

    @Override
    public final void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        maxStreamPos=Math.max(maxStreamPos,out.getStreamPosition());
    }

    public void dispose() throws IOException {
        if (forwardFlushAndClose) {
            checkClosed();
        }
    }
}
