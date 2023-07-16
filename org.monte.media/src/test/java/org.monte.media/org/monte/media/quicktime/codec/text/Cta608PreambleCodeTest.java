/*
 * @(#)Cta608ControlTest.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.quicktime.codec.text.cta608.PacToken;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cta608PreambleCodeTest {
    record PacData(short opCode, int channel, int row, PacToken.Attributes attributes, boolean underline) {
    }

    @ParameterizedTest
    @MethodSource("pacDataProvider")
    public void shouldEncodePac(PacData data) throws Exception {
        short code = data.opCode;
        PacToken encoded = new PacToken(data.channel, data.row, data.attributes, data.underline);
        System.out.println("expected: " + Integer.toHexString(code & 0x7f7f));
        System.out.println("actual  : " + Integer.toHexString(encoded.getCodeWithoutParity()));

        assertEquals(Integer.toHexString(0x7f7f & code), Integer.toHexString(0x7f7f & encoded.getCodeWithoutParity()), "opCode");
        assertEquals(data.row(), encoded.getRow(), "row");
        assertEquals(data.attributes(), encoded.getTextAttributes(), "text attributes");
        assertEquals(data.channel(), encoded.getChannel(), "channel");
        assertEquals(data.underline(), encoded.isUnderline(), "underline");
    }


    @ParameterizedTest
    @MethodSource("pacDataProvider")
    public void shouldDecodePac(PacData data) throws Exception {
        short code = data.opCode;
        PacToken decoded = new PacToken(code);
        System.out.println("expected: " + Integer.toHexString(code & 0x7f7f));
        System.out.println("actual  : " + Integer.toHexString(decoded.getCodeWithoutParity()));

        assertEquals(Integer.toHexString(0x7f7f & code), Integer.toHexString(0x7f7f & decoded.getCodeWithoutParity()), "opCode");
        assertEquals(data.row(), decoded.getRow(), "row");
        assertEquals(data.attributes(), decoded.getTextAttributes(), "text attributes");
        assertEquals(data.channel(), decoded.getChannel(), "channel");
        assertEquals(data.underline(), decoded.isUnderline(), "underline");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldEncodeChannel(int channel) throws Exception {
        PacToken actual = new PacToken(channel, 1, PacToken.Attributes.WHITE, false);
        System.out.println(channel + " " + Integer.toHexString(actual.getCodeWithoutParity()));
        assertEquals(channel, actual.getChannel());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
    public void shouldEncodeRow(int row) throws Exception {
        PacToken actual = new PacToken(1, row, PacToken.Attributes.WHITE, false);
        System.out.println(row + " " + Integer.toHexString(actual.getCodeWithoutParity()));
        assertEquals(row, actual.getRow());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    public void shouldEncodeTextAttributes(int ordinal) throws Exception {
        PacToken.Attributes attrs = PacToken.Attributes.values()[ordinal];
        PacToken actual = new PacToken(1, 1, attrs, false);
        System.out.println(attrs + " " + Integer.toHexString(actual.getCodeWithoutParity()));
        assertEquals(attrs, actual.getTextAttributes());
    }


    public static Stream<PacData> pacDataProvider() {
        return Stream.of(
                //--Table 53 Preamble Address Codes PACs:
                // Rows 1 to 15, White
                new PacData((short) 0x1140, 1, 1, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1940, 2, 1, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1160, 1, 2, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1960, 2, 2, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1240, 1, 3, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1a40, 2, 3, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1260, 1, 4, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1a60, 2, 4, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1540, 1, 5, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1d40, 2, 5, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1560, 1, 6, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1d60, 2, 6, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1640, 1, 7, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1e40, 2, 7, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1660, 1, 8, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1e60, 2, 8, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1740, 1, 9, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1f40, 2, 9, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1760, 1, 10, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1f60, 2, 10, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1040, 1, 11, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1840, 2, 11, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1340, 1, 12, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1b40, 2, 12, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1360, 1, 13, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1b60, 2, 13, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1440, 1, 14, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1c40, 2, 14, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1460, 1, 15, PacToken.Attributes.WHITE, false),
                new PacData((short) 0x1c60, 2, 15, PacToken.Attributes.WHITE, false),

                // Row 1, Text Attributes
                new PacData((short) 0x1141, 1, 1, PacToken.Attributes.WHITE, true),
                new PacData((short) 0x1142, 1, 1, PacToken.Attributes.GREEN, false),
                new PacData((short) 0x1144, 1, 1, PacToken.Attributes.BLUE, false),
                new PacData((short) 0x1146, 1, 1, PacToken.Attributes.CYAN, false),
                new PacData((short) 0x1148, 1, 1, PacToken.Attributes.RED, false),
                new PacData((short) 0x114A, 1, 1, PacToken.Attributes.YELLOW, false),
                new PacData((short) 0x114C, 1, 1, PacToken.Attributes.MAGENTA, false),
                new PacData((short) 0x114E, 1, 1, PacToken.Attributes.WHITE_ITALICS, false),

                // Row 1, Indentation
                new PacData((short) 0x1150, 1, 1, PacToken.Attributes.INDENT_0, false),
                new PacData((short) 0x1152, 1, 1, PacToken.Attributes.INDENT_4, false),
                new PacData((short) 0x1154, 1, 1, PacToken.Attributes.INDENT_8, false),
                new PacData((short) 0x1156, 1, 1, PacToken.Attributes.INDENT_12, false),
                new PacData((short) 0x1158, 1, 1, PacToken.Attributes.INDENT_16, false),
                new PacData((short) 0x115A, 1, 1, PacToken.Attributes.INDENT_20, false),
                new PacData((short) 0x115C, 1, 1, PacToken.Attributes.INDENT_24, false),
                new PacData((short) 0x115E, 1, 1, PacToken.Attributes.INDENT_28, false)
        );
    }
}