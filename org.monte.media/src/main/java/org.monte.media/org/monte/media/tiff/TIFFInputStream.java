/*
 * @(#)TIFFInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import org.monte.media.math.Rational;

import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * Reads a TIFF file.
 * <p>
 * References:
 * <p>
 * TIFF TM Revision 6.0. Final — June 3, 1992.
 * Adobe Systems Inc.
 * http://www.exif.org/specifications.html
 *
 * @author Werner Randelshofer
 */
public class TIFFInputStream extends InputStream {

    /**
     * A TIFF input stream can be little endian or big endian.
     */
    private ByteOrder byteOrder;
    /**
     * The offset of the first IFD.
     */
    private       long             firstIFDOffset;
    /**
     * The underlying input stream.
     */
    private final ImageInputStream in;

    public TIFFInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        readHeader();
    }

    /**
     * Creates a TIFFInputStream from a stream which does not have a header.
     */
    public TIFFInputStream(ImageInputStream in, ByteOrder byteOrder, long firstIFDOffset) {
        this.in = in;
        this.byteOrder = byteOrder;
        this.firstIFDOffset = firstIFDOffset;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder newValue) {
        byteOrder = newValue;
    }

    public long getFirstIFDOffset() {
        return firstIFDOffset;
    }

    /**
     * Reads the IFD at the specified offset.
     * <p>
     * An IFD consists of a 2-byte count of the number of directory entries
     * (i.e., the number of fields), followed by a sequence of 12-byte field entries,
     * followed by a 4-byte offset of the next IFD (or 0 if none).
     * <p>
     * Each 12-byte IFD entry has the following format:
     * Bytes 0-1 The Tag that identifies the field.
     * Bytes 2-3 The field Type.
     * Bytes 4-7 The number of values, Count of the indicated Type.
     * Bytes 8-11 The Value Offset, the file offset (in bytes) of the Value for the
     * field. The Value is expected to begin on a word boundary; the corresponding
     * Value Offset will thus be an even number. This file offset may point anywhere
     * in the file, even after the image data.
     * <p>
     * There must be at least 1 IFD in a TIFF file and each IFD must have at least
     * one entry.
     */
    public IFD readIFD(long offset) throws IOException {
        return readIFD(offset, true, false);
    }

    /**
     * Reads the IFD at the specified offset.
     * <p>
     * An IFD consists of a 2-byte count of the number of directory entries
     * (i.e., the number of fields), followed by a sequence of 12-byte field entries,
     * followed by a 4-byte offset of the next IFD (or 0 if none).
     * <p>
     * Each 12-byte IFD entry has the following format:
     * Bytes 0-1 The Tag that identifies the field.
     * Bytes 2-3 The field Type.
     * Bytes 4-7 The number of values, Count of the indicated Type.
     * Bytes 8-11 The Value Offset, the file offset (in bytes) of the Value for the
     * field. The Value is expected to begin on a word boundary; the corresponding
     * Value Offset will thus be an even number. This file offset may point anywhere
     * in the file, even after the image data.
     * <p>
     * There must be at least 1 IFD in a TIFF file and each IFD must have at least
     * one entry.
     */
    public IFD readIFD(long offset, boolean hasNextOffset, boolean isFirstIFD) throws IOException {
        if ((offset % 1) != 0) {
            throw new IOException("IFD does not start at word boundary");
        }
        if (offset == 0 && !isFirstIFD) {
            return null;
        }
        in.seek(offset);
        int numEntries = readSHORT();
        IFD ifd = new IFD(offset, hasNextOffset);
        for (int i = 0; i < numEntries; i++) {
            long entryOffset = in.getStreamPosition();
            int tag = readSHORT();
            int type = readSHORT();
            long count = readLONG();
            long valueOffset = readSLONG();
            if (count == 0) {
                throw new IOException("IFDEntry " + i + " of " + numEntries + " has count 0 in TIFF stream at offset 0x" + Long.toHexString(offset));
                //continue;
            }
            ifd.add(new IFDEntry(tag, type, count, valueOffset, entryOffset));
        }
        if (hasNextOffset) {
            ifd.setNextOffset(readSLONG());
            if ((ifd.getNextOffset() % 1) != 0) {
                throw new IOException("next IFD does not start at word boundary");
            }
        }

        return ifd;
    }

    /**
     * Reads an ASCII (8-bit byte that contains a 7-bit ASCII code; the last byte
     * must be NUL (binary zero).
     * value at the specified offset.
     */
    public String readASCII(long offset, long length) throws IOException {
        in.seek(offset);
        return readASCII(length);
    }

    private String readASCII(long length) throws IOException {
        byte[] buf = new byte[(int) length];
        readFully(buf);
        if (buf[(int) length - 1] != 0) {
            throw new IOException("String does not end with NUL byte.");
        }
        return new String(buf, 0, (int) length - 1, "ASCII");
    }

    private void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);

    }

    private void readFully( byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException("EOF after " + n + " bytes (needed " + len + " bytes)");
            }
            n += count;
        }
    }

    /**
     * Reads a LONG (32-bit (4-byte) unsigned integer).
     * value at the specified offset.
     */
    public long readLONG(long offset) throws IOException {
        in.seek(offset);
        return readLONG();
    }

    /**
     * Reads the specified number of LONGs (32-bit (4-byte) unsigned integer).
     * value at the specified offset.
     */
    public long[] readLONG(long offset, long count) throws IOException {
        in.seek(offset);
        long[] longs = new long[(int) count];
        for (int i = 0; i < count; i++) {
            longs[i] = readLONG();
        }
        return longs;
    }

    /**
     * Reads a SLONG (32-bit (4-byte) signed integer).
     * value at the specified offset.
     */
    public int readSLONG(long offset) throws IOException {
        in.seek(offset);
        return readSLONG();
    }

    /**
     * Reads the specified number of SLONGs (32-bit (4-byte) signed integer).
     * value at the specified offset.
     */
    public int[] readSLONG(long offset, long count) throws IOException {
        in.seek(offset);
        int[] longs = new int[(int) count];
        for (int i = 0; i < count; i++) {
            longs[i] = readSLONG();
        }
        return longs;
    }

    /**
     * Reads the specified number of SHORTs (16-bit (2-byte) unsigned integer).
     * value at the specified offset.
     */
    public int[] readSHORT(long offset, long count) throws IOException {
        in.seek(offset);
        int[] shorts = new int[(int) count];
        for (int i = 0; i < count; i++) {
            shorts[i] = readSHORT();
        }
        return shorts;
    }

    /**
     * Reads the specified number of SSHORTs (16-bit (2-byte) signed integer).
     * value at the specified offset.
     */
    public short[] readSSHORT(long offset, long count) throws IOException {
        in.seek(offset);
        short[] shorts = new short[(int) count];
        for (int i = 0; i < count; i++) {
            shorts[i] = readSSHORT();
        }
        return shorts;
    }

    /**
     * Reads a RATIONAL number at the specified offset.
     */
    public Rational readRATIONAL(long offset) throws IOException {
        in.seek(offset);
        long num = readLONG();
        long denom = readLONG();
        return new Rational(num, denom);
    }

    /**
     * Reads a RATIONAL number at the specified offset.
     */
    public Rational readSRATIONAL(long offset) throws IOException {
        in.seek(offset);
        int num = readSLONG();
        int denom = readSLONG();
        return new Rational(num, denom);
    }

    /**
     * Reads the specified number of RATIONALs at the specified offset.
     */
    public Rational[] readRATIONAL(long offset, long count) throws IOException {
        in.seek(offset);
        Rational[] r = new Rational[(int) count];
        for (int i = 0; i < count; i++) {
            r[i] = new Rational(readLONG(), readLONG());
        }
        return r;
    }

    /**
     * Reads the specified number of RATIONALs at the specified offset.
     */
    public Rational[] readSRATIONAL(long offset, long count) throws IOException {
        in.seek(offset);
        Rational[] r = new Rational[(int) count];
        for (int i = 0; i < count; i++) {
            r[i] = new Rational(readSLONG(), readSLONG());
        }
        return r;
    }

    /**
     * Reads a 16-bit signed integer.
     */
    private short readSSHORT() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b0 == -1 || b1 == -1) {
            throw new EOFException();
        }

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return (short) ((b1 << 8) | b0);
        } else {
            return (short) ((b0 << 8) | b1);
        }
    }

    /**
     * Reads a 16-bit unsigned integer.
     */
    private int readSHORT() throws IOException {
        return readSSHORT() & 0xffff;
    }

    /**
     * Reads a 32-bit signed integer.
     */
    private int readSLONG() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b0 == -1 || b1 == -1 || b1 == -1 || b2 == -1) {
            throw new EOFException();
        }

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
        } else {
            return ((b0 << 24) | (b1 << 16) | (b2 << 8) | b3);
        }
    }

    /**
     * Reads a 32-bit unsigned integer.
     */
    private long readLONG() throws IOException {
        return readSLONG() & 0xffffffffL;
    }

    /**
     * Reads the Image File header.
     * <p>
     * struct {
     * short byteOrder // 0x4949=little endian, 0x4d4d=big endian
     * short magic // 42 in little or big endian
     * long offset // offset in little or big endian to the first IFD
     * }
     */
    private void readHeader() throws IOException {
        in.seek(0);
        byteOrder = ByteOrder.BIG_ENDIAN;
        int byteOrder = readSHORT();
        switch (byteOrder) {
            case 0x4949:
                this.byteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            case 0x4d4d:
                this.byteOrder = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new IOException("Image File Header illegal byte order value 0x" + Integer.toHexString(byteOrder));
        }

        int magic = readSHORT();
        if (magic != 42) {
            throw new IOException("Image File Header illegal magic value 0x" + Integer.toHexString(magic));
        }

        firstIFDOffset = readSLONG();
        if ((firstIFDOffset & 1) == 1) {
            throw new IOException("Image File Header IFD must be on a word boundary 0x" + Long.toHexString(firstIFDOffset));
        }
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(long offset, byte b[], int off, int len) throws IOException {
        in.seek(offset);
        return in.read(b, off, len);
    }
}
