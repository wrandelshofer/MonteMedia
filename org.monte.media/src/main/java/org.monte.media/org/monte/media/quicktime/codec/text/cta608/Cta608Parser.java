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
 * ISO EBNF Grammar:
 * <pre>
 *     ClosedCaptions = Token , { Token } ;
 *     Token          = ( CmdToken | PacToken | CharsToken ) ;
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
    public List<Token> parse(InputStream in) throws IOException {
        ImageInputStream iis = in instanceof ImageInputStream s ? s : new UncachedImageInputStream(in);
        return parse(iis);
    }

    /**
     * Parses CTA-608 data until the end of the provided stream.
     * <p>
     */
    public List<Token> parse(ImageInputStream iis) throws IOException {
        long length = iis.length();
        List<Token> tokens = new ArrayList<>();
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

    private void parsePreambleCode(List<Token> buf, int b) throws IOException {
        buf.add(new PacToken((short) b));
    }

    private void parseCommandCode(List<Token> buf, int b) throws IOException {
        buf.add(new CmdToken((short) b));
    }

    private static void drainTextBuffer(List<Token> tokens, StringBuilder buf) {
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

    public String parseToStringWithOpCodes(InputStream in) throws IOException {
        ImageInputStream iis = in instanceof ImageInputStream s ? s : new UncachedImageInputStream(in);
        return parseToStringWithOpCodes(iis);
    }

    public String parseToString(InputStream in) throws IOException {
        ImageInputStream iis = in instanceof ImageInputStream s ? s : new UncachedImageInputStream(in);
        return parseToString(iis);
    }

    private String parseToString(ImageInputStream iis) throws IOException {
        List<Token> tokens = new Cta608Parser().parse(iis);
        StringBuilder buf = new StringBuilder();
        for (Token token : tokens) {
            if (token instanceof TextToken) {
                buf.append(((TextToken) token).getText());
            }
        }
        return buf.toString();
    }

    public String parseToStringWithOpCodes(ImageInputStream iis) throws IOException {
        List<Token> tokens = new Cta608Parser().parse(iis);
        StringBuilder buf = new StringBuilder();
        for (Token token : tokens) {
            if (buf.length() > 0) buf.append(' ');
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

}
