/*
 * @(#)Cta608Parser.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

import org.monte.media.io.UncachedImageInputStream;

import javax.imageio.stream.ImageInputStream;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        ImageInputStream iis = in instanceof ImageInputStream ? (ImageInputStream) in : new UncachedImageInputStream(in);
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
                TextToken tx = (TextToken) token;
                buf.append('"');
                buf.append(tx.getText().replaceAll("\"", "\\\""));
                buf.append('"');
            } else if (token instanceof CmdToken) {
                CmdToken ct = (CmdToken) token;
                buf.append("{Cmd:C");
                buf.append(ct.getChannel());
                buf.append(':');
                if (ct.getOperation() == null) {
                    buf.append(Integer.toHexString(ct.getCodeWithoutParity()));
                } else {
                    buf.append(ct.getOperation());
                }
                buf.append('}');
            } else if (token instanceof PacToken) {
                PacToken ct = (PacToken) token;
                buf.append("{Pac:C");
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

    private static final Map<Cta608Color, String> colorHtmldMap = Map.ofEntries(
            Map.entry(Cta608Color.WHITE, "white"),
            Map.entry(Cta608Color.WHITE_SEMI, "rgba(255,255,255, 0.5)"),
            Map.entry(Cta608Color.GREEN, "green"),
            Map.entry(Cta608Color.GREEN_SEMI, "rgba(0,255,0, 0.5)"),
            Map.entry(Cta608Color.BLUE, "blue"),
            Map.entry(Cta608Color.BLUE_SEMI, "rgba(0,0,255, 0.5)"),
            Map.entry(Cta608Color.CYAN, "cyan"),
            Map.entry(Cta608Color.CYAN_SEMI, "rgba(0,255,255, 0.5)"),
            Map.entry(Cta608Color.RED, "red"),
            Map.entry(Cta608Color.RED_SEMI, "rgba(255,0,0, 0.5)"),
            Map.entry(Cta608Color.YELLOW, "yellow"),
            Map.entry(Cta608Color.YELLOW_SEMI, "rgba(255,255,0, 0.5)"),
            Map.entry(Cta608Color.MAGENTA, "magenta"),
            Map.entry(Cta608Color.MAGENTA_SEMI, "rgba(255,0,255, 0.5)"),
            Map.entry(Cta608Color.BLACK, "black"),
            Map.entry(Cta608Color.BLACK_SEMI, "rgba(0,0,0, 0.5)"),
            Map.entry(Cta608Color.TRANSPARENT, "rgba(0,0,0, 0.0)")
    );

    private static final Map<CmdToken.Command, Cta608Color> bgCommandMap = Map.ofEntries(
            Map.entry(CmdToken.Command.BWO, Cta608Color.WHITE),
            Map.entry(CmdToken.Command.BWS, Cta608Color.WHITE_SEMI),
            Map.entry(CmdToken.Command.BGO, Cta608Color.GREEN),
            Map.entry(CmdToken.Command.BGS, Cta608Color.GREEN_SEMI),
            Map.entry(CmdToken.Command.BBO, Cta608Color.BLUE),
            Map.entry(CmdToken.Command.BBS, Cta608Color.BLUE_SEMI),
            Map.entry(CmdToken.Command.BCO, Cta608Color.CYAN),
            Map.entry(CmdToken.Command.BCS, Cta608Color.CYAN_SEMI),
            Map.entry(CmdToken.Command.BRO, Cta608Color.RED),
            Map.entry(CmdToken.Command.BRS, Cta608Color.RED_SEMI),
            Map.entry(CmdToken.Command.BYO, Cta608Color.YELLOW),
            Map.entry(CmdToken.Command.BYS, Cta608Color.YELLOW_SEMI),
            Map.entry(CmdToken.Command.BMO, Cta608Color.MAGENTA),
            Map.entry(CmdToken.Command.BMS, Cta608Color.MAGENTA_SEMI),
            Map.entry(CmdToken.Command.BAO, Cta608Color.BLACK),
            Map.entry(CmdToken.Command.BAS, Cta608Color.BLACK_SEMI),
            Map.entry(CmdToken.Command.BT, Cta608Color.TRANSPARENT)
    );
    private static final Map<CmdToken.Command, Cta608Color> fgCommandMap = Map.ofEntries(
            Map.entry(CmdToken.Command.WHITE, Cta608Color.WHITE),
            Map.entry(CmdToken.Command.GREEN, Cta608Color.GREEN),
            Map.entry(CmdToken.Command.BLUE, Cta608Color.BLUE),
            Map.entry(CmdToken.Command.CYAN, Cta608Color.CYAN),
            Map.entry(CmdToken.Command.RED, Cta608Color.RED),
            Map.entry(CmdToken.Command.YELLOW, Cta608Color.YELLOW),
            Map.entry(CmdToken.Command.MAGENTA, Cta608Color.MAGENTA),
            Map.entry(CmdToken.Command.WHITE_UNDERLINE, Cta608Color.WHITE),
            Map.entry(CmdToken.Command.GREEN_UNDERLINE, Cta608Color.GREEN),
            Map.entry(CmdToken.Command.BLUE_UNDERLINE, Cta608Color.BLUE),
            Map.entry(CmdToken.Command.CYAN_UNDERLINE, Cta608Color.CYAN),
            Map.entry(CmdToken.Command.RED_UNDERLINE, Cta608Color.RED),
            Map.entry(CmdToken.Command.YELLOW_UNDERLINE, Cta608Color.YELLOW),
            Map.entry(CmdToken.Command.MAGENTA_UNDERLINE, Cta608Color.MAGENTA),
            Map.entry(CmdToken.Command.FA, Cta608Color.BLACK),
            Map.entry(CmdToken.Command.FAU, Cta608Color.BLACK)
    );
    private static final Map<PacToken.Attributes, Cta608Color> pacFgCommandMap = Map.ofEntries(
            Map.entry(PacToken.Attributes.WHITE, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.GREEN, Cta608Color.GREEN),
            Map.entry(PacToken.Attributes.BLUE, Cta608Color.BLUE),
            Map.entry(PacToken.Attributes.CYAN, Cta608Color.CYAN),
            Map.entry(PacToken.Attributes.RED, Cta608Color.RED),
            Map.entry(PacToken.Attributes.YELLOW, Cta608Color.YELLOW),
            Map.entry(PacToken.Attributes.MAGENTA, Cta608Color.MAGENTA),
            Map.entry(PacToken.Attributes.WHITE_ITALICS, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_0, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_8, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_12, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_16, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_20, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_24, Cta608Color.WHITE),
            Map.entry(PacToken.Attributes.INDENT_28, Cta608Color.WHITE)
    );
    private static final Map<PacToken.Attributes, Integer> pacIndentCommandMap = Map.ofEntries(
            Map.entry(PacToken.Attributes.INDENT_0, 0),
            Map.entry(PacToken.Attributes.INDENT_8, 8),
            Map.entry(PacToken.Attributes.INDENT_12, 12),
            Map.entry(PacToken.Attributes.INDENT_16, 16),
            Map.entry(PacToken.Attributes.INDENT_20, 20),
            Map.entry(PacToken.Attributes.INDENT_24, 24),
            Map.entry(PacToken.Attributes.INDENT_28, 28)
    );
    private static final Map<PacToken.Attributes, Boolean> pacItalicsCommandMap = Map.ofEntries(
            Map.entry(PacToken.Attributes.WHITE_ITALICS, true)
    );
    private static final Map<CmdToken.Command, Boolean> underlineCommandMap = Map.ofEntries(
            Map.entry(CmdToken.Command.WHITE, false),
            Map.entry(CmdToken.Command.GREEN, false),
            Map.entry(CmdToken.Command.BLUE, false),
            Map.entry(CmdToken.Command.CYAN, false),
            Map.entry(CmdToken.Command.RED, false),
            Map.entry(CmdToken.Command.YELLOW, false),
            Map.entry(CmdToken.Command.MAGENTA, false),
            Map.entry(CmdToken.Command.WHITE_UNDERLINE, true),
            Map.entry(CmdToken.Command.GREEN_UNDERLINE, true),
            Map.entry(CmdToken.Command.BLUE_UNDERLINE, true),
            Map.entry(CmdToken.Command.CYAN_UNDERLINE, true),
            Map.entry(CmdToken.Command.RED_UNDERLINE, true),
            Map.entry(CmdToken.Command.YELLOW_UNDERLINE, true),
            Map.entry(CmdToken.Command.MAGENTA_UNDERLINE, true),
            Map.entry(CmdToken.Command.FA, false),
            Map.entry(CmdToken.Command.FAU, true),
            Map.entry(CmdToken.Command.ITALICS, false),
            Map.entry(CmdToken.Command.ITALICS_UNDERLINE, true)
    );
    private static final Map<CmdToken.Command, Boolean> italicsCommandMap = Map.ofEntries(
            Map.entry(CmdToken.Command.WHITE, false),
            Map.entry(CmdToken.Command.GREEN, false),
            Map.entry(CmdToken.Command.BLUE, false),
            Map.entry(CmdToken.Command.CYAN, false),
            Map.entry(CmdToken.Command.RED, false),
            Map.entry(CmdToken.Command.YELLOW, false),
            Map.entry(CmdToken.Command.MAGENTA, false),
            Map.entry(CmdToken.Command.WHITE_UNDERLINE, false),
            Map.entry(CmdToken.Command.GREEN_UNDERLINE, false),
            Map.entry(CmdToken.Command.BLUE_UNDERLINE, false),
            Map.entry(CmdToken.Command.CYAN_UNDERLINE, false),
            Map.entry(CmdToken.Command.RED_UNDERLINE, false),
            Map.entry(CmdToken.Command.YELLOW_UNDERLINE, false),
            Map.entry(CmdToken.Command.MAGENTA_UNDERLINE, false),
            Map.entry(CmdToken.Command.FA, false),
            Map.entry(CmdToken.Command.FAU, false),
            Map.entry(CmdToken.Command.ITALICS, true),
            Map.entry(CmdToken.Command.ITALICS_UNDERLINE, true)
    );


    /**
     * Parses to HTML that can be used in a Swing {@code JLabel}.
     *
     * @param tokens the tokens
     * @param memory
     * @return the HTML String
     * @throws IOException on IO failure
     */
    public void updateMemory(List<Cta608Token> tokens, Cta608Memory memory) throws IOException {
        Point pos = new Point(0, 0);
        Cta608CharAttr attr = Cta608Screen.DEFAULT_ATTR;
        for (Cta608Token token : tokens) {
            if (Objects.requireNonNull(token) instanceof TextToken) {
                TextToken tx = (TextToken) Objects.requireNonNull(token);
                pos = memory.nonDisplayed.write(pos, attr, tx.getText());
            } else if (token instanceof CmdToken) {
                CmdToken cmd = (CmdToken) token;
                CmdToken.Command op = cmd.getOperation();
                attr = attr.withBackground(bgCommandMap.get(op));
                attr = attr.withForeground(fgCommandMap.get(op));
                attr = attr.withUnderline(underlineCommandMap.get(op));
                attr = attr.withItalics(italicsCommandMap.get(op));

                switch (op) {
                    case RCL -> {
                        memory.nonDisplayed.style = Cta608Style.POP_ON;
                    }
                    case BS -> {
                        pos.x = Math.max(pos.x - 1, 0);
                        memory.nonDisplayed.write(pos, attr, "\0");
                    }
                    case DER -> {
                        memory.nonDisplayed.deleteToEndOfRow(pos);
                    }
                    case RU2 -> {
                        memory.nonDisplayed.rollUp(2);
                    }
                    case RU3 -> {
                        memory.nonDisplayed.rollUp(3);
                    }
                    case RU4 -> {
                        memory.nonDisplayed.rollUp(4);
                    }
                    case RDC -> {
                        memory.nonDisplayed.style = Cta608Style.PAINT_ON;
                    }
                    case TR -> {
                        memory.nonDisplayed.textRestart();
                    }
                    case RTD -> {
                        pos.x = 0;
                        pos.y = 0;
                    }
                    case EDM -> {
                        memory.displayed.erase();
                    }
                    case CR -> {
                        pos.x = 0;
                        if (pos.y == Cta608Screen.HEIGHT - 1) {
                            memory.nonDisplayed.rollUp(1);
                        } else {
                            pos.y = pos.y + 1;
                        }
                    }
                    case ENM -> {
                        memory.nonDisplayed.erase();
                    }
                    case EOC -> {
                        memory.flipMemories();
                    }
                    case TO1 -> {
                        pos.x = Math.min(Cta608Screen.WIDTH - 1, pos.x + 1);
                    }
                    case TO2 -> {
                        pos.x = Math.min(Cta608Screen.WIDTH - 1, pos.x + 2);
                    }
                    case TO3 -> {
                        pos.x = Math.min(Cta608Screen.WIDTH - 1, pos.x + 3);
                    }
                }
            } else if (token instanceof PacToken) {
                PacToken pac = (PacToken) token;
                pos.y = pac.getRow() - 1;
                PacToken.Attributes ta = pac.getTextAttributes();
                attr = attr.withForeground(pacFgCommandMap.get(ta));
                attr = attr.withItalics(pacItalicsCommandMap.get(ta));
                Integer indent = pacIndentCommandMap.get(ta);
                if (indent != null) {
                    pos.x = indent;
                }
            }
        }
    }

    /**
     * Parses to HTML that can be used in a Swing {@code JLabel}.
     *
     * @param memory the memory
     * @return the HTML String
     * @throws IOException on IO failure
     */
    public String toHtml(Cta608Memory memory) throws IOException {
        StringBuilder buf = new StringBuilder("<html>");
        Cta608Screen screen = memory.displayed;
        boolean isPopOn = screen.style == Cta608Style.POP_ON;
        Rectangle textBox = isPopOn ? screen.getTextBox() : null;
        for (int y = 0; y < Cta608Screen.HEIGHT; y++) {
            int minX = screen.getMinX(y);
            int indent;
            if (textBox != null && y == textBox.y) {
                for (int i = 0; i < textBox.x; i++) {
                    buf.append("&nbsp");
                }
                buf.append("<div style=\"");
                attrToCssStyle(buf, screen.attrAt(textBox.x, textBox.y), false);
                buf.append("padding:4 8 4 8;");
                buf.append("\">");
                indent = minX - textBox.x;
            } else {
                indent = minX;
            }
            if (minX < Cta608Screen.WIDTH) {
                for (int i = 0; i < indent; i++) {
                    buf.append("&nbsp");
                }
                int maxX = screen.getMaxX(y);
                Cta608CharAttr prevAttr = null;
                for (int x = minX; x <= maxX; x++) {
                    Cta608CharAttr attr = screen.attrAt(x, y);
                    if (!Objects.equals(prevAttr, attr)) {
                        if (prevAttr != null) {
                            buf.append("</span>");
                        }
                        buf.append("<span style=\"");
                        attrToCssStyle(buf, attr, isPopOn);
                        buf.append("\">");
                    }
                    char c = screen.charAt(x, y);
                    switch (c) {
                        case ' ', '0' -> buf.append("&nbsp;");
                        case '<' -> buf.append("&lt;");
                        case '>' -> buf.append("&gt;");
                        case '&' -> buf.append("&amp;");
                        default -> buf.append(c);

                    }
                    prevAttr = attr;
                }
                if (prevAttr != null) {
                    buf.append("</span>");
                }
            }
            if (textBox != null && y == textBox.y + textBox.height - 1) {
                buf.append("</div>");
            }
            buf.append("<br>");
        }
        return buf.toString();
    }

    private static void attrToCssStyle(StringBuilder buf, Cta608CharAttr attr, boolean ignoreBackground) {
        if (!ignoreBackground) {
            buf.append("background-color:");
            buf.append(colorHtmldMap.get(attr.background()));
            buf.append(';');
        }
        buf.append("color:");
        buf.append(colorHtmldMap.get(attr.foreground()));
        buf.append(";font-style:");
        buf.append(attr.italic() ? "italic;" : "normal;");
        buf.append("text-decoration:");
        buf.append(attr.underlined() ? "underline;" : "none;");
    }

    /**
     * Parses to String that can be used in a Swing {@code JTextArea}.
     * <p>
     * This format will lose most of the formatting.
     *
     * @param memory the memory
     * @return the String
     * @throws IOException on IO failure
     */
    public String toString(Cta608Memory memory) throws IOException {
        StringBuilder buf = new StringBuilder("");
        Cta608Screen screen = memory.displayed;
        for (int y = 0; y < Cta608Screen.HEIGHT; y++) {
            int minX = screen.getMinX(y);
            if (minX < Cta608Screen.WIDTH) {
                for (int i = 0; i < minX; i++) {
                    buf.append(' ');
                }
                int maxX = screen.getMaxX(y);
                for (int x = minX; x <= maxX; x++) {
                    char c = screen.charAt(x, y);
                    switch (c) {
                        case '0' -> buf.append(' ');
                        default -> buf.append(c);
                    }
                }
            }
            buf.append("\n");
        }
        return buf.toString();
    }

}
