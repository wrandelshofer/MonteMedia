/*
 * @(#)Octree.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.octree;

/// Octree data structure.
///
/// References:
/// <dl>
/// <dt>M. Gervautz, W. Purgathofer. (1988).
///       A Simple Method for Color Quantization: Octree Quantizati.</dt>
///  <dd>[www.cg.tuwien.ac.at](https://www.cg.tuwien.ac.at/research/publications/1988/purgathofer-1988-simple/purgathofer-1988-simple-Paper.PDF)</a></dd>
/// </dl>
public class Octree {
    final int level;
    int colorCount;
    int colorIndex = -1;
    Color rgb = new Color(0, 0, 0);
    Octree[] next;
    /// Next node in the same depth level.
    Octree nextNode;
    /// True if leaf node, false if intermediate node.
    boolean isLeaf;

    public Octree(int level, boolean isLeaf) {
        this.level = level;
        this.isLeaf = isLeaf;
        if (!isLeaf) next = new Octree[8];
    }

    public void turnIntoLeaf() {
        isLeaf = true;
        // next = null;
    }
}
