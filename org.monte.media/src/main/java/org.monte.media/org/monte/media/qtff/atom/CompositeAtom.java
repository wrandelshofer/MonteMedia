/*
 * @(#)CompositeAtom.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff.atom;

import org.monte.media.qtff.QTFFImageOutputStream;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A CompositeAtom contains an ordered list of Atoms.
 */
public class CompositeAtom extends DataAtom {

    protected LinkedList<Atom> children;

    /**
     * Creates a new CompositeAtom at the current position of the
     * ImageOutputStream.
     *
     * @param type The type of the atom.
     * @param out
     */
    public CompositeAtom(String type, ImageOutputStream out) throws IOException {
        super(type, out);
        children = new LinkedList<>();
    }

    public void add(Atom child) throws IOException {
        if (children.size() > 0) {
            children.get(children.size() - 1).finish();
        }
        children.add(child);
    }

    /**
     * Writes the atom and all its children to the ImageOutputStream and
     * disposes of all resources held by the atom.
     *
     * @throws IOException
     */
    @Override
    public void finish() throws IOException {
        if (!finished) {
            if (size() > 0xffffffffL) {
                throw new IOException("CompositeAtom \"" + type + "\" is too large: " + size());
            }

            long pointer = out.getStreamPosition();
            out.seek(offset);

            QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
            headerData.writeInt((int) size());
            headerData.writeType(type);
            for (Atom child : children) {
                child.finish();
            }
            out.seek(pointer);
            finished = true;
        }
    }

    @Override
    public long size() {
        long length = 8 + data.length();
        for (Atom child : children) {
            length += child.size();
        }
        return length;
    }
}
