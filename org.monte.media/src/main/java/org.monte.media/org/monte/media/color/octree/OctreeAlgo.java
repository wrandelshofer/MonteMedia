/*
 * @(#)OctreeAlgo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.octree;

import java.util.ArrayList;
import java.util.List;

/// Octree data structure.
///
/// References:
/// <dl>
/// <dt>M. Gervautz, W. Purgathofer. (1988).
///       A Simple Method for Color Quantization: Octree Quantizati.</dt>
///  <dd>[www.cg.tuwien.ac.at](https://www.cg.tuwien.ac.at/research/publications/1988/purgathofer-1988-simple/purgathofer-1988-simple-Paper.PDF)</a></dd>
/// </dl>
public class OctreeAlgo {
    /// Maximum depth of the octree.
    final static int MAX_DEPTH = 8;
    /// The number of desired colors.
    private final int K;
    /// One list for every depth level in the octree.
    private final Octree[] reduceList = new Octree[MAX_DEPTH];
    /// The number of leaves.
    private int size;
    private Octree root;

    public OctreeAlgo(int k) {
        K = k;
    }

    /// Adds a color to the octree.
    ///
    /// @param color a color
    public void add(Color color) {
        root = insertTree(root, color, 1);
        while (size > K) {
            reduceTree();
        }
    }

    private void addColors(Color rgb, Color rgb1) {
        rgb.R += rgb1.R;
        rgb.G += rgb1.G;
        rgb.B += rgb1.B;
    }

    /// Evaluates the branch of the octree for the color `rgb` in
    /// depth `depth`.
    ///
    /// @param rgb   the color
    /// @param depth the depth
    /// @return the branch to take (a value between 0 and 7).
    private int branch(Color rgb, int depth) {
        int shift = MAX_DEPTH - depth;
        int bitMask = 1 << shift;
        return (((bitMask & rgb.R) >>> shift) << 2)
                | (((bitMask & rgb.G) >>> shift) << 1)
                | ((bitMask & rgb.B) >>> shift);
    }

    /// Creates the color table from the tree.
    public List<Color> createColorTable() {
        var colorTable = new ArrayList<Color>();
        initColorTable(root, colorTable);
        return colorTable;
    }

    /// Finds the best reducible node of the octree.
    private Octree getReducible() {
        Octree node;
        int octreeDepth = MAX_DEPTH;
        while (reduceList[octreeDepth - 1] == null) {
            octreeDepth--;
        }
        node = reduceList[octreeDepth - 1];
        reduceList[octreeDepth - 1] = node.nextNode;
        return node;
    }

    /// Fills the color table with the means of the colors
    /// represented by the octree leaves.
    private void initColorTable(Octree tree, List<Color> colorTable) {
        if (tree == null) return;
        if (tree.isLeaf) {
            // the color index is also written into the octree leaf
            tree.colorIndex = colorTable.size();
            colorTable.add(mean(tree.rgb, tree.colorCount));
        } else {
            for (int i = 0; i < 8; i++) {
                initColorTable(tree.next[i], colorTable);
            }
        }
    }

    /// Inserts the color `rgb` into the subtree `tree`
    /// in depth `depth`.
    ///
    /// @param tree  the subtree
    /// @param rgb   the color
    /// @param depth the depth
    private Octree insertTree(Octree tree, Color rgb, int depth) {
        if (tree == null) {
            tree = newAndInit(depth);
        }
        if (tree.isLeaf) {
            tree.colorCount++;
            addColors(tree.rgb, rgb);
        } else {
            int index = branch(rgb, depth);
            tree.next[index] = insertTree(tree.next[index], rgb, depth + 1);
        }
        return tree;
    }

    /// Inserts the node `node` with depth `level`
    /// into the right list.
    private void makeReducible(int level, Octree node) {
        node.nextNode = reduceList[level];
        reduceList[level] = node;
    }

    private Color mean(Color rgb, int colorCount) {
        return new Color(rgb.R / colorCount, rgb.G / colorCount, rgb.B / colorCount);
    }

    /// Produces and initializes a new octree node
    /// for insertion into the tree at the specified depth.
    ///
    /// @param depth the depth
    public Octree newAndInit(int depth) {
        int i;
        Octree node;
        if (depth == MAX_DEPTH) {
            node = new Octree(depth, true);
            size++;
        } else {
            node = new Octree(depth, false);
            makeReducible(depth, node);
        }
        return node;
    }

    /// For the original color `orig` its representative
    /// is searched for in the octree, and the index of
    /// its color table entry is returned.
    ///
    /// @param orig original color
    /// @return color table index
    public int quant(Color orig) {
        return quant(root, orig);
    }

    private int quant(Octree tree, Color orig) {
        if (tree.isLeaf) {
            return tree.colorIndex;
        } else {
            return quant(tree.next[branch(orig, tree.level)], orig);
        }
    }

    /// Combines the successors of an intermediate node to one leaf.
    private void reduceTree() {
        Octree tree = getReducible();
        int children = 0;
        int colorCount = 0;
        Color sum = tree.rgb;
        for (int i = 0; i < 8; i++) {
            if (tree.next[i] != null) {
                children++;
                addColors(sum, tree.next[i].rgb);
                colorCount += tree.next[i].colorCount;
            }
        }
        tree.turnIntoLeaf();
        tree.colorCount = colorCount;
        size = size - children + 1;
    }
}
