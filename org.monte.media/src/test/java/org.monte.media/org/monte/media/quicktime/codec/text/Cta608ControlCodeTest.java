/*
 * @(#)Cta608ControlTest.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.quicktime.codec.text.cta608.CmdToken;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cta608ControlCodeTest {
    record ControlCodeData(short opCode, int channel, CmdToken.Command cc) {
    }

    @ParameterizedTest
    @MethodSource("controlCodesDataProvider")
    public void shouldEncodeControlCode(ControlCodeData data) throws Exception {
        short code = data.opCode;
        CmdToken encoded = new CmdToken(data.channel, data.cc);
        System.out.println("expected: " + Integer.toHexString(code & 0x7f7f));
        System.out.println("actual  : " + Integer.toHexString(encoded.getCodeWithoutParity()));

        assertEquals(Integer.toHexString(0x7f7f & code), Integer.toHexString(0x7f7f & encoded.getCodeWithoutParity()), "opCode");
        assertEquals(data.channel(), encoded.getChannel(), "channel");
        assertEquals(data.cc(), encoded.getOperation(), "control opCode");
    }


    @ParameterizedTest
    @MethodSource("controlCodesDataProvider")
    public void shouldDecodeControlCode(ControlCodeData data) throws Exception {
        short code = data.opCode;
        CmdToken decoded = new CmdToken(code);
        System.out.println("expected: " + Integer.toHexString(code & 0x7f7f));
        System.out.println("actual  : " + Integer.toHexString(decoded.getCodeWithoutParity()));

        assertEquals(Integer.toHexString(0x7f7f & code), Integer.toHexString(0x7f7f & decoded.getCodeWithoutParity()), "opCode");
        assertEquals(data.cc(), decoded.getOperation(), "control opCode");
        assertEquals(data.channel(), decoded.getChannel(), "channel");
    }

    @ParameterizedTest
    @MethodSource("midRowDataProvider")
    public void shouldEncodeMidRowCode(ControlCodeData data) throws Exception {
        shouldEncodeControlCode(data);
    }


    @ParameterizedTest
    @MethodSource("midRowDataProvider")
    public void shouldDecodeMidRowCode(ControlCodeData data) throws Exception {
        shouldDecodeControlCode(data);
    }

    @ParameterizedTest
    @MethodSource("bgFgDataProvider")
    public void shouldEncodeBgFgCode(ControlCodeData data) throws Exception {
        shouldEncodeControlCode(data);
    }


    @ParameterizedTest
    @MethodSource("bgFgDataProvider")
    public void shouldDecodeBgFgCode(ControlCodeData data) throws Exception {
        shouldDecodeControlCode(data);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldEncodeChannel(int channel) throws Exception {
        CmdToken actual = new CmdToken(channel, CmdToken.Command.BS);
        System.out.println(channel + " " + Integer.toHexString(actual.getCodeWithoutParity()));
        assertEquals(channel, actual.getChannel());
    }

    public static Stream<ControlCodeData> bgFgDataProvider() {
        return Stream.of(
                //--Table 3 Background and Foreground Attribute Codes
                new ControlCodeData((short) 0x1020, 1, CmdToken.Command.BWO),
                new ControlCodeData((short) 0x1021, 1, CmdToken.Command.BWS),
                new ControlCodeData((short) 0x1022, 1, CmdToken.Command.BGO),
                new ControlCodeData((short) 0x1023, 1, CmdToken.Command.BGS),
                new ControlCodeData((short) 0x1024, 1, CmdToken.Command.BBO),
                new ControlCodeData((short) 0x1025, 1, CmdToken.Command.BBS),
                new ControlCodeData((short) 0x1026, 1, CmdToken.Command.BCO),
                new ControlCodeData((short) 0x1027, 1, CmdToken.Command.BCS),
                new ControlCodeData((short) 0x1028, 1, CmdToken.Command.BRO),
                new ControlCodeData((short) 0x1029, 1, CmdToken.Command.BRS),
                new ControlCodeData((short) 0x102a, 1, CmdToken.Command.BYO),
                new ControlCodeData((short) 0x102b, 1, CmdToken.Command.BYS),
                new ControlCodeData((short) 0x102c, 1, CmdToken.Command.BMO),
                new ControlCodeData((short) 0x102d, 1, CmdToken.Command.BMS),
                new ControlCodeData((short) 0x102e, 1, CmdToken.Command.BAO),
                new ControlCodeData((short) 0x102f, 1, CmdToken.Command.BAS),
                new ControlCodeData((short) 0x172d, 1, CmdToken.Command.BT),
                new ControlCodeData((short) 0x172e, 1, CmdToken.Command.FA),
                new ControlCodeData((short) 0x172f, 1, CmdToken.Command.FAU)
        );
    }

    public static Stream<ControlCodeData> midRowDataProvider() {
        return Stream.of(
                //--Table 51 Mid-Row Codes
                new ControlCodeData((short) 0x1120, 1, CmdToken.Command.WHITE),
                new ControlCodeData((short) 0x1920, 2, CmdToken.Command.WHITE),
                new ControlCodeData((short) 0x1121, 1, CmdToken.Command.WHITE_UNDERLINE),
                new ControlCodeData((short) 0x1921, 2, CmdToken.Command.WHITE_UNDERLINE),
                new ControlCodeData((short) 0x1122, 1, CmdToken.Command.GREEN),
                new ControlCodeData((short) 0x1922, 2, CmdToken.Command.GREEN),
                new ControlCodeData((short) 0x1123, 1, CmdToken.Command.GREEN_UNDERLINE),
                new ControlCodeData((short) 0x1923, 2, CmdToken.Command.GREEN_UNDERLINE),
                new ControlCodeData((short) 0x1124, 1, CmdToken.Command.BLUE),
                new ControlCodeData((short) 0x1924, 2, CmdToken.Command.BLUE),
                new ControlCodeData((short) 0x1125, 1, CmdToken.Command.BLUE_UNDERLINE),
                new ControlCodeData((short) 0x1925, 2, CmdToken.Command.BLUE_UNDERLINE),
                new ControlCodeData((short) 0x1126, 1, CmdToken.Command.CYAN),
                new ControlCodeData((short) 0x1926, 2, CmdToken.Command.CYAN),
                new ControlCodeData((short) 0x1127, 1, CmdToken.Command.CYAN_UNDERLINE),
                new ControlCodeData((short) 0x1927, 2, CmdToken.Command.CYAN_UNDERLINE),
                new ControlCodeData((short) 0x1128, 1, CmdToken.Command.RED),
                new ControlCodeData((short) 0x1928, 2, CmdToken.Command.RED),
                new ControlCodeData((short) 0x1129, 1, CmdToken.Command.RED_UNDERLINE),
                new ControlCodeData((short) 0x1929, 2, CmdToken.Command.RED_UNDERLINE),
                new ControlCodeData((short) 0x112A, 1, CmdToken.Command.YELLOW),
                new ControlCodeData((short) 0x192A, 2, CmdToken.Command.YELLOW),
                new ControlCodeData((short) 0x112B, 1, CmdToken.Command.YELLOW_UNDERLINE),
                new ControlCodeData((short) 0x192B, 2, CmdToken.Command.YELLOW_UNDERLINE),
                new ControlCodeData((short) 0x112C, 1, CmdToken.Command.MAGENTA),
                new ControlCodeData((short) 0x192C, 2, CmdToken.Command.MAGENTA),
                new ControlCodeData((short) 0x112D, 1, CmdToken.Command.MAGENTA_UNDERLINE),
                new ControlCodeData((short) 0x192D, 2, CmdToken.Command.MAGENTA_UNDERLINE),
                new ControlCodeData((short) 0x112E, 1, CmdToken.Command.ITALICS),
                new ControlCodeData((short) 0x192E, 2, CmdToken.Command.ITALICS),
                new ControlCodeData((short) 0x112F, 1, CmdToken.Command.ITALICS_UNDERLINE),
                new ControlCodeData((short) 0x192F, 2, CmdToken.Command.ITALICS_UNDERLINE)
        );
    }

    public static Stream<ControlCodeData> controlCodesDataProvider() {
        return Stream.of(
                //--Table 52 Miscellaneous Control Codes
                // Rows 1 to 15, White
                new ControlCodeData((short) 0x1420, 1, CmdToken.Command.RCL),
                new ControlCodeData((short) 0x1C20, 2, CmdToken.Command.RCL),
                new ControlCodeData((short) 0x1421, 1, CmdToken.Command.BS),
                new ControlCodeData((short) 0x1C21, 2, CmdToken.Command.BS),
                new ControlCodeData((short) 0x1422, 1, CmdToken.Command.AOF),
                new ControlCodeData((short) 0x1C22, 2, CmdToken.Command.AOF),
                new ControlCodeData((short) 0x1423, 1, CmdToken.Command.AON),
                new ControlCodeData((short) 0x1C23, 2, CmdToken.Command.AON),
                new ControlCodeData((short) 0x1424, 1, CmdToken.Command.DER),
                new ControlCodeData((short) 0x1C24, 2, CmdToken.Command.DER),
                new ControlCodeData((short) 0x1425, 1, CmdToken.Command.RU2),
                new ControlCodeData((short) 0x1C25, 2, CmdToken.Command.RU2),
                new ControlCodeData((short) 0x1426, 1, CmdToken.Command.RU3),
                new ControlCodeData((short) 0x1C26, 2, CmdToken.Command.RU3),
                new ControlCodeData((short) 0x1427, 1, CmdToken.Command.RU4),
                new ControlCodeData((short) 0x1C27, 2, CmdToken.Command.RU4),
                new ControlCodeData((short) 0x1428, 1, CmdToken.Command.FON),
                new ControlCodeData((short) 0x1C28, 2, CmdToken.Command.FON),
                new ControlCodeData((short) 0x1429, 1, CmdToken.Command.RDC),
                new ControlCodeData((short) 0x1C29, 2, CmdToken.Command.RDC),
                new ControlCodeData((short) 0x142A, 1, CmdToken.Command.TR),
                new ControlCodeData((short) 0x1C2A, 2, CmdToken.Command.TR),
                new ControlCodeData((short) 0x142B, 1, CmdToken.Command.RTD),
                new ControlCodeData((short) 0x1C2B, 2, CmdToken.Command.RTD),
                new ControlCodeData((short) 0x142C, 1, CmdToken.Command.EDM),
                new ControlCodeData((short) 0x1C2C, 2, CmdToken.Command.EDM),
                new ControlCodeData((short) 0x142D, 1, CmdToken.Command.CR),
                new ControlCodeData((short) 0x1C2D, 2, CmdToken.Command.CR),
                new ControlCodeData((short) 0x142E, 1, CmdToken.Command.ENM),
                new ControlCodeData((short) 0x1C2E, 2, CmdToken.Command.ENM),
                new ControlCodeData((short) 0x142F, 1, CmdToken.Command.EOC),
                new ControlCodeData((short) 0x1C2F, 2, CmdToken.Command.EOC),
                new ControlCodeData((short) 0x1721, 1, CmdToken.Command.TO1),
                new ControlCodeData((short) 0x1F21, 2, CmdToken.Command.TO1),
                new ControlCodeData((short) 0x1722, 1, CmdToken.Command.TO2),
                new ControlCodeData((short) 0x1F22, 2, CmdToken.Command.TO2),
                new ControlCodeData((short) 0x1723, 1, CmdToken.Command.TO3),
                new ControlCodeData((short) 0x1F23, 2, CmdToken.Command.TO3)
        );
    }
}