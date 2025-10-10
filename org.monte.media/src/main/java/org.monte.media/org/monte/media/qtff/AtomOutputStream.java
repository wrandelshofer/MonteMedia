/*
 * @(#)AtomOutputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.util.Stack;

public class AtomOutputStream extends QTFFImageOutputStream {
    private Stack<Atom> stack = new Stack<>();


    public AtomOutputStream(ImageOutputStream out) throws IOException {
        this(out, false);
    }

    public AtomOutputStream(ImageOutputStream out, boolean forwardFlushAndClose) throws IOException {
        super(out, forwardFlushAndClose);
    }

    public void pushAtom(int atomType) throws IOException {
        pushAtom(QTFFParser.typeToString(atomType));
    }

    public void pushAtom(String atomType) throws IOException {
        stack.push(new SimpleAtom(atomType));
    }

    public void pushWideAtom(int atomType) throws IOException {
        pushAtom(QTFFParser.typeToString(atomType));
    }

    public void pushWideAtom(String atomType) throws IOException {
        stack.push(new WideAtom(atomType));
    }

    public void popAtom() throws IOException {
        Atom atom = stack.pop();
        atom.finish();
    }

    /**
     * Atom base class.
     */
    private abstract class Atom {

        /**
         * The atomType of the atom. A String with the length of 4 characters.
         */
        protected String atomType;
        /**
         * The offset of the atom relative to the start of the
         * ImageOutputStream.
         */
        protected long offset;
        protected boolean finished;

        /**
         * Creates a new Atom at the current position of the ImageOutputStream.
         *
         * @param atomType The atomType of the atom. A string with a length of 4 characters.
         */
        public Atom(String atomType) throws IOException {
            this.atomType = atomType;
            offset = getStreamPosition();
        }

        /**
         * Writes the atom to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

    }

    /**
     * Simple Atom.
     */
    private class SimpleAtom extends Atom {

        /**
         * Creates a new SimpleAtom at the current position of the
         * ImageOutputStream.
         *
         * @param atomType The atomType of the atom.
         */
        public SimpleAtom(String atomType) throws IOException {
            super(atomType);
            out.writeLong(0); // make room for the atom header
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long streamPosition = getStreamPosition();
                long size = streamPosition - offset;
                if (size > 0xffffffffL) {
                    throw new IOException("SimpleAtom \"" + atomType + "\" is too large: " + size);
                }

                seek(offset);

                writeUInt(size);
                writeType(atomType);
                seek(streamPosition);
                finished = true;
            }
        }

    }

    /**
     * Wide Atom.
     */
    private class WideAtom extends Atom {

        /**
         * Creates a new DataAtom at the current position of the
         * ImageOutputStream.
         *
         * @param atomType The atomType of the atom.
         */
        public WideAtom(String atomType) throws IOException {
            super(atomType);
            out.writeLong(0); // make room for the wide atom
            out.writeLong(0); // make room for the atom header
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long streamPosition = getStreamPosition();
                long size = streamPosition - offset;

                seek(offset);

                if (size > 0xffffffffL) {
                    writeUInt(1);
                    writeType(atomType);
                    writeLong(size);
                } else {
                    writeUInt(8);
                    writeType("wide");
                    writeUInt(size);
                    writeType(atomType);
                }
                seek(streamPosition);
                finished = true;
            }
        }

    }
}
