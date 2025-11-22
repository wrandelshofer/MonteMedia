/*
 * @(#)LanczosKernelFactoryTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LanczosKernelFactoryTest {

    @Test
    public void shouldComputeLanczosKernel0p25() {
        float s = 0.25f;
        float sigma = 0.5f / s;
        var actual = new LanczosKernelFactory().createKernel(sigma * 3);
        System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);
        assertArrayEquals(new float[]{-1.5195744E-33f, 7.44496E-18f, -1.611881E-17f, 2.4816533E-17f, -3.223762E-17f, 3.72248E-17f, 1.0f, 3.72248E-17f, -3.223762E-17f, 2.4816533E-17f, -1.611881E-17f, 7.44496E-18f, -1.5195744E-33f}, actual);
    }

    @Test
    public void shouldComputeLanczosKernel0p5() {
        float s = 0.5f;
        float sigma = 0.5f / s;
        var actual = new LanczosKernelFactory().createKernel(sigma * 3);
        System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);
        assertArrayEquals(new float[]{1.5195744E-33f, -1.611881E-17f, 3.223762E-17f, 1.0f, 3.223762E-17f, -1.611881E-17f, 1.5195744E-33f}, actual);
    }

    @Test
    public void shouldComputeLanczosKernel0p75() {
        float s = 0.75f;
        float sigma = 0.5f / s;
        var actual = new LanczosKernelFactory().createKernel(sigma * 3);
        System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);
        assertArrayEquals(new float[]{-1.5195744E-33f, 2.4816533E-17f, 1.0f, 2.4816533E-17f, -1.5195744E-33f}, actual);
    }

    private static void checkSumEqualsTo1(float[] actual) {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (float v : actual) {
            stats.accept(v);
        }
        assertEquals(1f, (float) stats.getSum(), 0f, "sum must be equal to 1");
    }
}