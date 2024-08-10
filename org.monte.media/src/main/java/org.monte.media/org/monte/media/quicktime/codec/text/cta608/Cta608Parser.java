/*
 * @(#)Cta608Parser.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

import org.monte.media.io.UncachedImageInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a CTA-608 stream containing closed captions.
 * <p>
 * A CTA-608 screen has 32 columns and 15 rows.
 * <p>
 * ISO EBNF Grammar:
 * <pre>
 *     ClosedCaptions = Cta608Token , { Cta608Token } ;
 *     Cta608Token          = ( CmdToken | PacToken | CharsToken ) ;
 *     CmdToken       = uint16BE ; (* has constant bits: .001.....0...... *)
 *     PacToken       = uint16BE ; (* has constant bits: .001.....1...... *)
 *     CharsToken     = uint16BE ; (* has a value greater or equal 0x0200 *)
 * </pre>
 * The stream consists of a sequence of {@code uint16BE}.
 * <p>
 * Only the bits 0x7f7f contain data. The bits 0x8080 are parity bits.
 * <pre>
 *     CmdToken: Control command.
 *     15     12 11     8   7     4   3     0
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *     |P|0|0|1| |.|.|.|.| |P|0|.|.| |.|.|.|.|
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *
 *     PacToken: Preamble address code and tab offsets
 *     15     12 11     8   7     4   3     0
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *     |P|0|0|1| |.|.|.|.| |P|1|.|.| |.|.|.|.|
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *
 *     CharsToken: 1 or 2 text characters
 *     15     12 11     8   7     4   3     0
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 *     |P|.|.|.| |.|.|.|.| |P|.|.|.| |.|.|.|.|
 *     +-+-+-+-+ +-+-+-+-+ +-+-+-+-+ +-+-+-+-+
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Digital Television (DTV) Closed Captioning. CTA-708-E S-2023. August 2013.</dt>
 *     <dd><a href="https://shop.cta.tech/collections/standards/products/digital-television-dtv-closed-captioning">
 *         ANSI CTA-708-E S-2023 + Errata Letter and Replacement Pages FINAL.pdf</a></dd>
 * </dl>
 */
public class Cta608Parser {
    /**
     * Parses CTA-608 data until the end of the provided stream.
     * <p>
     */
    public List<Cta608Token> parse(InputStream in) throws IOException {
        ImageInputStream iis = in instanceof ImageInputStream s ? s : new UncachedImageInputStream(in);
        return parse(iis);
    }

    /**
     * Parses CTA-608 data until the end of the provided stream.
     * <p>
     */
    public List<Cta608Token> parse(ImageInputStream iis) throws IOException {
        long length = iis.length();
        List<Cta608Token> tokens = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (int b = iis.readUnsignedShort(); iis.getStreamPosition() < length - 1; b = iis.readUnsignedShort()) {
            b = b & 0x7f7f;// discard parity bits
            switch (b & 0b0111_0000_0100_0000) {
                case 0b0001_0000_0000_0000 -> {
                    drainTextBuffer(tokens, buf);
                    parseCommandCode(tokens, b);
                }
                case 0b0001_0000_0100_0000 -> {
                    drainTextBuffer(tokens, buf);
                    parsePreambleCode(tokens, b);
                }
                default -> parseCharacters(buf, b);
            }
        }
        drainTextBuffer(tokens, buf);
        return tokens;
    }

    private void parsePreambleCode(List<Cta608Token> buf, int b) throws IOException {
        buf.add(new PacToken((short) b));
    }

    private void parseCommandCode(List<Cta608Token> buf, int b) throws IOException {
        buf.add(new CmdToken((short) b));
    }

    private static void drainTextBuffer(List<Cta608Token> tokens, StringBuilder buf) {
        if (!buf.isEmpty()) {
            tokens.add(new TextToken(buf.toString()));
            buf.setLength(0);
        }
    }

    private void parseCharacters(StringBuilder buf, int b) throws IOException {
        // FIXME this code assumes ISO-8859-1 character set - use the proper character set instead
        buf.append((char) (b >>> 8));
        if ((b & 0xff) != 0) {
            buf.append((char) (b & 0xff));
        }
    }


