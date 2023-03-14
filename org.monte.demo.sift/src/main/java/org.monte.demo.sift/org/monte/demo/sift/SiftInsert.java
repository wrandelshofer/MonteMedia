/*
 * @(#)SiftInsert.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.sift;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;
import org.monte.media.iff.IFFChunk;
import org.monte.media.iff.IFFOutputStream;
import org.monte.media.iff.IFFParser;
import org.monte.media.iff.IFFVisitor;

import javax.imageio.stream.FileImageOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Takes any IFF file and extracts the body of the i-th chunk.
 */
public class SiftInsert {
    public void insert(Path inputPath, Path outputPath,
                       int parentIndex,
                       int childIndex,
                       String chunkId,
                       Path insertionPath) throws IOException, ParseException, AbortException {
        class MyVisitor implements IFFVisitor {
            private int index = 0;
            private int depth = 0;

            private final IFFOutputStream out;

            public MyVisitor(IFFOutputStream out) {
                this.out = out;
            }

            @Override
            public void enterGroup(IFFChunk group) throws ParseException, AbortException {
                try {
                    out.pushCompositeChunk(group.getID(), group.getType());
                } catch (IOException e) {
                    throw new ParseException("IOException", e);
                }
                index++;
                depth++;
            }

            @Override
            public void leaveGroup(IFFChunk group) throws ParseException, AbortException {
                try {
                    out.popChunk();
                } catch (IOException e) {
                    throw new ParseException("IOException", e);
                }
                depth--;
            }

            @Override
            public void visitChunk(IFFChunk group, IFFChunk chunk) throws ParseException, AbortException {
                try {
                    out.pushDataChunk(IFFParser.idToString(chunk.getID()));
                    out.write(chunk.getData());
                    out.popChunk();
                } catch (IOException e) {
                    throw new ParseException("IOException", e);
                }

                index++;
            }


        }
        try (RandomAccessFile rw = new RandomAccessFile(outputPath.toFile(), "rw");
             IFFOutputStream out = new IFFOutputStream(
                     new FileImageOutputStream(rw));
             InputStream in = Files.newInputStream(inputPath)) {
            rw.setLength(0L);
            IFFVisitor visitor = new MyVisitor(out);
            new IFFParser().parse(in, visitor);
        }
    }
}
