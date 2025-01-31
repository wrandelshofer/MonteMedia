/*
 * @(#)DataAtom.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff.atom;

import org.monte.media.qtff.QTFFImageOutputStream;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Data Atom.
 */
public class DataAtom extends Atom {

    protected QTFFImageOutputStream data;
    protected boolean finished;

    /**
     * Creates a new DataAtom at the current position of the
     * ImageOutputStream.
     *
     * @param type The type name of the atom.
     * @param out  the output stream
     */
    public DataAtom(String type, ImageOutputStream out) throws IOException {
        super(type, out);
        out.writeLong(0); // make room for the atom header
        data = new QTFFImageOutputStream(out);
    }

    @Override
    public void finish() throws IOException {
        if (!finished) {
            long sizeBefore = size();

            if (size() > 0xffffffffL) {
                throw new IOException("DataAtom \"" + type + "\" is too large: " + size());
            }

            long pointer = out.getStreamPosition();
            out.seek(offset);

            QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
            headerData.writeInt((int) size());
            headerData.writeType(type);
            out.seek(pointer);
            finished = true;
            long sizeAfter = size();
            if (sizeBefore != sizeAfter) {
                System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
            }
        }
    }

    /**
     * Returns the offset of this atom to the beginning of the random access
     * file
     */
    public long getOffset() {
        return offset;
    }

    public QTFFImageOutputStream getOutputStream() {
        if (finished) {
            throw new IllegalStateException("DataAtom is finished");
        }
        return data;
    }

    @Override
    public long size() {
        return 8 + data.length();
    }
}
