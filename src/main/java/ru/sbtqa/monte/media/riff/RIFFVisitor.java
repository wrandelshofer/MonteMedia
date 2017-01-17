/* @(#)RIFFVIsitor.java
 * Copyright © 2005 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.riff;

import ru.sbtqa.monte.media.AbortException;
import ru.sbtqa.monte.media.ParseException;

/**
 * RIFFVIsitor is notified each time the RIFFParser visits a data chunk and when
 * a group is entered or leaved.
 *
 * @version 1.0 2005-01-09 Created.
 */
public interface RIFFVisitor {

    /**
     * This method is invoked when the parser attempts to enter a group. The
     * visitor can return false, if the parse shall skip the group contents.
     *
     * @param group TODO
     * @return True to enter the group, false to skip over the group.
     */
    public boolean enteringGroup(RIFFChunk group);

    /**
     * This method is invoked when the parser enters a group chunk
     *
     * @param group TODO
     * @throws ru.sbtqa.monte.media.ParseException TODO
     * @throws ru.sbtqa.monte.media.AbortException TODO
     */
    public void enterGroup(RIFFChunk group)
          throws ParseException, AbortException;

    /**
     * This method is invoked when the parser leaves a group chunk
     *
     * @param group TODO
     * @throws ru.sbtqa.monte.media.ParseException TODO
     * @throws ru.sbtqa.monte.media.AbortException TODO
     */
    public void leaveGroup(RIFFChunk group)
          throws ParseException, AbortException;

    /**
     * This method is invoked when the parser has read a data chunk or has
     * skipped a stop chunk
     *
     * @param group TODO
     * @param chunk TODO
     * @throws ru.sbtqa.monte.media.ParseException TODO
     * @throws ru.sbtqa.monte.media.AbortException TODO
     */
    public void visitChunk(RIFFChunk group, RIFFChunk chunk)
          throws ParseException, AbortException;
}
