/*
 * @(#)Cta608CmdToken.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CTA-608 Control command.
 *
 * <pre>
 *     control command
 *     15     12 11     8   7     4   3     0
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *     |P|0|0|1| |C|c|c|c| |P|0|c|c| |c|c|c|c|
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *
 *     bits   interpretation
 *     15     odd parity bit of bits 8-19 (shown as 'P')
 *     14-13  always 0 (shown as '00').
 *     12     always 1 (shown as '1').
 *     11     channel (shown as 'C')
 *     7      odd parity bit of bits 0-7 (shown as 'P')
 *     6      always 0 (shown as '0')
 *     10-8,5-0 control code (9 bits)
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Line 21 Data Services. ANSI/CTA-608-E S-2019. April 2008.</dt>
 *     <dd><a href="https://shop.cta.tech/products/line-21-data-services">ANSI-CTA-608-E-S-2019-Final.pdf</a></dd>
 * </dl>
 */
public final class CmdToken implements Token {
    private final short code;

    /**
     * Constructs a new instance.
     *
     * @param code a control code
     */
    public CmdToken(short code) {
        if ((code & 0b0111_0000_0100_0000) != 0b0001_0000_0000_0000)
            throw new IllegalArgumentException("code=" + Integer.toHexString(code));
        // clear parity bits
        this.code = Token.fixParityBits(code);
    }

    /**
     * Constructs a new instance.
     *
     * @param channel data channel in the range [1,2]
     * @param command a control code
     */
    public CmdToken(int channel, Command command) {
        if (channel < 1 || channel > 2) throw new IllegalArgumentException("channel=" + channel);
        if (command == null) throw new IllegalArgumentException("operation is null");
        int opCode = command.code;
        int low = opCode & 0b11_1111;
        int high = 0b0001_0000 | ((channel - 1) << 3) | ((opCode & 0b111_00_0000) >>> 6);
        this.code = Token.fixParityBits((short) ((high << 8) | low));
    }

