/*
 * @(#)Cta708Parser.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta708;

import org.monte.media.io.ImageInputStreamAdapter;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Parses a CTA-708 stream containing closed captions.
 * <p>
 *
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Digital Television (DTV) Closed Captioning. CTA-708-E S-2023. August 2013.</dt>
 *     <dd><a href="https://shop.cta.tech/collections/standards/products/digital-television-dtv-closed-captioning">
 *         ANSI CTA-708-E S-2023 + Errata Letter and Replacement Pages FINAL.pdf</a></dd>
 * </dl>
 */
public class Cta708Parser {
    /**
     * Number of bytes of a syntactic element.
     */
    private final static int[] numBytes = new int[256];

    static {
        // Ony-byte syntactic elements
        Arrays.fill(numBytes, 1);
        // Syntactic elements starting with code 0x10 (EXT1) may be two or more bytes in length!
        Arrays.fill(numBytes, 0x10, 0x10 + 1, 2);
        // Two-byte syntactic elements
        Arrays.fill(numBytes, 0x11, 0x17 + 1, 2);
        // Three-byte syntactic elements
        Arrays.fill(numBytes, 0x18, 0x1f + 1, 3);
        // Syntactic elements starting with code 0x80..0x92 may be one or more bytes in length!
        Arrays.fill(numBytes, 0x80, 0x92 + 1, 2);
        // Syntactic elements starting with code 0x87..0x9f may be one or more bytes in length!
        Arrays.fill(numBytes, 0x97, 0x9f + 1, 2);
    }

    /**
     * Skip this opcode.
     */
    private final static int TT_SKIP = -1;
    /**
     * Extend the DTVCC code space.
     */
    private final static int TT_EXT1 = -2;
    /**
     * The ETX code has a special use at the end of a caption text
     * segment to terminate the segment when the segment is not immediately
     * followed by another caption command.
     */
    private final static int TT_ETX = -3;
    /**
     * Carriage Return (CR) moves the current entry point to the beginning of the next row.
     * If the next row is below the visible window, the window “rolls up” .
     */
    private final static int TT_CR = -4;
    /**
     * Horizontal Carriage Return (HCR) moves the current entry point to the beginning of the current row without row
     * increment or decrement. It shall erase all text on the row.
     */
    private final static int TT_HCR = -5;
    /**
     * Form Feed (FF) erases all text in the window and moves the cursor to the first character position in the
     * window (0,0). This is equivalent to specifying the window in a ClearWindows (CLW) command, followed by
     * SetPenLocation (SPL) (0, 0).
     */
    private final static int TT_FF = -6;

    /**
     * Code type: Values greater or equal are characters.
     */
    private final static int[] codeType = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            codeType[i] = i;
        }
        codeType[0x03] = TT_ETX;
        codeType[0x0C] = TT_FF;
        codeType[0x0D] = TT_CR;
        codeType[0x0E] = TT_HCR;
        codeType[0x10] = TT_EXT1;
        codeType[0xff] = '♪';
        Arrays.fill(codeType, 0x1, 0x2 + 1, TT_SKIP);
        Arrays.fill(codeType, 0x4, 0x7 + 1, TT_SKIP);
        Arrays.fill(codeType, 0x9, 0xB + 1, TT_SKIP);
        Arrays.fill(codeType, 0xf, 0xf + 1, TT_SKIP);
        Arrays.fill(codeType, 0x11, 0x17 + 1, TT_SKIP);
        Arrays.fill(codeType, 0x19, 0x1f + 1, TT_SKIP);
    }


    public String parse(InputStream in) throws IOException {
        CCDataInputStream ccin = new CCDataInputStream(in, Integer.MAX_VALUE);
        StringBuilder buf = new StringBuilder();
        for (int b = ccin.read(); b >= 0; b = ccin.read()) {
            switch (codeType[b]) {
                case TT_SKIP -> ccin.skipNBytes(numBytes[b] - 1);
                default -> {
                    buf.append((char) b);
                    for (int i = 1, n = numBytes[b]; i < n; i++) {
                        buf.append((char) ccin.read());
                    }
                }
            }

        }
        return buf.toString();
    }

    public void parse(ImageInputStream in) throws IOException {
        parse(new ImageInputStreamAdapter(in));
    }

    public String parseToStringWithOpCodes(InputStream in) throws IOException {
        in.skipNBytes(3);// Always starts with the String "ih"
        return parse(in);
/*
        StringBuffer buf=new StringBuffer();
        StringBuffer buf2=new StringBuffer();
        buf2.append("\nnew byte[]{");
        for(int b = in.read();b>=0;b=in.read()){
          //  buf.append("0x").append(Integer.toHexString(b));
            buf.append((char)b);
            buf2.append("(byte)0x");
            buf2.append(Integer.toHexString(b));
            buf2.append(",");
          //  buf.append(" ");
        }
        buf2.append("};");
        return buf.toString()+buf2.toString();
*/

    }

    public String parseToStringWithOpCodes(ImageInputStream in) throws IOException {
        return parseToStringWithOpCodes(new ImageInputStreamAdapter(in));
    }

}
