/* @(#)EnumerationIterator.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.util;

import java.util.Enumeration;

/**
 * Wraps an Enumeration with the Iterator interface.
 *
 * @author Werni Randelshofer
 * @version $Id$
 */
public class EnumerationIterator<T> implements java.util.Iterator<T> {
    private Enumeration<T> enumer;

    /**
     * Creates new EnumIterator
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
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
