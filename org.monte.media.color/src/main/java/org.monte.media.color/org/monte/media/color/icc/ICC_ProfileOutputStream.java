/*
 * @(#)ICC_ProfileInputStream.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.monte.media.io.FilterImageOutputStream;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/// Reference:
/// International Color Consortium. ICC Profile Format Specification. Version 3.4. August 15, 1997.
/// [color.org](https://www.color.org/icc34.pdf)
public class ICC_ProfileOutputStream extends FilterImageOutputStream {
    private final byte[] byteBuf = new byte[8];

    public ICC_ProfileOutputStream(ImageOutputStream out) throws IOException {
        super(out);
    }

    public void writeFourCC(String str) throws IOException {
        byteBuf[0] = (byte) str.charAt(0);
        byteBuf[1] = (byte) str.charAt(1);
        byteBuf[2] = (byte) str.charAt(2);
        byteBuf[3] = (byte) str.charAt(3);
        out.write(byteBuf, 0, 4);
    }

    public static String toFourCC(int data) {
        var byteBuf = new byte[]{(byte) (data >>> 24),
                (byte) (data >>> 16),
                (byte) (data >>> 8),
                (byte) (data)};
        return new String(byteBuf, 0, 4, StandardCharsets.US_ASCII);

    }

    public void writeS15Fixed16Number(float value) throws IOException {
        writeInt((int) (value * 65535.0f));

    }

    public void writeXYZNumber(float[] values) throws IOException {
        writeS15Fixed16Number(values[0]);
        writeS15Fixed16Number(values[1]);
        writeS15Fixed16Number(values[2]);
    }

    public void writeU16Fixed16Number(float value) throws IOException {
        writeInt((int) (value * 65535.0f));
    }

    public void writeU8Fixed8Number(float value) throws IOException {
        writeShort((short) (value * 255f));
    }

    public void writeUInt16(int value) throws IOException {
        writeShort((short) value);
    }

    public void writeUInt32(long value) throws IOException {
        writeInt((int) value);
    }

    public void writeInt32(int value) throws IOException {
        writeInt(value);
    }

    public void writeUInt64(long value) throws IOException {
        writeLong(value);
    }

    public void writeInt64(long value) throws IOException {
        writeLong(value);
    }

    public void writeUInt8(int value) throws IOException {
        writeByte((byte) value);
    }

    public void writeDateTimeNumber(OffsetDateTime t) throws IOException {
        writeUInt16((char) t.getYear());
        writeUInt16((char) t.getMonthValue());
        writeUInt16((char) t.getDayOfMonth());
        writeUInt16((char) t.getHour());
        writeUInt16((char) t.getMinute());
        writeUInt16((char) t.getSecond());
    }
}
