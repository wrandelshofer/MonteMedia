/*
 * @(#)DataAtomInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.io.ByteArray;
import org.monte.media.io.ImageInputStreamAdapter;

import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * {@code DataAtomInputStream}.
 *
 * @author Werner Randelshofer
 */
public class DataAtomInputStream extends FilterInputStream {

    protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();
    private final byte[] byteBuffer = new byte[8];

    public DataAtomInputStream(InputStream in) {
        super(in);
    }

    public DataAtomInputStream(ImageInputStream in) {
        super(new ImageInputStreamAdapter(in));
    }

    public final byte readByte() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte) (ch);
    }

    public final short readShort() throws IOException {
        readFully(byteBuffer, 0, 2);
        return ByteArray.getShortBE(byteBuffer, 0);
    }

    public final int readInt() throws IOException {
        readFully(byteBuffer, 0, 4);
        return ByteArray.getIntBE(byteBuffer, 0);
    }

    public final long readLong() throws IOException {
        readFully(byteBuffer, 0, 8);
        return ByteArray.getLongBE(byteBuffer, 0);
    }

    public final int readUByte() throws IOException {
        return readByte() & 0xFF;
    }

    public final int readUShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    public final long readUInt() throws IOException {
        return readInt() & 0xFFFFFFFFL;
    }

    public final long skipBytes(long n) throws IOException {
        long total = 0;
        long cur = 0;

        while ((total < n) && ((cur = (int) skip(n - total)) > 0)) {
            total += cur;
        }

        return total;
    }

    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    /**
     * Reads a 32-bit Mac timestamp (seconds since 1902).
     *
     * @return date
     * @throws java.io.IOException if an I/O error occurs
     */
    public Date readMacTimestamp() throws IOException {
        long timestamp = ((long) readInt()) & 0xffffffffL;
        return new Date(MAC_TIMESTAMP_EPOCH + timestamp * 1000);
    }

    /**
     * Reads 32-bit fixed-point number divided as 16.16.
     */
    public double readFixed16D16() throws IOException {
        int wholePart = readUShort();
        int fractionPart = readUShort();

        return (wholePart + fractionPart) / 65536.0;
    }

    /**
     * Reads 32-bit fixed-point number divided as 2.30.
     */
    public double readFixed2D30() throws IOException {
        int fixed = readInt();
        int wholePart = fixed >>> 30;
        int fractionPart = fixed & 0x3fffffff;

        return (wholePart + fractionPart) / (double) 0x3fffffff;
    }

    /**
     * Reads 16-bit fixed-point number divided as 8.8.
     */
    public double readFixed8D8() throws IOException {
        int fixed = readUShort();
        int wholePart = fixed >>> 8;
        int fractionPart = fixed & 0xff;

        return (wholePart + fractionPart) / 256.0;
    }

    public String readType() throws IOException {
        readFully(byteBuffer, 0, 4);
        return new String(byteBuffer, 0, 4, StandardCharsets.US_ASCII);
    }

    public String readPString() throws IOException {
        int size = read();
        if (size == 0) {
            size = read();
            skipBytes(2); // why do we skip two bytes here?
        }
        if (size < 0) {
            return "";
        }
        byte[] b = size <= byteBuffer.length ? byteBuffer : new byte[size];
        readFully(b, 0, size);

        return new String(b, 0, size, StandardCharsets.US_ASCII);
    }

    /**
     * Reads a Pascal String which is padded to a fixed size.
     */
    public String readPString(int fixedSize) throws IOException {
        int size = read();
        fixedSize--;
        if (size == 0) {
            size = read();
            skipBytes(2); // why do we skip two bytes here?
            fixedSize -= 3;
        }
        if (size < 0) {
            skipBytes(fixedSize);
            return "";
        }
        byte[] b = fixedSize <= byteBuffer.length ? byteBuffer : new byte[fixedSize];
        readFully(b, 0, fixedSize);

        return new String(b, 0, fixedSize, StandardCharsets.US_ASCII);
    }
}
