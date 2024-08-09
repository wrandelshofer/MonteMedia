/*
 * @(#)TIFFNode.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * TIFFNode.
 *
 * @author Werner Randelshofer
 */
public abstract class TIFFNode {

    /**
     * The tag which identifies this node.
     */
    protected TIFFTag tag;
    /**
     * The children.
     */
    private final ArrayList<TIFFNode> children = new ArrayList<TIFFNode>();
    private TIFFNode parent;
    /**
     * The IFDEntry from which this node was read.
     * IFDEntry is null, if this node has not been read from a TIFF file.
     */
    protected IFDEntry ifdEntry;

    public TIFFNode(TIFFTag tag) {
        this.tag = tag;
    }

    public String getTagName() {
        return tag == null ? "unnamed" : tag.getName();
    }

    public TIFFTag getTag() {
        return tag;
    }

    /**
     * Returns the tag number or -1 if not known.
     */
    public int getTagNumber() {
        return tag != null ? tag.getNumber() : -1;
    }

    public IFDEntry getIFDEntry() {
        return ifdEntry;
    }

    public void add(TIFFNode node) {
        children.add(node);
        node.parent = this;
    }

    public ArrayList<TIFFNode> getChildren() {
        return children;
    }

    public TIFFNode getParent() {
        return parent;
    }

    public Iterator<TIFFNode> preorderIterator() {
        return new PreorderIterator(this);
    }

    public Iterator<TIFFNode> postorderIterator() {
        return new PostorderIterator(this);
    }

    public int getChildCount() {
        return children.size();
    }

    public TIFFNode getChildAt(int i) {
        return children.get(i);
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    private static class PreorderIterator implements Iterator<TIFFNode> {

        private final Stack<Iterator<TIFFNode>> stack = new Stack<Iterator<TIFFNode>>();
        private TIFFNode current;

        private PreorderIterator(TIFFNode root) {
            LinkedList<TIFFNode> ll = new LinkedList<TIFFNode>();
            ll.add(root);
            stack.push(ll.iterator());
        }

        @Override
        public boolean hasNext() {
            return (!stack.empty()
                    && stack.peek().hasNext());
        }

        @Override
        public TIFFNode next() {
            Iterator<TIFFNode> enumer = stack.peek();
            current = enumer.next();

            if (!enumer.hasNext()) {
                stack.pop();
            }
            ArrayList<TIFFNode> children = current.getChildren();
            if (!children.isEmpty()) {
                stack.push(children.iterator());
            }
            return current;
        }

        @Override
        public void remove() {
            current.removeFromParent();
        }
    }

    private static class PostorderIterator implements Iterator<TIFFNode> {

        protected TIFFNode root;
        protected Iterator<TIFFNode> children;
        protected Iterator<TIFFNode> subtree;
        private TIFFNode current;

        private PostorderIterator(TIFFNode rootNode) {
            root = rootNode;
            children = new ArrayList<TIFFNode>(root.children).iterator();
            subtree = EMPTY_ITERATOR;
        }

        @Override
        public boolean hasNext() {
            return root != null;
        }

        @Override
        public TIFFNode next() {

            if (subtree.hasNext()) {
                current = subtree.next();
            } else if (children.hasNext()) {
                subtree = new PostorderIterator(children.next());
                current = subtree.next();
            } else {
                current = root;
                root = null;
            }

            return current;
        }

        @Override
        public void remove() {
            current.removeFromParent();
        }
    }  // End of class PostorderEnumeration

    static private final Iterator<TIFFNode> EMPTY_ITERATOR = new Iterator<TIFFNode>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public TIFFNode next() {
            throw new NoSuchElementException("No more elements");
        }

        @Override
        public void remove() {
            throw new NoSuchElementException("No more elements");
        }
    };
}
