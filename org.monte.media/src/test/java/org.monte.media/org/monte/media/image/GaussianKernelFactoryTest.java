/*
 * @(#)GaussianKernelFactoryTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

import org.junit.jupiter.api.Test;
import org.monte.media.image.op.GaussianKernelFactory;

import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GaussianKernelFactoryTest {
    @Test
    public void shouldComputeGuassianKernel0p25() {
        float s = 0.25f;
        float sigma = 0.5f / s;
        var actual = new GaussianKernelFactory().createKernel(sigma);

        //System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);

        assertArrayEquals(new float[]{0.0022159242f, 0.008764151f, 0.026995484f, 0.0647588f, 0.12098536f, 0.17603266f, 0.20049524f, 0.17603266f, 0.12098536f, 0.0647588f, 0.026995484f, 0.008764151f, 0.0022159242f}, actual);
    }

    private static void checkSumEqualsTo1(float[] actual) {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (float v : actual) {
            stats.accept(v);
        }
        assertEquals(1f, (float) stats.getSum(), 0f, "sum must be equal to 1");
    }

    @Test
    public void shouldComputeGuassianKernel0p5() {
        float s = 0.5f;
        float sigma = 0.5f / s;
        var actual = new GaussianKernelFactory().createKernel(sigma);
        //System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);
        assertArrayEquals(new float[]{0.0044318484f, 0.053990968f, 0.24197072f, 0.39921293f, 0.24197072f, 0.053990968f, 0.0044318484f}, actual);
    }

    @Test
    public void shouldComputeGuassianKernel0p75() {
        float s = 0.75f;
        float sigma = 0.5f / s;
        var actual = new GaussianKernelFactory().createKernel(sigma);
        //System.out.println(Arrays.toString(actual));
        checkSumEqualsTo1(actual);
        assertArrayEquals(new float[]{0.0066477754f, 0.1942764f, 0.5981516f, 0.1942764f, 0.0066477754f}, actual);
    }

}