/*
 * @(#)WideDataAtom.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff.atom;

import org.monte.media.qtff.QTFFImageOutputStream;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * WideDataAtom can grow larger then 4 gigabytes.
 */
public class WideDataAtom extends Atom {

    protected QTFFImageOutputStream data;
    protected boolean finished;

    /**
     * Creates a new DataAtom at the current position of the
     * ImageOutputStream.
     *
     * @param type The type of the atom.
     * @param out
     */
    public WideDataAtom(String type, ImageOutputStream out) throws IOException {
        super(type, out);
        out.writeLong(0); // make room for the atom header
        out.writeLong(0); // make room for the atom header
        data = new QTFFImageOutputStream(out) {
            @Override
            public void flush() throws IOException {
                // DO NOT FLUSH UNDERLYING STREAM!
            }
        };
    }

    @Override
    public void finish() throws IOException {
        if (!finished) {
            long pointer = out.getStreamPosition();
            out.seek(offset);

            QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
            long finishedSize = size();
            if (finishedSize <= 0xffffffffL) {
                headerData.writeInt(8);
                headerData.writeType("wide");
                headerData.writeInt((int) (finishedSize - 8));
                headerData.writeType(type);
            } else {
                headerData.writeInt(1); // special value for extended size atoms
                headerData.writeType(type);
                headerData.writeLong(finishedSize - 8);
            }

            out.seek(pointer);
            finished = true;
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
            throw new IllegalStateException("Atom is finished");
        }
        return data;
    }

    @Override
    public long size() {
        return 16 + data.length();
    }
}
