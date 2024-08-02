/*
 * @(#)IFD.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a TIFF Image File Directory (IFD).
 * <p>
 * An IFD consists of a 2-byte count of the number of directory entries
 * (i.e., the number of fields), followed by a sequence of 12-byte field entries,
 * followed by a 4-byte offset of the next IFD (or 0 if none).
 * <p>
 * There must be at least 1 IFD in a TIFF file and each IFD must have at least
 * one entry.
 *
 * @author Werner Randelshofer
 */
public class IFD {

    /**
     * The offset of this IFD inside the TIFF input stream when it was
     * read from the input stream.
     */
    private final long offset;

    /**
     * Whether this IFD has a nextOffset field.
     */
    private final boolean hasNextOffset;

    /**
     * The offset of the next IFD.
     * 0 if there are no more IFD's.
     */
    private long nextOffset;

    /**
     * The entries of this IFD.
     */
    private final ArrayList<IFDEntry> entries;

    public IFD(long offset, boolean hasNextOffset) {
        this.offset = offset;
        this.hasNextOffset = hasNextOffset;
        this.entries = new ArrayList<IFDEntry>();
    }

    /**
     * Returns the offset of the IFD.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the next IFD.
     */
    /*package*/ void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }

    /**
     * Gets the offset of the next IFD.
     * Returns 0 if there is no next IFD.
     */
    public long getNextOffset() {
        return (hasNextOffset) ? this.nextOffset : 0;
    }

    public boolean hasNextOffset() {
        return hasNextOffset;
    }

    /**
     * Returns the number of entries in the IFD.
     */
    public int getCount() {
        return entries.size();
    }

    /**
     * Returns the {@code IFDEntry} at the specified index.
     */
    public IFDEntry get(int index) {
        return null;
    }

    /**
     * Adds an {@code IFDEntry}.
     */
    /* package */ void add(IFDEntry entry) {
        entries.add(entry);
    }

    /**
     * Returns an unmodifiale list of the {@code IFDEntry}s.
     */
    public List<IFDEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("IFD offset:");
        buf.append(offset);
        buf.append(", numEntries:");
        buf.append(entries.size());
        buf.append(", next:");
        buf.append(nextOffset);

        for (IFDEntry e : entries) {
            buf.append("\n  ");
            buf.append(e);
        }

        return buf.toString();
    }

    /**
     * Returns the length of this IFD in bytes.
     */
    public long getLength() {
        return getCount() * 12 + (hasNextOffset ? 4 : 0);
    }

}
