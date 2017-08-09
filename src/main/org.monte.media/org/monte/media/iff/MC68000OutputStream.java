/* @(#)MC68000OutputStream.java
 * Copyright © 2006-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.iff;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * MC68000OutputStream.
 *
 * @author Werner Randelshofer
 * @version 1.1 2010-12-26 Added method writeType().
 * <br>1.0.1 2008-08-03 The ByteRun1 encoder incorrectly added 1 to its
 * index when flushing the literal run.
 * <br>1.0 December 25, 2006 Created.
 */
public class MC68000OutputStream extends FilterOutputStream {
    /**
     * The number of bytes written to the data output stream so far.
     * If this counter overflows, it will be wrapped to Integer.MAX_VALUE.
     */
    protected long written;

    /** Creates a new instance. */
    public MC68000OutputStream(OutputStream out) {
        super(out);
    }

    public void writeLONG(int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(4);
    }

    public void writeULONG(long v) throws IOException {
        out.write((int) ((v >>> 24) & 0xFF));
        out.write((int) ((v >>> 16) & 0xFF));
        out.write((int) ((v >>> 8) & 0xFF));
        out.write((int) ((v >>> 0) & 0xFF));
        incCount(4);
    }

    public void writeWORD(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    public void writeUWORD(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    public void writeUBYTE(int v) throws IOException {
        out.write((v >>> 0) & 0xFF);
        incCount(1);
    }

    /**
     * ByteRun1 Run Encoding.
     * <p>
     * The run encoding scheme in byteRun1 is best described by
     * pseudo code for the decoder Unpacker (called UnPackBits in the
     * Macintosh toolbox):
     * <pre>
     * UnPacker:
     *    LOOP until produced the desired number of bytes
     *       Read the next source byte into n
     *       SELECT n FROM
     *          [ 0..127 ] ⇒ copy the next n+1 bytes literally
     *          [-1..-127] ⇒ replicate the next byte -n+1 timees
     *          -128       ⇒ no operation
     *       ENDCASE
     *    ENDLOOP
     * </pre>
     */
    public void writeByteRun1(byte[] data) throws IOException {
        writeByteRun1(data, 0, data.length);
    }

    public void writeByteRun1(byte[] data, int offset, int length) throws IOException {
        int end = offset + length;

        // Start offset of the literal run
        int literalOffset = offset;
        int i;
        for (i = offset; i < end; i++) {
            // Read a byte
            byte b = data[i];

            // Count repeats of that byte
            int repeatCount = i + 1;
            for (; repeatCount < end; repeatCount++) {
                if (data[repeatCount] != b) {
                    break;
                }
            }
            repeatCount = repeatCount - i;

            if (repeatCount == 1) {
                // Flush the literal run, if it gets too large
                if (i - literalOffset > 127) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                    literalOffset = i;
                }

                // If the byte repeats just twice, and we have a literal
                // run with enough space, add it the literal run
            } else if (repeatCount == 2
                    && literalOffset < i && i - literalOffset < 127) {
                i++;
            } else {
                // Flush the literal run, if we have one
                if (literalOffset < i) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                }
                // Write the repeat run
                i += repeatCount - 1;
                literalOffset = i + 1;
                // We have to write multiple runs, if the byte repeats more
                // than 128 times.
                for (; repeatCount > 128; repeatCount -= 128) {
                    write(-127);
                    write(b);
                }
                write(-repeatCount + 1);
                write(b);
            }
        }

        // Flush the literal run, if we have one
        if (literalOffset < end) {
            write(i - literalOffset - 1);
            write(data, literalOffset, i - literalOffset);
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        incCount(len);
    }

    /**
     * Writes an chunk type identifier (4 bytes).
     * @param s A string with a length of 4 characters.
     */
    public void writeType(String s) throws IOException {
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            out.write(s.getBytes("ASCII"), 0, 4);
            incCount(4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Returns the current value of the counter <code>written</code>,
     * the number of bytes written to this data output stream so far.
     * If the counter overflows, it will be wrapped to Integer.MAX_VALUE.
     *
     * @return  the value of the <code>written</code> field.
     * @see     java.io.DataOutputStream#written
     */
    public final long size() {
        return written;
    }

    /**
     * Sets the value of the counter <code>written</code> to 0.
     */
    public void clearCount() {
        written = 0;
    }
    /**
     * Increases the written counter by the specified value
     * until it reaches Long.MAX_VALUE.
     */
    protected void incCount(int value) {
        long temp = written + value;
        if (temp < 0) {
            temp = Long.MAX_VALUE;
        }
        written = temp;
    }
}
