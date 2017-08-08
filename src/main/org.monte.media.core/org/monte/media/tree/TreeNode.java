/* @(#)TreeNode
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TreeNode.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
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
        child.setParent((T) this);

        children.add(child);
    }

    public Iterable<T> children() {
        return children;
    }

    public void sortChildren(Comparator<T> comparator) {
        Collections.sort(children, comparator);
    }

    public void removeAllChildren() {
        for (T child : children) {
            child.setParent(null);
        }
        children.clear();
    }
}
