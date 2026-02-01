/*
 * @(#)TreeNode.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// TreeNode.
///
/// @param <T> the type of the `TreeNode`
/// @author Werner Randelshofer
public class TreeNode<T extends TreeNode<T>> {

    private T parent;
    private final List<T> children = new ArrayList<>();

    public T getChildAt(int index) {
        return children.get(index);
    }

    public int getChildCount() {
        return children.size();
    }

    protected void setParent(T newParent) {
        parent = newParent;
    }

    public T getParent() {
        return parent;
    }

    public void remove(T child) {
        if (child.getParent() == this) {
            children.remove(child);
            child.setParent(null);
        }
    }

    public void add(T child) {
        T oldParent = child.getParent();
        if (oldParent != null) {
            oldParent.remove(child);
        }
        //noinspection unchecked
        child.setParent((T) this);

        children.add(child);
    }

    public Iterable<T> children() {
        return children;
    }

    public void sortChildren(Comparator<T> comparator) {
        children.sort(comparator);
    }

    public void removeAllChildren() {
        for (T child : children) {
            child.setParent(null);
        }
        children.clear();
    }
}
