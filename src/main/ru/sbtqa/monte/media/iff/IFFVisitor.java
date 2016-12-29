/* @(#)IFFVisitor.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.iff;

import ru.sbtqa.monte.media.AbortException;
import ru.sbtqa.monte.media.ParseException;

/**
IFFVisitor is notified each time the IFFParser visits
a data chunk and when a group is entered or leaved.

@version  1.0  1999-10-19
*/
public interface IFFVisitor
  {
  public void enterGroup(IFFChunk group)
  throws ParseException, AbortException;

  public void leaveGroup(IFFChunk group)
  throws ParseException, AbortException;

  public void visitChunk(IFFChunk group, IFFChunk chunk)
  throws ParseException, AbortException;
  }
