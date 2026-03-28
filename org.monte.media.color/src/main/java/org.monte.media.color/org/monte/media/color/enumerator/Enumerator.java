/*
 * @(#)Enumerator.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color.enumerator;


import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/// Interface for classes that implement both the [BareEnumerator] and
/// the [Spliterator] interface.
///
/// @param <E> the element type
public interface Enumerator<E> extends BareEnumerator<E>, Spliterator<E> {
    @Override
    default boolean tryAdvance(Consumer<? super E> action) {
        if (moveNext()) {
            action.accept(current());
            return true;
        }
        return false;
    }

    /// An object for enumerating primitive long-valued elements of a collection.
    ///
    /// The protocol for accessing elements via a `Enumerator` imposes smaller per-element overhead than
    /// [Iterator], and avoids the inherent race involved in having separate methods for
    /// `hasNext()` and `next()`.
    interface OfLong extends Enumerator<Long>, Spliterator.OfLong {

        /// {@inheritDoc}
        @Override
        default Long current() {
            return currentAsLong();
        }

        /// Returns the current value.
        ///
        /// @return current
        /// @see Enumerator#current()
        long currentAsLong();

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(LongConsumer action) {
            if (moveNext()) {
                action.accept(currentAsLong());
                return true;
            }
            return false;
        }

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(Consumer<? super Long> action) {
            return Enumerator.super.tryAdvance(action);
        }

    }

    /// An object for enumerating primitive double-valued elements of a collection.
    interface OfDouble extends Enumerator<Double>, Spliterator.OfDouble {

        /// {@inheritDoc}
        @Override
        default Double current() {
            return currentAsDouble();
        }

        /// Returns the current value.
        ///
        /// @return current
        /// @see Enumerator#current()
        double currentAsDouble();

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(DoubleConsumer action) {
            if (moveNext()) {
                action.accept(currentAsDouble());
                return true;
            }
            return false;
        }

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(Consumer<? super Double> action) {
            return Enumerator.super.tryAdvance(action);
        }

    }

    /// An object for enumerating primitive int-valued elements of a collection.
    interface OfInt extends Enumerator<Integer>, Spliterator.OfInt {

        /// {@inheritDoc}
        @Override
        default Integer current() {
            return currentAsInt();
        }

        /// Returns the current value.
        ///
        /// @return current
        /// @see Enumerator#current()
        int currentAsInt();

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(IntConsumer action) {
            if (moveNext()) {
                action.accept(currentAsInt());
                return true;
            }
            return false;
        }

        /// {@inheritDoc}
        @Override
        default boolean tryAdvance(Consumer<? super Integer> action) {
            return Enumerator.super.tryAdvance(action);
        }

    }

    class EmptyOfInt implements OfInt {

        @Override
        public int currentAsInt() {
            return 0;
        }

        @Override
        public boolean moveNext() {
            return false;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }

    OfInt EMPTY_OF_INT = new EmptyOfInt();
}
