/*
 * @(#)FileMultiplexer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.imgseq;

import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.Multiplexer;
import org.monte.media.io.IOStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.monte.media.av.BufferFlag.DISCARD;

/**
 * Multiplexes samples into individual files.
 *
 * @author Werner Randelshofer
 */
public class FileMultiplexer implements Multiplexer {

    private final File dir;
    private final String baseName;
    private final String extension;
    private long position = 0;
    private final int minDigits = 4;
    private int trackCount = 0;

    public FileMultiplexer(File dir, String baseName, String extension) {
        this.dir = dir;
        this.baseName = baseName;
        this.extension = extension;
    }

    @Override
    public int addTrack(Format fmt) throws IOException {
        return trackCount++;
    }

    @Override
    public void setCodec(int trackIndex, Codec codec) {
        // do nothing
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        if (buf.isFlag(DISCARD)) {
            return;
        }

        File file = new File(dir, baseName + numToString(position + 1) + extension);

        if (buf.data instanceof byte[]) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write((byte[]) buf.data, buf.offset, buf.length);
            }
        } else if (buf.data instanceof File) {
            IOStreams.copy((File) buf.data, file);
        } else {
            throw new IllegalArgumentException("Can't process buffer data:" + buf.data);
        }

        position++;
    }

    private String numToString(long num) {
        StringBuilder b = new StringBuilder();
        b.append(Long.toString(num));
        while (b.length() < minDigits) {
            b.insert(0, '0');
        }
        return b.toString();
    }

    @Override
    public void close() throws IOException {
        //
    }
}