    public int getChannel() {
        return ((code & 0b0000_1000_0000_0000) >> 11) + 1;
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

    public Command getOperation() {
        int high = code & 0b0111_0000_0000;
        int low = code & 0b0011_1111;
        int opCode = (high >>> 2) | low;
        return Command.valueOf(opCode);
    }

    /**
     * The sequence (ordinal) of this enum is not significant.
     */
    public enum Command {
        // Table 3 Background and Foreground Attribute Codes
        // Each Background Attribute Code incorporates an automatic backspace (BS) for backward compatibility with
        // standard decoders. The captioning service provider is expected to precede each Background Attribute Code
        // with a standard space.
        // Each Foreground Attribute Code incorporates an automatic BS for backward compatibility with standard
        // decoders. The captioning service provider shall precede each Foreground Attribute Code with a standard space.

        /**
         * Background White, Opaque.
         */
        BWO(0x20),

        /**
         * Background White, Semi-transparent.
         */
        BWS(0x21),

        /**
         * Background Green, Opaque.
         */
        BGO(0x22),

        /**
         * Background Green, Semi-transparent.
         */
        BGS(0x23),

        /**
         * Background Blue, Opaque.
         */
        BBO(0x24),

        /**
         * Background Blue, Semi-transparent.
         */
        BBS(0x25),

        /**
         * Background Cyan, Opaque.
         */
        BCO(0x26),

        /**
         * Background Cyan, Semi-transparent.
         */
        BCS(0x27),

        /**
         * Background Red, Opaque.
         */
        BRO(0x28),

        /**
         * Background Red, Semi-transparent.
         */
        BRS(0x29),

        /**
         * Background Yellow, Opaque.
         */
        BYO(0x2a),

        /**
         * Background Yellow, Semi-transparent.
         */
        BYS(0x2b),

        /**
         * Background Magenta, Opaque.
         */
        BMO(0x2c),

        /**
         * Background Magenta, Semi-transparent.
         */
        BMS(0x2d),

        /**
         * Background Black, Opaque.
         */
        BAO(0x2e),

        /**
         * Background Black, Semi-transparent.
         */
        BAS(0x2f),

        /**
         * Background Transparent.
         */
        BT(0x1ed),

        /**
         * Foreground Black.
         */
        FA(0x1ee),

        /**
         * Foreground Black Underline.
         */
        FAU(0x1ef),

        // Table 4 Special Assignments
        /**
         * Select the standard line 21 character set in normal size.
         */
        CHARSET_STANDARD(0x0),
        /**
         * Select the standard line 21 character set in double size.
         */
        CHARSET_STANDARD_DOUBLE_SIZE(0x0),
        /**
         * Select the first private character set.
         */
        CHARSET_PRIVATE_1(0x0),
        /**
         * Select the second private character set.
         */
        CHARSET_PRIVATE_2(0x0),
        /**
         * Select the People's Republic of China character set: GB 2312-80.
         */
        CHARSET_CHINESE(0x0),
        /**
         * Select the Korean Standard character set: KSC 5601-1987.
         */
        CHARSET_KOREAN(0x0),
        /**
         * Select the first registered character set.
         */
        CHARSET_REGISTERED_1(0x0),


        // Table 51 Mid-Row Codes
        /**
         * Foreground White.
         */
        WHITE(0x60),
        /**
         * Foreground White, underline.
         */
        WHITE_UNDERLINE(0x61),
        GREEN(0x62),
        GREEN_UNDERLINE(0x63),
        BLUE(0x64),
        BLUE_UNDERLINE(0x65),
        CYAN(0x66),
        CYAN_UNDERLINE(0x67),
        RED(0x68),
        RED_UNDERLINE(0x69),
        YELLOW(0x6a),
        YELLOW_UNDERLINE(0x6b),
        MAGENTA(0x6c),
        MAGENTA_UNDERLINE(0x6d),
        ITALICS(0x6e),
        ITALICS_UNDERLINE(0x6f),

        // Table 52 Miscellaneous Control Codes
        /**
         * Resume caption loading.
         */
        RCL(0x120),
        /**
         * Backspace
         */
        BS(0x121),
        /**
         * Reserved (formerly Alarm Off)
         */
        AOF(0x122),
        /**
         * Reserved (formerly Alarm On)
         */
        AON(0x123),
        /**
         * Delete to End of Row
         */
        DER(0x124),
        /**
         * Roll-Up Captions-2 Rows
         */
        RU2(0x125),
        /**
         * Roll-Up Captions-3 Rows
         */
        RU3(0x126),
        /**
         * Roll-Up Captions-4 Rows
         */
        RU4(0x127),
        /**
         * Flash On
         */
        FON(0x128),
        /**
         * Resume Direct Captioning
         */
        RDC(0x129),
        /**
         * Text Restart
         */
        TR(0x12a),
        /**
         * Resume Text Display
         */
        RTD(0x12b),
        /**
         * Erase Displayed Memory
         */
        EDM(0x12c),
        /**
         * Carriage Return
         */
        CR(0x12d),
        /**
         * Erase Non-Displayed Memory
         */
        ENM(0x12e),
        /**
         * End of Caption (Flip Memories)
         */
        EOC(0x12f),
        /**
         * Tab Offset 1 Column
         */
        TO1(0x1e1),
        /**
         * Tab Offset 2 Columns
         */
        TO2(0x1e2),
        /**
         * Tab Offset 3 Columns
         */
        TO3(0x1e3),

        ;
        private final int code;

        Command(int code) {
            this.code = code;
        }

        private static Map<Integer, Command> codeMap;

        public static Command valueOf(int opCode) {
            if (codeMap == null) {
                Map<Integer, Command> m = new LinkedHashMap<>();
                for (Command value : values()) {
                    m.put(value.code, value);
                }
                codeMap = m;
            }
            return codeMap.get(opCode);
        }
    }
}
