/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.iff;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;

/**
 * IFFVisitor is notified each time the IFFParser visits
 * a data chunk and when a group is entered or leaved.
 *
 */
public interface IFFVisitor {
    public void enterGroup(IFFChunk group)
            throws ParseException, AbortException;

    public void leaveGroup(IFFChunk group)
            throws ParseException, AbortException;

    public void visitChunk(IFFChunk group, IFFChunk chunk)
            throws ParseException, AbortException;
}
