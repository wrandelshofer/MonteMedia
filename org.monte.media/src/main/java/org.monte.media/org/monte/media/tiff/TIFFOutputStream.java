/*
 * @(#)TIFFOutputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Stack;

/**
 * {@code TIFFOutputStream}.
 * <p>
 * References:
 * <p>
 * TIFF TM Revision 6.0. Final — June 3, 1992.
 * Adobe Systems Inc.
 * http://www.exif.org/specifications.html
 *
 * @author Werner Randelshofer
 */
public class TIFFOutputStream extends OutputStream {

    private final ImageOutputStream out;
    private final long              offset;
    private       Stack<IFD>        ifdStack = new Stack<IFD>();

    private enum State {

        INITIALIZED, STARTED, FINISHED
    }

    ;
    private State state = State.INITIALIZED;
    private long firstIFDOffset = 8;

    public TIFFOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        this.offset = out.getStreamPosition();
    }

    public void setByteOrder(ByteOrder bo) {
        if (state == State.INITIALIZED && bo != out.getByteOrder()) {
            throw new IllegalStateException("Can't change byte order within TIFF file");
        }
        out.setByteOrder(bo);
    }

    public ByteOrder getByteOrder() {
        return out.getByteOrder();
    }

    public long getStreamPosition() throws IOException {
        return out.getStreamPosition() - offset;
    }

    public void seek(long position) throws IOException {
        out.seek(position + offset);
    }

    @Override
    public void write(int b) throws IOException {
        ensureStarted();
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureStarted();
        out.write(b, off, len);
    }

    public void writeIFD(IFD ifd, long nextIFD) throws IOException {
        ensureStarted();
        writeSHORT(ifd.getCount());
        long ifdOffset = getStreamPosition();
        long valueOffset = getStreamPosition() + 12 * ifd.getCount() + 4;
        for (int i = 0, n = ifd.getCount(); i < n; i++) {
            IFDEntry entry = ifd.get(i);
            writeSHORT(entry.getTagNumber());
            writeSHORT(entry.getTypeNumber());
            if (entry.isDataInValueOffset()) {
                writeLONG(entry.getValueOffset());
            } else {
                writeLONG(valueOffset);
                valueOffset += entry.getLength();
            }
        }
        writeLONG(nextIFD);

        for (int i = 0, n = ifd.getCount(); i < n; i++) {
            IFDEntry entry = ifd.get(i);
            if (!entry.isDataInValueOffset()) {
                write((byte[]) entry.getData());
            }
        }
    }

    public long getFirstIFDOffset() {
        return firstIFDOffset;
    }

    public void setFirstIFDOffset(long newValue) {
        firstIFDOffset = newValue;
    }

    private void ensureStarted() throws IOException {
        if (state == State.INITIALIZED) {
            if (getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                writeSHORT(0x4949); // "II" little endian marker
            } else {
                writeSHORT(0x4D4D); // "MM" big endian marker
            }
            writeSHORT(42); // magic number

            state = State.STARTED;
        }
    }

    public void finish() throws IOException {
        ensureStarted();
        if (state == State.STARTED) {
            state = State.FINISHED;

            long pos = getStreamPosition();
            seek(4);
            writeLONG(firstIFDOffset);
            seek(pos);
        }
    }

    /**
     * Writes a 32-bit unsigned integer.
     */
    public void writeLONG(long v) throws IOException {
        out.writeInt((int) v);
    }

    /**
     * Writes a 12-bit unsigned integer.
     */
    public void writeSHORT(int v) throws IOException {
        out.writeShort((short) v);
    }

    @Override
    public void close() throws IOException {
        finish();
        out.close();
    }
}
