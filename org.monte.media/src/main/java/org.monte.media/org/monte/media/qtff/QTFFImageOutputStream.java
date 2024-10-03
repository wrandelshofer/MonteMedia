/*
 * @(#)QTFFImageOutputStream.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.qtff;


import org.monte.media.io.FilterImageOutputStream;
import org.monte.media.util.ByteArrays;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.GregorianCalendar;

/**
 * This output stream filter supports common data types used inside
 * of a QuickTime Data Atom.
 *
 * @author Werner Randelshofer
 */
public class QTFFImageOutputStream extends FilterImageOutputStream {

    protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();
    private final byte[] byteBuffer = new byte[8];

    public QTFFImageOutputStream(ImageOutputStream out) throws IOException {
        super(out);
    }

    /**
     * Writes an Atom Type identifier (4 bytes).
     *
     * @param s A string with a length of 4 characters.
     */
    public void writeType(String s) throws IOException {
        if (s == null) {
            writeInt(0);
            return;
        }
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            write(s.getBytes(StandardCharsets.US_ASCII), 0, 4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }


    /**
     * Writes a <code>BCD2</code> (one byte) to the underlying output stream.
     *
     * @param v an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeBCD2(int v) throws IOException {
        write(((v % 100 / 10) << 4) | (v % 10));
    }

    /**
     * Writes a <code>BCD4</code> (two bytes) to the underlying output stream.
     *
     * @param v an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeBCD4(int v) throws IOException {
        writeShort(
                ((v % 10000 / 1000) << 12)
                        | ((v % 1000 / 100) << 8)
                        | ((v % 100 / 10) << 4)
                        | (v % 10)
        );
    }

    /**
     * Writes a 32-bit Mac timestamp (seconds since 1902).
     *
     * @param date the date to be converted to a Mac timestamp
     * @throws IOException if an I/O error occurs
     */
    public void writeMacTimestamp(Instant date) throws IOException {
        long millis = date.toEpochMilli();
        long qtMillis = millis - MAC_TIMESTAMP_EPOCH;
        long qtSeconds = qtMillis / 1000;
        writeInt((int) qtSeconds);
    }

    /**
     * Writes 32-bit fixed-point number divided as 16.16.
     *
     * @param f an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeFixed16D16(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) Math.floor(v);
        int fractionPart = (int) ((v - wholePart) * 65536);
        int t = (wholePart << 16) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeInt(t);
    }

    /**
     * Writes 32-bit fixed-point number divided as 2.30.
     *
     * @param f an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeFixed2D30(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) v;
        int fractionPart = (int) ((v - wholePart) * 1073741824);
        int t = (wholePart << 30) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeInt(t);
    }

    /**
     * Writes 32-bit fixed-point number divided as 8.8.
     *
     * @param f an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeFixed8D8(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) v;
        int fractionPart = (int) ((v - wholePart) * 256);
        int t = (wholePart << 8) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeShort(t);
    }

    /**
     * Writes a zero-terminated C String.
     *
     * @param s the string to be written
     * @throws IOException if an I/O error occurs
     */
    public void writeCString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            byte ch = (byte) s.charAt(i);
            if (ch == 0) break;
            write(ch);
        }
        write(0);
    }

    /**
     * Writes a Pascal String.
     *
     * @param s the string to be written
     * @throws IOException if an I/O error occurs
     */
    public void writePString(String s) throws IOException {
        if (s == null) {
            write(0);
            writeShort(0);
            return;
        }
        if (s.length() > 0xffff) {
            throw new IllegalArgumentException("String too long for PString");
        }
        if (!s.isEmpty() && s.length() < 256) {
            write(s.length());
        } else {
            write(0);
            writeShort(s.length());
        }
        for (int i = 0; i < s.length(); i++) {
            write(s.charAt(i));
        }
    }

    /**
     * Writes a Pascal String padded to the specified fixed size in bytes.
     *
     * @param s      the string to be written
     * @param length the fixed size in bytes
     * @throws IOException if an I/O error occurs
     */
    public void writePString(String s, int length) throws IOException {
        if (s.length() > length) {
            throw new IllegalArgumentException("String too long for PString of length " + length);
        }
        if (!s.isEmpty() && s.length() < 256) {
            write(s.length());
        } else {
            write(0);
            writeShort(s.length()); // increments +2
        }
        for (int i = 0; i < s.length(); i++) {
            write(s.charAt(i));
        }

        // write pad bytes
        for (int i = 1 + s.length(); i < length; i++) {
            write(0);
        }
    }


    public void writeShorts(short[] s, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }

        byte[] b = new byte[len * 2];
        int boff = 0;
        for (int i = 0; i < len; i++) {
            short v = s[off + i];
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v);
        }

        write(b, 0, len * 2);
    }

    public void writeInts(int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 4];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            b[boff++] = (byte) (v >>> 24);
            b[boff++] = (byte) (v >>> 16);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v);
        }

        write(b, 0, len * 4);
    }


    public void writeInt24(int v) throws IOException {
        ByteArrays.setIntBE(byteBuffer, 0, v);
        write(byteBuffer, 0, 3);
    }

    public void writeInts24(int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            //b[boff++] = (byte)(v >>> 24);
            b[boff++] = (byte) (v >>> 16);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v);
        }

        write(b, 0, len * 3);
    }

    public void writeUInt(long value) throws IOException {
        writeInt((int) value);
    }

    public void writeUShort(int value) throws IOException {
        writeShort(value);
    }
}
