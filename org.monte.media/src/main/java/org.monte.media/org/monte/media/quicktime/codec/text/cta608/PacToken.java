/*
 * @(#)Cta608PacToken.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

import java.util.Objects;

/**
 * CTA-608 Preamble address code and tab offsets (PAC).
 * <p>
 * This is a 16-bit unsigned short in big-endian order ({@code uint16BE}).
 * <pre>
 *     15     12 11     8   7     4   3     0
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *     |P|0|0|1| |C|r|r|r| |P|1|r|a| |a|a|a|u|
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *
 *     bits   interpretation
 *     15     odd parity bit of bits 8-19 (shown as 'P')
 *     14-13  always 0 (shown as '00').
 *     12     always 1 (shown as '1').
 *     11     channel (shown as 'C')
 *     10-8   row position indicator low-bits (shown as 'rrr')
 *     7      odd parity bit of bits 0-7 (shown as 'P')
 *     6      always 1 (shown as '1')
 *     5      row position indicator high-bit (shown as 'r')
 *     4-1    text attributes indicator (shown as 'aaaa')
 *     u      underline indicator (shown as 'U')
 * </pre>
 * <p>
 * The screen area for a closed caption provides space for 15 rows with 32 columns of text.
 * This allows to show up to 480 characters on the same screen.
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Line 21 Data Services. ANSI/CTA-608-E S-2019. April 2008.</dt>
 *     <dd><a href="https://shop.cta.tech/products/line-21-data-services">ANSI-CTA-608-E-S-2019-Final.pdf</a></dd>
 * </dl>
 */
public non-sealed class PacToken implements Token {

    /**
     * Text attribute.
     * <p>
     * The ordinal is significant.
     */
    public enum Attributes {
        WHITE, GREEN, BLUE, CYAN, RED, YELLOW, MAGENTA,
        /**
         * Italics assigns white as the color attribute.
         */
        WHITE_ITALICS,
        /**
         * Indent codes assign white as the color attribute.
         */
        INDENT_0, INDENT_4, INDENT_8, INDENT_12, INDENT_16, INDENT_20, INDENT_24, INDENT_28
    }

    private final static int[] rowEncode = {
            -1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15, 0, 6, 7, 8, 9
    };
    private final static int[] rowDecode = {
            11, -1, 1, 2, 3, 4, 12, 13, 14, 15, 5, 6, 7, 8, 9, 10
    };

    private final short code;

    /**
     * Constructs a new instance.
     *
     * @param code a PAC code
     */
    public PacToken(short code) {
        if ((code & 0b0111_0000_0100_0000) != 0b0001_0000_0100_0000)
            throw new IllegalArgumentException("code=" + Integer.toHexString(code));
        // clear parity bits
        this.code = Token.fixParityBits(code);
    }

    /**
     * Constructs a new instance.
     *
     * @param channel    value in the range [1,2].
     * @param row        value in the range [1,15].
     * @param attributes text attributes, non-null
     * @param underline  true if underline
     */
    public PacToken(int channel, int row, Attributes attributes, boolean underline) {
        if (channel < 1 || channel > 2) throw new IllegalArgumentException("channel=" + channel);
        if (row < 1 || row > 15) throw new IllegalArgumentException("row=" + row);
        Objects.requireNonNull(attributes, "textAttribute is null");
        int attrs = attributes.ordinal();
        int u = underline ? 1 : 0;
        int rrrr = rowEncode[row];
        int low = 0b0100_0000 | ((rrrr & 0b0001) << 5) | (attrs << 1) | u;
        int high = 0b0001_0000 | ((channel - 1) << 3) | ((rrrr & 0b1110) >>> 1);
        this.code = Token.fixParityBits((short) ((high << 8) | low));
    }

    public int getRow() {
        int rowHigh = (code & 0b0000_0111_0000_0000) >> 7;
        int rowLow = (code & 0b0000_0000_0010_0000) >> 5;
        int row = rowHigh | rowLow;
        return rowDecode[row];
    }

    public int getChannel() {
        return ((code & 0b0000_1000_0000_0000) >> 11) + 1;
    }

    public Attributes getTextAttributes() {
        int attrs = (code & 0b1_1110) >>> 1;
        return Attributes.values()[attrs];
    }

    /**
     * Gets the code with parity bits.
     */
    public short getCode() {
        return code;
    }

    /**
     * Gets the code without parity bits.
     */
    public short getCodeWithoutParity() {
        return (short) (code & 0x7f7f);
    }

    public boolean isUnderline() {
        return (code & 1) != 0;
    }
}
