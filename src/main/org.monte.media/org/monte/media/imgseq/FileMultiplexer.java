/* @(#)FileMultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.imgseq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.monte.media.av.Buffer;
import static org.monte.media.av.BufferFlag.DISCARD;
import org.monte.media.av.Multiplexer;
import org.monte.media.io.IOStreams;

/**
 * Multiplexes samples into individual files.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class FileMultiplexer implements Multiplexer {

    private File dir;
    private String baseName;
    private String extension;
    private long position = 0;
    private int minDigits = 4;

    public FileMultiplexer(File dir, String baseName, String extension) {
        this.dir = dir;
        this.baseName = baseName;
        this.extension = extension;
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
            IOStreams.copy((File)buf.data, file);
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
