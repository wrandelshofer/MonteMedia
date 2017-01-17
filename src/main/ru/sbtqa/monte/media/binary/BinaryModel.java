/* @(#)BinaryModel.java
 * Copyright © 1999-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.binary;

/**
 * Model for untyped binary data.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version  2.1 2010-04-09 Refactored into an interface.
 * <br>1.0  1999-10-19
 */
public interface BinaryModel {
    /** Returns the total length of the binary data. */
    public long getLength();
    /**
    Gets a sequence of bytes and copies them into the supplied byte array.

    @param off the starting offset >= 0
    @param len the number of bytes >= 0 && <= size - offset
    @param target the target array to copy into
    @exception ArrayIndexOutOfBoundsException  Thrown if the area covered by
    the arguments is not contained in the model.
     */
    public int getBytes(long off, int len, byte[] target);

    /** Closes the model and disposes all resources. */
    public void close();
}
