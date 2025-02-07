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
import org.monte.media.util.HexDump;

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
                try {
                    new HexDump().formatHex(System.out, data, 0, (int) chunk.getSize(), scan, maxAddressDigits);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        IFFVisitor visitor = new MyVisitor();
        try (InputStream in = Files.newInputStream(path)) {
            new IFFParser().parse(in, visitor);
        }
    }


}
