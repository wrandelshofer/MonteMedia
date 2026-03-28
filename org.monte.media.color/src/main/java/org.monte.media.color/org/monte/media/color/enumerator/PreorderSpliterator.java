/*
 * @(#)PreorderSpliterator.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color.enumerator;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Function;

/// PreorderSpliterator.
///
/// @param <T> the element type
public class PreorderSpliterator<T> extends AbstractEnumerator<T> {
    private final Function<T, Iterable<? extends T>> getChildrenFunction;
    private final Deque<Iterator<? extends T>> stack = new ArrayDeque<>();

    public PreorderSpliterator(Function<T, Iterable<? extends T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        stack.push(Collections.singleton(root).iterator());
        this.getChildrenFunction = getChildrenFunction;
    }

    @Override
    public boolean moveNext() {
        Iterator<? extends T> iter = stack.peek();
        if (iter == null) {
            return false;
        }

        current = iter.next();
        if (!iter.hasNext()) {
            stack.pop();
        }
        Iterator<? extends T> children = getChildrenFunction.apply(current).iterator();
        if (children.hasNext()) {
            stack.push(children);
        }
        return true;
    }
}
