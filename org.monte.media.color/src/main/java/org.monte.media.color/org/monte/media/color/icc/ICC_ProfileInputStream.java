/*
 * @(#)ICC_ProfileInputStream.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.monte.media.io.FilterImageInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/// Reference:
/// International Color Consortium. ICC Profile Format Specification. Version 3.4. August 15, 1997.
/// [color.org](https://www.color.org/icc34.pdf)
public class ICC_ProfileInputStream extends FilterImageInputStream {
    public ICC_ProfileInputStream(ImageInputStream in) throws IOException {
        super(in);
    }

    public ICC_ProfileInputStream(ImageInputStream in, long offset, long length) throws IOException {
        super(in, offset, length);
    }

    public String readFourCC() throws IOException {
        readFully(byteBuf, 0, 4);
        return new String(byteBuf, 0, 4, StandardCharsets.US_ASCII);
    }

    public static String toFourCC(int data) {
        var byteBuf = new byte[]{(byte) (data >>> 24),
                (byte) (data >>> 16),
                (byte) (data >>> 8),
                (byte) (data)};
        return new String(byteBuf, 0, 4, StandardCharsets.US_ASCII);

    }

    public float readS15Fixed16Number() throws IOException {
        return readInt() / 65535.0f;

    }

    public float[] readXYZNumber() throws IOException {
        return new float[]{
                readS15Fixed16Number(),
                readS15Fixed16Number(),
                readS15Fixed16Number()
        };
    }

    public float readU16Fixed16Number() throws IOException {
        return readUnsignedInt() / 65535.0f;
    }

    public float readU8Fixed8Number() throws IOException {
        return readUnsignedShort() / 255.0f;
    }

    public int readUInt16() throws IOException {
        return readUnsignedShort();
    }

    public long readUInt32() throws IOException {
        return readUnsignedInt();
    }

    public int readInt32() throws IOException {
        return readInt();
    }

    public long readUInt64() throws IOException {
        return readLong();
    }

    public long readInt64() throws IOException {
        return readLong();
    }

    public int readUInt8() throws IOException {
        return readUnsignedByte();
    }

    public OffsetDateTime readDateTimeNumber() throws IOException {
        int year = readUnsignedShort();
        int month = readUnsignedShort();
        int day = readUnsignedShort();
        int hours = readUnsignedShort();
        int minutes = readUnsignedShort();
        int seconds = readUnsignedShort();
        return OffsetDateTime.of(year, month, day, hours, minutes, seconds, 0, ZoneOffset.UTC);
    }


    private int indexOf(byte[] byteBuf, int c) {
        for (int i = 0; i < byteBuf.length; i++) {
            if (byteBuf[i] == c) return i;
        }
        return -1;
    }
}
