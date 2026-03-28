/*
 * @(#)BareEnumerator.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.javafx.colorquantizer.model.enumerator;

import java.util.Iterator;

/// Bare (minimalistic) interface for enumerating elements of a collection.
///
/// The protocol for accessing elements via a `BareEnumerator` imposes
/// smaller per-element overhead than [Iterator], and avoids the inherent
/// race involved in having separate methods for
/// `hasNext()` and `next()`.
///
/// This interface is typically not used, it is more convenient to use
/// the [Enumerator] interface instead, which extends from the
/// [java.util.Spliterator] interface.
///
/// Usage:
/// ```
/// {@literal
///  for (BareEnumerator<E> i = ...; i.moveNext(); ) {
///    var e = i.current();
///    ...
///  }
///  }</pre>
///
/// @param <E> the element type
public interface BareEnumerator<E> {
    /// Advances the enumerator to the next element of the collection.
    ///
    /// @return true if the enumerator was successfully advanced to the next element;
    /// false if the enumerator has passed the end of the collection.
    boolean moveNext();

    /// Gets the element in the collection at the current position of the enumerator.
    ///
    /// Current is undefined under any of the following conditions:
    ///
    ///   - The enumerator is positioned before the first element in the collection.
    ///     Immediately after the enumerator is created [#moveNext] must be called to advance
    ///     the enumerator to the first element of the collection before reading the value of Current.
    ///   - The last call to [#moveNext] returned false, which indicates the end
    ///     of the collection.
    ///   - The enumerator is invalidated due to changes made in the collection,
    ///     such as adding, modifying, or deleting elements.
    ///
    /// Current returns the same object until MoveNext is called.MoveNext
    /// sets Current to the next element.
    ///
    /// @return current
    E current();
}
