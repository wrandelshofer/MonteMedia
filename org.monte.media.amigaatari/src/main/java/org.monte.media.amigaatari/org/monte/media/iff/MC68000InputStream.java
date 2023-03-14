/*
 * @(#)MC68000InputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.iff;

import org.monte.media.io.ByteArray;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A MC 68000 input stream lets an application read primitive data types in the
 * MC 68000 CPU format from an underlying input stream.
 *
 * <p>
 * This stream filter is suitable for IFF-EA85 files.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public class MC68000InputStream
        extends FilterInputStream {

    private long scan, mark;
    private byte byteBuffer[] = new byte[8];

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     */
    public MC68000InputStream(InputStream in) {
        super(in);
    }

    /**
     * Read 1 byte from the input stream and interpret them as an MC 68000 8 Bit
     * unsigned UBYTE value.
     */
    public int readUBYTE()
            throws IOException {
        int b0 = in.read();
        if (b0 == -1) {
            throw new EOFException();
        }
        scan += 1;

        return b0 & 0xff;
    }

    /**
     * Read 2 bytes from the input stream and interpret them as an MC 68000 16
     * Bit signed WORD value.
     */
    public short readWORD()
            throws IOException {
        readFully(byteBuffer, 0, 2);
        return ByteArray.getShortBE(byteBuffer, 0);
    }

    /**
     * Read 2 bytes from the input stream and interpret them as an MC 68000 16
     * Bit unsigned UWORD value.
     */
    public int readUWORD()
            throws IOException {
        return readWORD() & 0xffff;
    }

    /**
     * Read 4 bytes from the input stream and interpret them as an MC 68000 32
     * Bit signed LONG value.
     */
    public int readLONG()
            throws IOException {
        readFully(byteBuffer, 0, 4);
        return ByteArray.getIntBE(byteBuffer, 0);
    }

    /**
     * Read 8 bytes from the input stream and interpret them as 64 Bit signed
     * LONG LONG value.
     */
    public long readINT64()
            throws IOException {
        readFully(byteBuffer, 0, 8);
        return ByteArray.getLongBE(byteBuffer, 0);
    }

    /**
     * Read 4 Bytes from the input Stream and interpret them as an unsigned
     * Integer value of MC 68000 type ULONG.
     */
    public long readULONG()
            throws IOException {
        return (long) (readLONG()) & 0x00ffffffffL;
    }

    /**
     * Align to an even byte position in the input stream. This will skip one
     * byte in the stream if the current read position is not even.
     */
    public void align()
            throws IOException {
        if (scan % 2 == 1) {
            skipFully(1);
        }
    }

    /**
     * Get the current read position within the file (as seen by this input
     * stream filter).
     */
    public long getScan() {
        return scan;
    }

    /**
     * Reads one byte.
     */
    public int read()
            throws IOException {
        int data = in.read();
        scan++;
        return data;
    }

    /**
     * Reads a sequence of bytes.
     */
    public void readFully(byte[] b, int offset, int length)
            throws IOException {
        int count = 0;
        while (count < length) {
            int current = in.read(b, offset + count, length - count);
            if (count < 0) {
                throw new EOFException();
            }
            count += current;
            scan += current;
        }
    }

    /**
     * Reads a sequence of bytes.
     */
    public int read(byte[] b, int offset, int length)
            throws IOException {
        int count = in.read(b, offset, length);
        if (count > 0) {
            scan += count;
        }
        return count;
    }

    /**
     * Marks the input stream.
     *
     * @param    readlimit    The maximum limit of bytes that can be read before the
     * mark position becomes invalid.
     */
    public void mark(int readlimit) {
        in.mark(readlimit);
        mark = scan;
    }

    /**
     * Repositions the stream at the previously marked position.
     *
     * @throws IOException If the stream has not been marked or if the mark
     *                     has been invalidated.
     */
    public void reset()
            throws IOException {
        in.reset();
        scan = mark;
    }

    /**
     * Skips over and discards n bytes of data from this input stream. This skip
     * method tries to skip the p
     */
    public long skip(long n)
            throws IOException {
        long skipped = in.skip(n);
        scan += skipped;
        return skipped;
    }

    /**
     * Skips over and discards n bytes of data from this input stream. Throws
     *
     * @param n the number of bytes to be skipped.
     * @throws EOFException if this input stream reaches the end before
     *                      skipping all the bytes.
     */
    public void skipFully(long n)
            throws IOException {
        int total = 0;
        int cur = 0;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }
        if (cur == 0) {
            throw new EOFException();
        }
        scan += total;
    }

    /**
     * ByteRun1 run decoder.
     * <p>
     * The run encoding scheme by <em>byteRun1</em> is best described by pseudo
     * code for the decoder <em>Unpacker</em> (called <em>UnPackBits</em> in the
     * Macintosh toolbox.
     * <pre>
     * UnPacker:
     *  LOOP until produced the desired number of bytes
     *      Read the next source byte into n
     *      SELECT n FROM
     *          [0..127] ⇒ copy the next n+1 bytes literally
     *          [-1..-127] ⇒ replicate the next byte -n+1 times
     *          -128    ⇒ no operation
     *      ENDCASE;
     *   ENDLOOP;
     * </pre>
     *
     * @param in  input
     * @param out output
     */
    public static int unpackByteRun1(byte[] in, byte[] out)
            throws IOException {
        int iOut = 0; // output array index
        int iIn = 0; // input array index
        int n = 0; // The unpack command
        byte copyByte;

        try {
            while (iOut < out.length) {
                n = in[iIn++];
                if (n >= 0) { // [0..127] => copy the next n+1 bytes literally
                    n = n + 1;
                    System.arraycopy(in, iIn, out, iOut, n);
                    iOut += n;
                    iIn += n;
                } else {
                    if (n != -128) {//[-1..-127] ⇒ replicate the next byte -n+1 times
                        copyByte = in[iIn++];
                        for (; n < 1; n++) {
                            out[iOut++] = copyByte;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("MC68000InputStream.unpackByteRun1(): " + e);
            System.out.println("  Plane-Index: " + iOut + " Plane size:" + out.length);
            System.out.println("  Buffer-Index: " + iIn + " Buffer size:" + in.length);
            System.out.println("  Command: " + n);
        }
        return iOut;
    }
}
