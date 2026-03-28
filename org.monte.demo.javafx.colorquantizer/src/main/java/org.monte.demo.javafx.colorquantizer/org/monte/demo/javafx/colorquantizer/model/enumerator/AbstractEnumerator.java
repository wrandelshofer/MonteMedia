/*
 * @(#)AbstractEnumerator.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.javafx.colorquantizer.model.enumerator;

import java.util.Spliterators;

/// Abstract base classes for [Enumerator]s.
///
/// Subclasses should only implement the [#moveNext()]
/// method and (optionally) the [#trySplit()] method:
/// ```
///     public boolean moveNext() {
///         if (...end not reached...) {
///             current = ...;
///             return true;
///         }
///         return false;
///     }
/// ```
///
/// @param <E> the element type
public abstract class AbstractEnumerator<E> extends Spliterators.AbstractSpliterator<E>
        implements Enumerator<E> {
    /// The current element of the enumerator.
    protected E current;

    /// Creates a spliterator reporting the given estimated size and
    /// additionalCharacteristics.
    ///
    /// @param est                       the estimated size of this spliterator if known, otherwise
    ///                                                                                                                                                                                                       `Long.MAX_VALUE`.
    /// @param additionalCharacteristics properties of this spliterator's
    ///                                                                                                                                                                                                       source or elements.  If `SIZED` is reported then this
    ///                                                                                                                                                                                                       spliterator will additionally report `SUBSIZED`.
    protected AbstractEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /// {@inheritDoc}
    @Override
    public E current() {
        return current;
    }
}
