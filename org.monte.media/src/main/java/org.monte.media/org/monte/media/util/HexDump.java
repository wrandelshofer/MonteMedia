/*
 * @(#)Hexdump.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

import java.io.IOException;

public class HexDump {
    private final static String hex = "0123456789abcdef";

    public String formatHex(byte[] data, int offset, int length) {
        var buf = new StringBuilder();
        try {
            formatHex(buf, data, offset, length, 0, (int) Math.ceil(Math.log10(length)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buf.toString();
    }

    public void formatHex(Appendable out, byte[] data, int offset, int length) throws IOException {
        formatHex(out, data, offset, length, 0, (int) Math.ceil(Math.log10(length)));
    }

    public void formatHex(Appendable out, byte[] data, int offset, int length, long address, int maxAddressDigits) throws IOException {
        StringBuilder buf = new StringBuilder();
        //int maxAddressDigits=(int)Math.ceil(Math.log10(address+length));
        for (int y = 0; y < length; y += 16) {
            buf.setLength(0);
            String addressStr = Long.toHexString(address + y);
            for (int i = 0; i + addressStr.length() < maxAddressDigits; i++) {
                buf.append('0');
            }
            buf.append(addressStr);
            buf.append(':');

            int x = 0;
            for (; x < 16 && x + y < length; x++) {
                if ((x & 0b11) == 0) {
                    buf.append(' ');
                }
                byte b = data[x + y + offset];
                buf.append(hex.charAt((b & 0xf0) >>> 4));
                buf.append(hex.charAt(b & 0xf));
            }
            for (; x < 16; x++) {
                if ((x & 0b11) == 0) {
                    buf.append(' ');
                }
                buf.append("  ");
            }
            buf.append("  ");
            for (x = 0; x < 16 && x + y < length; x++) {
                if ((x & 0b11) == 0) {
                    buf.append(' ');
                }
                char c = (char) (data[x + y] & 0xff);
                buf.append((Character.isISOControl(c) ? '.' : c));
            }
            buf.append('\n');
            out.append(buf);
        }

    }
}
