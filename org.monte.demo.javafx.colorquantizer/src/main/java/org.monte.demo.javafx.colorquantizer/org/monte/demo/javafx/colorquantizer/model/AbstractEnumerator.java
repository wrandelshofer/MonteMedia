/*
 * @(#)AbstractEnumerator.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.javafx.colorquantizer.model;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Abstract base classes for {@link Enumerator}s.
 * <p>
 * Subclasses should only implement the {@link Enumerator#moveNext()}
 * method and (optionally) the {@link Spliterator#trySplit()} method:
 * <pre>
 *     public boolean moveNext() {
 *         if (...end not reached...) {
 *             current = ...;
 *             return true;
 *         }
 *         return false;
 *     }
 * </pre>
 *
 * @param <E> the element type
 */
public abstract class AbstractEnumerator<E> extends Spliterators.AbstractSpliterator<E>
        implements Enumerator<E> {
    /**
     * The current element of the enumerator.
     */
    protected E current;

    /**
     * Creates a spliterator reporting the given estimated size and
     * additionalCharacteristics.
     *
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Long.MAX_VALUE}.
     * @param additionalCharacteristics properties of this spliterator's
     *                                  source or elements.  If {@code SIZED} is reported then this
     *                                  spliterator will additionally report {@code SUBSIZED}.
     */
    protected AbstractEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public E current() {
        return current;
    }
}