    public String toStringWithOpCodes(List<Cta608Token> tokens) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (Cta608Token token : tokens) {
            if (!buf.isEmpty()) buf.append(' ');
            if (token instanceof TextToken) {
                buf.append('"');
                buf.append(((TextToken) token).getText().replaceAll("\"", "\\\""));
                buf.append('"');
            } else if (token instanceof CmdToken ct) {
                buf.append('{');
                buf.append(ct.getChannel());
                buf.append(':');
                if (ct.getOperation() == null) {
                    buf.append(Integer.toHexString(ct.getCodeWithoutParity()));
                } else {
                    buf.append(ct.getOperation());
                }
                buf.append('}');
            } else if (token instanceof PacToken ct) {
                buf.append('{');
                buf.append(ct.getChannel());
                buf.append(":R");
                buf.append(ct.getRow());
                buf.append(":");
                buf.append(ct.getTextAttributes());
                if (ct.isUnderline()) {
                    buf.append(":UL");
                }
                buf.append('}');
            }
        }
        return buf.toString();
    }

    /**
     * Parses to HTML that can be used in a Swing {@code JLabel}.
     *
     * @param tokens the tokens
     * @return the HTML String
     * @throws IOException on IO failure
     */
    public String toHtml(List<Cta608Token> tokens) throws IOException {
        StringBuilder buf = new StringBuilder("<html><font color=white>");
        int row = 0;
        String closingTag = "</font>";
        for (Cta608Token token : tokens) {
            if (token instanceof TextToken) {
                buf.append(
                        ((TextToken) token).getText()
                                .replaceAll("&", "&amp;")
                                .replaceAll("<", "&lt;")
                                .replaceAll(">", "&gt;")
                );
            } else if (token instanceof CmdToken ct) {
                switch (ct.getOperation()) {
                    case BWO -> {
                    }
                    case BWS -> {
                    }
                    case BGO -> {
                    }
                    case BGS -> {
                    }
                    case BBO -> {
                    }
                    case BBS -> {
                    }
                    case BCO -> {
                    }
                    case BCS -> {
                    }
                    case BRO -> {
                    }
                    case BRS -> {
                    }
                    case BYO -> {
                    }
                    case BYS -> {
                    }
                    case BMO -> {
                    }
                    case BMS -> {
                    }
                    case BAO -> {
                    }
                    case BAS -> {
                    }
                    case BT -> {
                    }
                    case FA -> {
                    }
                    case FAU -> {
                    }
                    case CHARSET_STANDARD -> {
                    }
                    case CHARSET_STANDARD_DOUBLE_SIZE -> {
                    }
                    case CHARSET_PRIVATE_1 -> {
                    }
                    case CHARSET_PRIVATE_2 -> {
                    }
                    case CHARSET_CHINESE -> {
                    }
                    case CHARSET_KOREAN -> {
                    }
                    case CHARSET_REGISTERED_1 -> {
                    }
                    case WHITE -> {
                        buf.append(closingTag);
                        buf.append("<font color=white>");
                        closingTag = "</font>";
                    }
                    case WHITE_UNDERLINE -> {
                        buf.append(closingTag);
                        buf.append("<font color=white><ul>");
                        closingTag = "<ul></font>";
                    }
                    case GREEN -> {
                        buf.append(closingTag);
                        buf.append("<font color=green>");
                        closingTag = "</font>";
                    }
                    case GREEN_UNDERLINE -> {
                    }
                    case BLUE -> {
                        buf.append(closingTag);
                        buf.append("<font color=blue>");
                        closingTag = "</font>";
                    }
                    case BLUE_UNDERLINE -> {
                    }
                    case CYAN -> {
                        buf.append(closingTag);
                        buf.append("<font color=cyan>");
                        closingTag = "</font>";
                    }
                    case CYAN_UNDERLINE -> {
                    }
                    case RED -> {
                        buf.append(closingTag);
                        buf.append("<font color=red>");
                        closingTag = "</font>";
                    }
                    case RED_UNDERLINE -> {
                    }
                    case YELLOW -> {
                        buf.append(closingTag);
                        buf.append("<font color=yellow>");
                        closingTag = "</font>";
                    }
                    case YELLOW_UNDERLINE -> {
                    }
                    case MAGENTA -> {
                        buf.append(closingTag);
                        buf.append("<font color=magenta>");
                        closingTag = "</font>";
                    }
                    case MAGENTA_UNDERLINE -> {
                    }
                    case ITALICS -> {
                        buf.append("<i>");
                        closingTag = "</i>" + closingTag;
                    }
                    case ITALICS_UNDERLINE -> {
                        buf.append("<i><u>");
                        closingTag = "</i></u>" + closingTag;
                    }
                    case RCL -> {
                        buf.setLength(0);
                        buf.append("<html>");
                        row = 0;
                        closingTag = "";
                    }
                    case BS -> {
                    }
                    case AOF -> {
                    }
                    case AON -> {
                    }
                    case DER -> {
                    }
                    case RU2 -> {
                    }
                    case RU3 -> {
                    }
                    case RU4 -> {
                    }
                    case FON -> {
                    }
                    case RDC -> {
                    }
                    case TR -> {
                    }
                    case RTD -> {
                    }
                    case EDM -> {
                    }
                    case CR -> {
                    }
                    case ENM -> {
                    }
                    case EOC -> {
                    }
                    case TO1 -> {
                    }
                    case TO2 -> {
                    }
                    case TO3 -> {
                    }
                }
            } else if (token instanceof PacToken ct) {
                int targetRow = ct.getRow();
                buf.append(closingTag);
                closingTag = "";
                while (row < targetRow) {
                    buf.append("<br>");
                    row++;
                }
                switch (ct.getTextAttributes()) {
                    case WHITE -> {
                        buf.append("<font color=white>");
                        closingTag = "</font>";
                    }
                    case GREEN -> {
                        buf.append("<font color=green>");
                        closingTag = "</font>";
                    }
                    case BLUE -> {
                        buf.append("<font color=blue>");
                        closingTag = "</font>";
                    }
                    case CYAN -> {
                        buf.append("<font color=cyan>");
                        closingTag = "</font>";
                    }
                    case RED -> {
                        buf.append("<font color=red>");
                        closingTag = "</font>";
                    }
                    case YELLOW -> {
                        buf.append("<font color=yellow>");
                        closingTag = "</font>";
                    }
                    case MAGENTA -> {
                        buf.append("<font color=magenta>");
                        closingTag = "</font>";
                    }
                    case WHITE_ITALICS -> {
                        buf.append("<font color=white><i>");
                        closingTag = "</i></font>";
                    }
                    case INDENT_0 -> {
                        buf.append("<font color=white>");
                        closingTag = "</font>";
                    }
                    case INDENT_4 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 4; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_8 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 8; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_12 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 12; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_16 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 16; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_20 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 20; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_24 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 24; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                    case INDENT_28 -> {
                        buf.append("<font color=white>");
                        for (int i = 0; i < 28; i++) {
                            buf.append("&nbsp;");
                        }
                        closingTag = "</font>";
                    }
                }
                if (ct.isUnderline()) {
                    buf.append("<ul>");
                    closingTag = "</ul>" + closingTag;
                }
            }
        }
        buf.append(closingTag);
        return buf.toString();
    }

    /**
     * Parses to String that can be used in a Swing {@code JTextArea}.
     * <p>
     * This format will lose most of the formatting.
     *
     * @param tokens the tokens
     * @return the String
     * @throws IOException on IO failure
     */
    public String toString(List<Cta608Token> tokens) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (Cta608Token token : tokens) {
            if (token instanceof TextToken) {
                buf.append(((TextToken) token).getText());
            } else if (token instanceof CmdToken ct) {
                switch (ct.getOperation()) {
                    case BWO -> {
                    }
                    case BWS -> {
                    }
                    case BGO -> {
                    }
                    case BGS -> {
                    }
                    case BBO -> {
                    }
                    case BBS -> {
                    }
                    case BCO -> {
                    }
                    case BCS -> {
                    }
                    case BRO -> {
                    }
                    case BRS -> {
                    }
                    case BYO -> {
                    }
                    case BYS -> {
                    }
                    case BMO -> {
                    }
                    case BMS -> {
                    }
                    case BAO -> {
                    }
                    case BAS -> {
                    }
                    case BT -> {
                    }
                    case FA -> {
                    }
                    case FAU -> {
                    }
                    case CHARSET_STANDARD -> {
                    }
                    case CHARSET_STANDARD_DOUBLE_SIZE -> {
                    }
                    case CHARSET_PRIVATE_1 -> {
                    }
                    case CHARSET_PRIVATE_2 -> {
                    }
                    case CHARSET_CHINESE -> {
                    }
                    case CHARSET_KOREAN -> {
                    }
                    case CHARSET_REGISTERED_1 -> {
                    }
                    case WHITE -> {
                    }
                    case WHITE_UNDERLINE -> {
                    }
                    case GREEN -> {
                    }
                    case GREEN_UNDERLINE -> {
                    }
                    case BLUE -> {
                    }
                    case BLUE_UNDERLINE -> {
                    }
                    case CYAN -> {
                    }
                    case CYAN_UNDERLINE -> {
                    }
                    case RED -> {
                    }
                    case RED_UNDERLINE -> {
                    }
                    case YELLOW -> {
                    }
                    case YELLOW_UNDERLINE -> {
                    }
                    case MAGENTA -> {
                    }
                    case MAGENTA_UNDERLINE -> {
                    }
                    case ITALICS -> {
                    }
                    case ITALICS_UNDERLINE -> {
                    }
                    case RCL -> {
                        buf.setLength(0);
                    }
                    case BS -> {
                    }
                    case AOF -> {
                    }
                    case AON -> {
                    }
                    case DER -> {
                    }
                    case RU2 -> {
                    }
                    case RU3 -> {
                    }
                    case RU4 -> {
                    }
                    case FON -> {
                    }
                    case RDC -> {
                    }
                    case TR -> {
                    }
                    case RTD -> {
                    }
                    case EDM -> {
                    }
                    case CR -> {
                    }
                    case ENM -> {
                    }
                    case EOC -> {
                    }
                    case TO1 -> {
                    }
                    case TO2 -> {
                    }
                    case TO3 -> {
                    }
                }
            } else if (token instanceof PacToken ct) {
                buf.append('{');
                buf.append(ct.getChannel());
                buf.append(":R");
                buf.append(ct.getRow());
                buf.append(":");
                buf.append(ct.getTextAttributes());
                if (ct.isUnderline()) {
                    buf.append(":UL");
                }
                buf.append('}');
            }
        }
        return buf.toString();
    }

}
