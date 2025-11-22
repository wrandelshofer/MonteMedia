/*
 * @(#)OctreeColorQuantizer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.quant;

import org.monte.media.color.octree.Color;
import org.monte.media.color.octree.OctreeAlgo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/// Estimates a color palette using an octree algorithm.
///
/// References:
/// <dl>
/// <dt>M. Gervautz, W. Purgathofer. (1988).
///       A Simple Method for Color Quantization: Octree Quantizati.</dt>
///  <dd>[www.cg.tuwien.ac.at](https://www.cg.tuwien.ac.at/research/publications/1988/purgathofer-1988-simple/purgathofer-1988-simple-Paper.PDF)</a></dd>
/// </dl>
public class OctreeColorQuantizer implements ColorQuantizer {
    private OctreeAlgo algo;
    private IndexColorModel colorModel;

    public OctreeColorQuantizer(int K) {
        this.algo = new OctreeAlgo(K);
    }

    @Override
    public void addImage(BufferedImage image) {
        int[] rgbArray = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgbArray, 0, image.getWidth());
        for (int i = 0; i < rgbArray.length; i++) {
            int rgb = rgbArray[i];
            algo.add(new Color(rgb));
        }
    }

    @Override
    public IndexColorModel computeColorPalette() {
        if (colorModel == null) {
            var table = algo.createColorTable();
            int[] cmap = new int[table.size()];
            for (int i = 0; i < cmap.length; i++) {
                cmap[i] = table.get(i).getRGB();
            }
            colorModel = new IndexColorModel(table.size() <= 256 ? 8 : 16, table.size(), cmap, 0, false, -1,
                    table.size() <= 256 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT);
        }
        return colorModel;
    }

    /// Returns the index of the representative color in the palette.
    ///
    /// @param rgb a color
    /// @return the representative color in the palette
    public int quant(int rgb) {
        return algo.quant(new Color(rgb));
    }
}
