/* @(#)AbstractVideoCodecCore.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@code AbstractVideoCodecCore}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AbstractVideoCodecCore {

    private byte[] byteBuf = new byte[4];

    protected static void writeInt24LE(ByteBuffer out, int v) throws IOException {
        out.put((byte) (v));
        out.put((byte) (v >>> 8));
        out.put((byte) (v >>> 16));
    }

    protected static void writeInts24LE(ByteBuffer out, int[] i, int off, int len) throws IOException {
        for (int j = off, n = off + len; j < n; j++) {
            int v = i[j];
            out.put((byte) (v));
            out.put((byte) (v >>> 8));
            out.put((byte) (v >>> 16));
        }
    }

    protected void readInts24LE(ImageInputStream in, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!, off=" + off + ", len=" + len);
        }

        byte[] b = byteBuf;
        for (int j = off, end = off + len; j < end; j++) {
            in.readFully(b, 0, 3);

            int v = (b[0] & 0xff) | ((b[1] & 0xff) << 8) | ((b[2] & 0xff) << 16);
            i[j] = v;
        }
    }

    protected int readInt24LE(ImageInputStream in) throws IOException {
        in.readFully(byteBuf, 0, 3);
        return ((byteBuf[2] & 0xff) << 16) | ((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0);
    }

    protected static void writeInts16LE(ByteBuffer out, short[] i, int off, int len) throws IOException {
        for (int j = off, n = off + len; j < n; j++) {
            int v = i[j];
            out.put((byte) (v));
            out.put((byte) (v >>> 8));
        }
    }

    protected static void readInts24LE(ByteBuffer in, int[] i, int off, int len) throws IOException {
        for (int j = off, end = off + len; j < end; j++) {
            byte b0 = in.get();
            byte b1 = in.get();
            byte b2 = in.get();

            int v = (b0 & 0xff) | ((b1 & 0xff) << 8) | ((b2 & 0xff) << 16);
            i[j] = v;
        }
    }

    protected static int readInt24LE(ByteBuffer in) throws IOException {
        byte b0 = in.get();
        byte b1 = in.get();
        byte b2 = in.get();
        return ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff) << 0);
    }

    /**
     * Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by byte
     * buffer.
     */
    protected static void readRGBs565to24(ByteBuffer in, int[] i, int off, int len) throws IOException {
        for (int j = off, end = off + len; j < end; j++) {
            int v = in.getShort();
            i[j] = ((v & 0xf800) << 8) | ((v & 0x3800) << 5)
                    | ((v & 0x07e0) << 5) | ((v & 0x0060) << 3)
                    | ((v & 0x001f) << 3) | ((v & 0x0007));
        }
    }

    /**
     * Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by byte
     * buffer.
     */
    protected int readRGB565to24(ByteBuffer in) throws IOException {
        int v = in.getShort();
        return ((v & 0xf800) << 8) | ((v & 0x3800) << 5)
                | ((v & 0x07e0) << 5) | ((v & 0x0060) << 3)
                | ((v & 0x001f) << 3) | ((v & 0x0007));
    }

    /**
     * Reads 16-bit RGB and converts it to 24-bit RGB BE. Endian of input is
     * defined by byte buffer.
     */
    protected static void readRGBs555to24(ImageInputStream in, int[] i, int off, int len) throws IOException {
        for (int j = off, end = off + len; j < end; j++) {
            int v = in.readUnsignedShort();
            i[j] = ((v & (0x1f << 10)) << 9) | ((v & (0x1c << 10)) << 4) // red
                    | ((v & (0x1f << 5)) << 6) | ((v & (0x1c << 5)) << 1) // green
                    | ((v & (0x1f << 0)) << 3) | ((v & (0x1c << 0)) >> 2); // blue;
        }
    }

    /**
     * ---
     */
    protected static void readRGBs555to24(ByteBuffer in, int[] i, int off, int len) throws IOException {
        for (int j = off, end = off + len; j < end; j++) {
            int v = in.getShort();
            i[j] = ((v & (0x1f << 10)) << 9) | ((v & (0x1c << 10)) << 4) // red
                    | ((v & (0x1f << 5)) << 6) | ((v & (0x1c << 5)) << 1) // green
                    | ((v & (0x1f << 0)) << 3) | ((v & (0x1c << 0)) >> 2); // blue;
        }
    }

    /**
     * Reads 16-bit RGB and converts it to 24-bit RGB BE. Endian of input is
     * defined by byte buffer.
     */
    protected int readRGB555to24(ImageInputStream in) throws IOException {
        int v = in.readUnsignedShort();
        return ((v & (0x1f << 10)) << 9) | ((v & (0x1c << 10)) << 4) // red
                | ((v & (0x1f << 5)) << 6) | ((v & (0x1c << 5)) << 1) // green
                | ((v & (0x1f << 0)) << 3) | ((v & (0x1c << 0)) >> 2); // blue;
    }

    protected static int readRGB555to24(ByteBuffer in) throws IOException {
        int v = in.getShort();
        return ((v & (0x1f << 10)) << 9) | ((v & (0x1c << 10)) << 4) // red
                | ((v & (0x1f << 5)) << 6) | ((v & (0x1c << 5)) << 1) // green
                | ((v & (0x1f << 0)) << 3) | ((v & (0x1c << 0)) >> 2); // blue;
    }
}
