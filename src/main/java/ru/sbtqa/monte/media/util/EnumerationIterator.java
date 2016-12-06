/* @(#)EnumerationIterator.java
 * Copyright © 2001-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.util;

import java.util.*;

/**
 * Wraps an Enumeration with the Iterator interface.
 *
 * @author Werni Randelshofer
 * @version 1.2 2010-01-03 Generified.
 * <br>1.0 2001-10-08
 * @param <T> TODO
 */
public class EnumerationIterator<T> implements java.util.Iterator<T> {

    private Enumeration<T> enumer;

    /**
     * Creates new EnumIterator
     *
     * @param e TODO
     */
    public EnumerationIterator(Enumeration<T> e) {
        enumer = e;
    }

    @Override
    public boolean hasNext() {
        return enumer.hasMoreElements();
    }

    @Override
    public T next() {
        return enumer.nextElement();
    }

    /**
     * Throws always UnsupportedOperationException.
     *
     * @exception UnsupportedOperationException TODO
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
