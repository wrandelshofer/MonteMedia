/*
 * @(#)Atom.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.qtff.atom;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Atom base class.
 */
public abstract class Atom {

    /**
     * The type of the atom. A String with the length of 4 characters.
     */
    protected String type;
    /**
     * The offset of the atom relative to the start of the
     * ImageOutputStream.
     */
    protected long offset;

    protected final ImageOutputStream out;

    /**
     * Creates a new Atom at the current position of the ImageOutputStream.
     *
     * @param type The type of the atom. A string with a length of 4
     *             characters.
     * @param out  the output stream
     */
    public Atom(String type, ImageOutputStream out) throws IOException {
        this.type = type;
        this.out = out;
        this.offset = out.getStreamPosition();
    }

    /**
     * Writes the atom to the ImageOutputStream and disposes it.
     */
    public abstract void finish() throws IOException;

    /**
     * Returns the size of the atom including the size of the atom header.
     *
     * @return The size of the atom.
     */
    public abstract long size();
}
