/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.riff;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;

/**
 * RIFFVIsitor is notified each time the RIFFParser visits
 * a data chunk and when a group is entered or leaved.
 *
 */
public interface RIFFVisitor {
    /**
     * This method is invoked when the parser attempts to enter a group.
     * The visitor can return false, if the parse shall skip the group contents.
     *
     * @param group
     * @return True to enter the group, false to skip over the group.
     */
    public boolean enteringGroup(RIFFChunk group);

    /**
     * This method is invoked when the parser enters a group chunk.
     */
    public void enterGroup(RIFFChunk group)
            throws ParseException, AbortException;

    /**
     * This method is invoked when the parser leaves a group chunk.
     */
    public void leaveGroup(RIFFChunk group)
            throws ParseException, AbortException;

    /**
     * This method is invoked when the parser has read a data chunk or
     * has skipped a stop chunk.
     */
    public void visitChunk(RIFFChunk group, RIFFChunk chunk)
            throws ParseException, AbortException;
}
