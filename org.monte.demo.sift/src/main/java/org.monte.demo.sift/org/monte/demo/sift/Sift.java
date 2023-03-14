/*
 * @(#)Sift.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.sift;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;
import org.monte.media.iff.IFFChunk;
import org.monte.media.iff.IFFParser;
import org.monte.media.iff.IFFVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Takes any IFF file and tells you what's in it.
 */
public class Sift {
    private boolean hexdump = false;

    public boolean isHexdump() {
        return hexdump;
    }

    public void setHexdump(boolean hexdump) {
        this.hexdump = hexdump;
    }

    public void sift(Path path) throws IOException, ParseException, AbortException {
        class MyVisitor implements IFFVisitor {
            private int index = 0;
            private int depth = 0;
            private int maxAddressDigits = 1;
            private final static String hex = "0123456789abcdef";

            @Override
            public void enterGroup(IFFChunk group) throws ParseException, AbortException {
                System.out.println(
                        ".".repeat(depth + 1)
                                + IFFParser.idToString(group.getID())
                                + " " + group.getSize()
                                + " " + IFFParser.idToString(group.getType())
                                + " @" + group.getScan()
                                + " " + index + "."
                );
                if (depth == 0) {
                    maxAddressDigits = Long.toHexString(group.getSize() + 12).length();
                }
                depth++;
                index++;
            }

            @Override
            public void leaveGroup(IFFChunk group) throws ParseException, AbortException {
                depth--;
            }

            @Override
            public void visitChunk(IFFChunk group, IFFChunk chunk) throws ParseException, AbortException {
                System.out.println(
                        ".".repeat(depth + 1)
                                + IFFParser.idToString(chunk.getID())
                                + " " + chunk.getSize()
                                + " " + IFFParser.idToString(chunk.getType())
                                + " @" + chunk.getScan()
                                + " " + index + "."
                );
                if (hexdump) {
                    printHexdump(chunk);
                }
                index++;
            }

            private void printHexdump(IFFChunk chunk) {
                byte[] data = chunk.getData();
                long scan = chunk.getScan();
                StringBuilder buf = new StringBuilder();
                for (int y = 0; y < data.length; y += 16) {
                    buf.setLength(0);
                    String address = Long.toHexString(scan + y);
                    for (int i = 0; i + address.length() < maxAddressDigits; i++) {
                        buf.append('0');
                    }
                    buf.append(address);
                    buf.append(": ");

                    for (int x = 0; x < 16 && x + y < data.length; x++) {
                        byte b = data[x + y];
                        buf.append(hex.charAt((b & 0xf0) >>> 4));
                        buf.append(hex.charAt(b & 0xf));
                        buf.append(' ');
                    }
                    while (buf.length() < maxAddressDigits + 51) {
                        buf.append(' ');
                    }
                    for (int x = 0; x < 16 && x + y < data.length; x++) {
                        char c = (char) (data[x + y] & 0xff);
                        buf.append((Character.isISOControl(c) ? '.' : c));
                    }
                    System.out.println(buf);
                }
            }
        }
        IFFVisitor visitor = new MyVisitor();
        try (InputStream in = Files.newInputStream(path)) {
            new IFFParser().parse(in, visitor);
        }
    }
}
