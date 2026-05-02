/*
 * @(#)PiecewiseToneMapperTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PiecewiseToneMapperTest {
    /// Should compute the expected inverse map
    @ParameterizedTest
    @ValueSource(ints = {2, 256, 512, 1024, 2048})
    public void shouldComputeInverseMapForGamma(int n) {
        float[] trc = new float[n];
        var reference = new GammaToneMapper(2.4f, 1.055f, 0.055f, 12.92f, 0.04045f);
        for (int i = 0; i < n; i++) {
            var y = i / (float) (trc.length - 1);
            var x = reference.linearToCurved(0, y);
            trc[i] = x;
        }
        var instance = new PiecewiseToneMapper(trc);
        for (int i = 0; i < n; i++) {
            var v = i / (float) (trc.length - 1);
            var expectedX = reference.linearToCurved(0, v);
            var actualX = instance.linearToCurved(0, v);
            assertEquals(expectedX, actualX, 1e-4f, "linearToCurved at i=" + i);
            var actualV = instance.curvedToLinear(0, actualX);
            assertEquals(v, actualV, 1e-4f, "inverse at i=" + i);

            var expectedY = reference.curvedToLinear(0, v);
            var actualY = instance.curvedToLinear(0, v);
            assertEquals(expectedY, actualY, 1e-3f, "curvedToLinear at i=" + i);
        }

    }

    /// Should compute the expected inverse map
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8, 256, 512, 1024, 2048})
    public void shouldComputeInverseMapForLinear(int n) {
        float[] trc = new float[n];
        var reference = new ToneMapper() {
            @Override
            public float linearToCurved(int component, float y) {
                return y;
            }

            @Override
            public float curvedToLinear(int component, float x) {
                return x;
            }
        };
        for (int i = 0; i < n; i++) {
            var y = i / (float) (trc.length - 1);
            var x = reference.linearToCurved(0, y);
            trc[i] = x;
        }
        var instance = new PiecewiseToneMapper(trc);
        for (int i = 0; i < n; i++) {
            var v = i / (float) (trc.length - 1);
            var expectedX = reference.linearToCurved(0, v);
            var actualX = instance.linearToCurved(0, v);
            assertEquals(expectedX, actualX, 1e-4f, "linearToCurved at i=" + i);
            var expectedY = reference.curvedToLinear(0, v);
            var actualY = instance.curvedToLinear(0, v);
            assertEquals(expectedY, actualY, 1e-4f, "curvedToLinear at i=" + i);
        }

    }

}