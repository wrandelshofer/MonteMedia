/*
 * @(#)ParallelExtractAmigaBitmapFactoryTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractAmigaBitmapImageConverterTest {
    protected abstract AmigaBitmapImageConverter newInstance();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
    public void shouldConvertToBitmapAndBack(int depth) {
        var rng = new Random(0);
        var cmap = new int[1 << depth];
        for (int i = 0; i < cmap.length; i++) cmap[i] = rng.nextInt();
        var icm = new IndexColorModel(depth, 1 << depth, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
        var input = new BufferedImage(32, 20, BufferedImage.TYPE_BYTE_INDEXED, icm);
        byte[] pixels = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int mask = (1 << depth) - 1;
        for (int i = 0; i < 256; i++) pixels[i] = (byte) (i & mask);
        for (int i = 256; i < pixels.length; i++) pixels[i] = (byte) (rng.nextInt() & mask);

        var factory = newInstance();
        var bitmap = factory.toBitmapImage(input, null);

        var output = factory.toBufferedImage(bitmap, null);
        assertEquals(input.getColorModel(), output.getColorModel());
        assertArrayEquals(pixels, ((DataBufferByte) output.getRaster().getDataBuffer()).getData());
    }
}