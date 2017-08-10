/* @(#)ANIMKeyFrame.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.iff.IFFParser;
import org.monte.media.exception.ParseException;

/**
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class ANIMKeyFrame
        extends ANIMFrame {

    private int compression;
    protected final static int VDAT_ID = IFFParser.stringToID("VDAT");

    public ANIMKeyFrame() {
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    /** For possible values see {@link ANIMMovieTrack}. */
    public void setCompression(int compression) {
        this.compression = compression;
    }

    @Override
    public void decode(AmigaBitmapImage bitmap, ANIMMovieTrack track) {
        switch (compression) {

            case ANIMMovieTrack.CMP_BYTE_RUN_1:
                unpackByteRun1(data, bitmap.getBitmap());
                break;
            case ANIMMovieTrack.CMP_VERTICAL:
                unpackVertical(data, bitmap);
                break;
            case ANIMMovieTrack.CMP_NONE:
            default:
                System.arraycopy(data, 0, bitmap.getBitmap(), 0, data.length);
                break;
        }
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
     */
    public static int unpackByteRun1(byte[] in, byte[] out) {
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
            System.out.println("ANIMKeyFrame.unpackByteRun1(): " + e);
            System.out.println("  Plane-Index: " + iOut + " Plane size:" + out.length);
            System.out.println("  Buffer-Index: " + iIn + " Buffer size:" + in.length);
            System.out.println("  Command: " + n);
        }
        return iOut;
    }
    /**
     * Vertical run decoder.
     * <p>
     * Each plane is stored in a separate VDAT chunk.
     * <p>
     * A VDAT chunk consists of an id, a length, and a body.
     * <pre>
     * struct {
     *    uint16 id;  // The 4 ASCII characters "VDAT"
     *    uint16 length,
     *    byte[length] body
     * }
     * </pre>
     * The body consists of a command list and a data list.
     * <pre>
     * struct {
     *    uint16         cnt;        // Command count + 2
     *    uint8[cnt - 2] cmd;        // The commands
     *    uint16[]       data;       // Data words
     * }
     * </pre>
     * Pseudo code for the unpacker:
     * <pre>
     * UnPacker:
     *  Read cnt;
     *  LOOP cnt - 2 TIMES
     *      Read the next command byte into cmd
     *      SELECT cmd FROM
     *          0 =&gt;
     *                  Read the next data word into n
     *                  Copy the next n data words literally
     *          1    =&gt;
     *                  Read the next data word into n
     *                  Replicate the next data word n times
     *          [2..127] =&gt;
     *                  Replicate the next data word cmd times
     *          [-1..-128] =&gt;
     *                  Copy the next -cmd data words literally
     *      ENDCASE;
     *      IF end of data reached THEN EXIT END;
     *   ENDLOOP;
     * </pre>
     *
     */
    public void unpackVertical(byte[] in, AmigaBitmapImage bm)
             {
        byte[] out = bm.getBitmap();
        int iIn = 0; // input index
        int endOfData = 0;
        int bmhdWidth = bm.getWidth();
        int bmhdHeight = bm.getHeight();
        int bmhdNbPlanes = bm.getDepth();
        byte buf[] = new byte[bmhdWidth * bmhdHeight / 8]; // temporary bitplane buffer
        int scanlineStride = bm.getScanlineStride();
        int columnCount = (bmhdWidth / 8) * bmhdHeight;
        int columnStride = bmhdHeight * 2;


        try {
            for (int p = 0; p < bmhdNbPlanes; p++) {
                // Each plane is stored in a separate VDAT chunk.
                // ----------------------------------------------
                int iBuf = 0;
                iIn = endOfData;

                // read the "VDAT" chunk id and length
                int id = (in[iIn++] & 0xff) << 24 | (in[iIn++] & 0xff) << 16 | (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                if (id != VDAT_ID) {
                    throw new ParseException("Illegal VDAT chunk ID:" + IFFParser.idToString(id) + " at " + (iIn - 4));
                }
                long length = (in[iIn++] & 0xffL) << 24 | (in[iIn++] & 0xffL) << 16 | (in[iIn++] & 0xffL) << 8 | (in[iIn++] & 0xffL);
                if (iIn + length > in.length) {
                    throw new ParseException("Illegal VDAT chunk length:" + length + " at " + (iIn - 4));
                }
                endOfData += length + 8;

                // The body consists of a command list and a data list.
                // ----------------------------------------------------

                // read the command count, compute the offset to the data list
                int cnt = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                int iCmd = iIn;
                iIn = iIn + cnt - 2;
                try {
                    // Process the commands until all commands read or end of data reached
                    for (int i = cnt - 2; i > 0 && iIn < endOfData; i--) {
                        int cmd = in[iCmd++];
                        if (cmd == 0) {
                            // 0 => Read the next data word into n
                            //      Copy the next n data words literally
                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            for (n *= 2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        } else if (cmd == 1) {
                            // 1 => Read the next data word into n
                            //      Replicate the next data word n times
                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            byte dhigh = in[iIn++]; // high byte
                            byte dlow = in[iIn++]; // low byte
                            for (; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else if (cmd >= 2) {
                            // [2..127] => Replicate the next data word cmd times
                            byte dhigh = in[iIn++]; // high byte
                            byte dlow = in[iIn++]; // low byte
                            for (int n = cmd; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else {
                            // [-1..-128] => Copy the next -cmd data words literally
                            for (int n = cmd * -2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("IndexOutOfBounds in bitplane " + p);
                    e.printStackTrace();
                }

                // Copy buffer into bitmap
                int bitplaneOffset = bm.getBitplaneStride() * p;
                for (int xBuf = 0, xOut = 0; xBuf < columnCount; xBuf += columnStride, xOut += 2) {
                    for (int yBuf = 0, yOut = bitplaneOffset; yBuf < columnStride; yBuf += 2, yOut += scanlineStride) {
                        out[xOut + yOut] = buf[xBuf + yBuf];
                        out[xOut + 1 + yOut] = buf[xBuf + 1 + yBuf];
                    }
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            // System.out.println("ILBMDecoder.unpackVertical(): " + e);
        }
    }

}
