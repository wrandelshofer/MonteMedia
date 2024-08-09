/*
 * @(#)QTFFImageInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.io.FilterImageInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.lang.Math.min;

/**
 * {@code QuickTimeImageInputStream}.
 *
 * @author Werner Randelshofer
 */
public class QTFFImageInputStream extends FilterImageInputStream {
    protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();

    public QTFFImageInputStream(ImageInputStream in) {
        super(in);
        setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads a 32-bit Mac timestamp (seconds since 1902).
     *
     * @return the date corresponding to the Mac timestamp
     * @throws IOException if an I/O error occurs
     */
    public Date readMacTimestamp() throws IOException {
        long timestamp = ((long) readInt()) & 0xffffffffL;
        return new Date(MAC_TIMESTAMP_EPOCH + timestamp * 1000);
    }

    /**
     * Reads 32-bit fixed-point number divided as 16.16.
     */
    public double readFixed16D16() throws IOException {
        int wholePart = readUnsignedShort();
        int fractionPart = readUnsignedShort();

        return (wholePart + fractionPart / 65536.0);
    }

    /**
     * Reads 32-bit fixed-point number divided as 2.30.
     */
    public double readFixed2D30() throws IOException {
        int fixed = readInt();
        int wholePart = fixed >>> 30;
        int fractionPart = fixed & 0x3fffffff;

        return (wholePart + fractionPart / (double) 0x3fffffff);
    }

    /**
     * Reads 16-bit fixed-point number divided as 8.8.
     */
    public double readFixed8D8() throws IOException {
        int fixed = readUnsignedShort();
        int wholePart = fixed >>> 8;
        int fractionPart = fixed & 0xff;

        return (wholePart + fractionPart / 256d);
    }

    public String readType() throws IOException {
        readFully(byteBuf, 0, 4);
        return new String(byteBuf, 0, 4, StandardCharsets.US_ASCII);
    }

    public String readPString() throws IOException {
        int size = readUnsignedByte();
        if (size == 0) {
            size = readUnsignedByte();
            skipBytes(2); // why do we skip two bytes here?
        }
        if (size < 0) {
            return "";
        }
        byte[] b = (size <= byteBuf.length) ? byteBuf : new byte[size];
        readFully(b, 0, size);

        return new String(b, 0, size, StandardCharsets.US_ASCII);
    }

    /**
     * Reads a Pascal String which is padded to a fixed size.
     */
    public String readPString(int fixedSize) throws IOException {
        int remaining = fixedSize;
        int size = readUnsignedByte();
        remaining--;
        if (size < 0 || size > remaining) {
            skipBytes(remaining);
            return "";
        }
        byte[] b = (size <= byteBuf.length) ? byteBuf : new byte[size];
        readFully(b, 0, size);
        if (remaining - size > 0) {
            skipBytes(remaining - size);
        }

        return new String(b, 0, size, StandardCharsets.US_ASCII);
    }

    public int readUnsignedBCD4() throws IOException {
        readFully(byteBuf, 0, 2);
        int value = min(9, (byteBuf[0] >>> 4) & 0x0f) * 1000//
                + min(9, byteBuf[0] & 0x0f) * 100//
                + min(9, (byteBuf[1] >>> 4) & 0x0f) * 10//
                + min(9, byteBuf[1] & 0x0f);
        return value;
    }

    public int readUnsignedBCD2() throws IOException {
        readFully(byteBuf, 0, 1);
        //
        return min(9, (byteBuf[2] >>> 4) & 0x0f) * 10//
                + min(9, (byteBuf[2]) & 0x0f);
    }
}
