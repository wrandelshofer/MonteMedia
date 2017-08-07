/* @(#)MC68000InputStream.java
 * Copyright © 1999-2003 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.iff;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A MC 68000 input stream lets an application read primitive data
 * types in the MC 68000 CPU format from an underlying input stream.
 *
 * <p>This stream filter is suitable for IFF-EA85 files.
 *
 * @author	Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.3 2010-08-03 Moved unpackByteRun1 method into this class.
 * <br>1.2.1 2004-08-09 Read methods for primitives throw now EOFException's,
 * when the unexpected EOF's occur.
 * <br>1.2 2003-04-01 Method skipFully added.
 * <br>1.1  2000-06-11 Method readFully added.
 * <br>1.0  1999-10-19
 * <br>0.1  1999-01-02	Created.
 */
public class MC68000InputStream
extends FilterInputStream {
    private long scan_, mark_;
    
    /**
     * Creates a new instance.
     *
     * @param  in   the input stream.
     */
    public MC68000InputStream(InputStream in)
    { super(in); }
    
    /**
     * Read 1 byte from the input stream and interpret
     * them as an MC 68000 8 Bit unsigned UBYTE value.
     */
    public int readUBYTE()
    throws IOException {
        int b0 = in.read();
        if (b0 == -1) {
            throw new EOFException();
        }
        scan_ += 1;
        
        return b0 & 0xff;
    }
    /**
     * Read 2 bytes from the input stream and interpret
     * them as an MC 68000 16 Bit signed WORD value.
     */
    public short readWORD()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b1 == -1) {
            throw new EOFException();
        }
        scan_ += 2;
        
        return (short) (((b0 & 0xff) << 8) | (b1 & 0xff));
    }
    /**
     * Read 2 bytes from the input stream and interpret
     * them as an MC 68000 16 Bit unsigned UWORD value.
     */
    public int readUWORD()
    throws IOException {
        return readWORD() & 0xffff;
    }
    /**
     * Read 4 bytes from the input stream and interpret
     * them as an MC 68000 32 Bit signed LONG value.
     */
    public int readLONG()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b3 == -1) {
            throw new EOFException();
        }
        scan_ += 4;
        
        return ((b0 & 0xff) << 24) 
        | ((b1 & 0xff) << 16)
        | ((b2 & 0xff) << 8)
        | (b3 & 0xff);
    }
    
   /**
     * Read 8 bytes from the input stream and interpret
     * them as 64 Bit signed LONG LONG value.
     */
    public long readINT64()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        int b5 = in.read();
        int b6 = in.read();
        int b7 = in.read();
        if (b7 == -1) {
            throw new EOFException();
        }
        scan_ += 4;
        
        return ((b0 & 0xffL) << 56) 
        | ((b1 & 0xffL) << 48)
        | ((b2 & 0xffL) << 40)
        | ((b3 & 0xffL) << 32)
                |  ((b4 & 0xffL) << 24) 
        | ((b5 & 0xffL) << 16)
        | ((b6 & 0xffL) << 8)
        | (b7 & 0xffL);
    }    
    
    /**
     * Read 4 Bytes from the input Stream and interpret
     * them as an unsigned Integer value of MC 68000
     * type ULONG.
     */
    public long readULONG()
    throws IOException {
        return (long)(readLONG()) & 0x00ffffffffL;
    }
    
    /**
     * Align to an even byte position in the input stream.
     * This will skip one byte in the stream if the current
     * read position is not even.
     */
    public void align()
    throws IOException {
        if (scan_ % 2 == 1) {
            in.skip(1);
            scan_++;
        }
    }
    
    /**
     * Get the current read position within the file (as seen
     * by this input stream filter).
     */
    public long getScan()
    { return scan_; }
    
    /**
     * Reads one byte.
     */
    public int read()
    throws IOException {
        int data = in.read();
        scan_++;
        return data;
    }
    /**
     * Reads a sequence of bytes.
     */
    public int readFully(byte[] b,int offset, int length)
    throws IOException {
        return read(b, offset, length);
    }
    /**
     * Reads a sequence of bytes.
     */
    public int read(byte[] b,int offset, int length)
    throws IOException {
        int count = 0;
        while (count < length) {
            count += in.read(b,offset+count,length-count);
        }
        scan_ += count;
        return count;
    }
    /**
     * Marks the input stream.
     * @param	readlimit	The maximum limit of bytes that can be read before
     * the mark position becomes invalid.
     */
    public void mark(int readlimit) {
        in.mark(readlimit);
        mark_ = scan_;
    }
    /**
     * Repositions the stream at the previously marked position.
     *
     * @exception  IOException  If the stream has not been marked or if the
     * mark has been invalidated.
     */
    public void reset()
    throws IOException {
        in.reset();
        scan_ = mark_;
    }
    /**
     * Skips over and discards n bytes of data from this input stream. This skip
     * method tries to skip the p
     */
    public long skip(long n)
    throws IOException {
        long skipped = in.skip(n);
        scan_ += skipped;
        return skipped;
    }
    /**
     * Skips over and discards n bytes of data from this input stream. Throws
     *
     * @param      n   the number of bytes to be skipped.
     * @exception  EOFException  if this input stream reaches the end before
     *               skipping all the bytes.
     */
    public void skipFully(long n)
    throws IOException {
        int total = 0;
        int cur = 0;
        
        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        if (cur == 0) throw new EOFException();
        scan_ += total;
    }

    /**
     * ByteRun1 run decoder.
     * <p>
     * The run encoding scheme by <em>byteRun1</em> is best described by pseudo
     * code for the decoder <em>Unpacker</em> (called <em>UnPackBits</em> in
     * the Macintosh toolbox.
     * <pre>
     * UnPacker:
     *  LOOP until produced the desired number of bytes
     *      Read the next source byte into n
     *      SELECT n FROM
     *          [0..127] =&gt; copy the next n+1 bytes literally
     *          [-1..-127] =&gt; replicate the next byte -n+1 times
     *          -128    =&gt; no operation
     *      ENDCASE;
     *   ENDLOOP;
     * </pre>
     *
     * @param in
     * @param out
     * @throws ParseException
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
                    if (n != -128) {//[-1..-127] =&gt; replicate the next byte -n+1 times
                        copyByte = in[iIn++];
                        for (; n < 1; n++) {
                            out[iOut++] = copyByte;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("PBMDecoder.unpackByteRun1(): " + e);
            System.out.println("  Plane-Index: " + iOut + " Plane size:" + out.length);
            System.out.println("  Buffer-Index: " + iIn + " Buffer size:" + in.length);
            System.out.println("  Command: " + n);
        }
        return iOut;
    }
}