/*
 * @(#)AtomInputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Stack;

public class AtomInputStream extends QTFFImageInputStream {
    private Stack<Atom> stack = new Stack<>();
    private long atomEndPosition;

    public AtomInputStream(ImageInputStream in) throws IOException {
        super(in);
        atomEndPosition = length;
    }

    public String openAtom() throws IOException {
        long offset = getStreamPosition();
        long size = readUnsignedInt();
        String type = readType();
        if ("wide".equals(type)) {
            if (size != 8) throw new IOException("'wide' atom has illegal size: " + size);
            size = readUnsignedInt();
            type = readType();
        } else {
            if (size == 1) {
                size = readLong();
                if (size < 0) throw new IOException("'" + type + "' atom has illegal size: " + size);
            }
        }
        stack.push(new Atom(type, size, offset));
        atomEndPosition = offset + size;
        return type;
    }

    @Override
    public long available() throws IOException {
        return atomEndPosition - getStreamPosition();
    }

    @Override
    public long length() {
        return atomEndPosition;
    }

    public void closeAtom(String type) throws IOException {
        String actual = stack.isEmpty() ? null : stack.peek().atomType;
        if (!type.equals(actual)) {
            throw new IOException("attempted to close atom '" + type + "' but had atom '" + actual + "'.");
        }
        closeAtom();
    }

    public void closeAtom() throws IOException {
        Atom atom = stack.pop();
        atom.finish();
        if (stack.isEmpty()) {
            atomEndPosition = length;
        } else {
            atom = stack.peek();
            atomEndPosition = atom.offset + atom.size;
        }
    }

    /**
     * Atom base class.
     */
    private class Atom {

        /**
         * The atomType of the atom. A String with the length of 4 characters.
         */
        protected String atomType;
        /**
         * The offset of the atom relative to the start of the
         * ImageOutputStream.
         */
        protected long offset;
        protected long size;
        protected boolean finished;

        /**
         * Creates a new Atom at the current position of the ImageOutputStream.
         *
         * @param atomType The atomType of the atom. A string with a length of 4 characters.
         */
        public Atom(String atomType, long size, long offset) throws IOException {
            this.atomType = atomType;
            this.size = size;
            this.offset = offset;
        }

        public void finish() throws IOException {
            seek(offset + size);
        }

    }
}
