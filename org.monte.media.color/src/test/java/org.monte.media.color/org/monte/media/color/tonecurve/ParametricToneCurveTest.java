/*
 * @(#)ParametricToneCurveTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParametricToneCurveTest {
    @Test
    public void shouldConvertToGammaToneCurveAndBack() {
        var expected = new ParametricToneCurve(2.22222f, 0.9099121f, 0.09008789f, 0.22222f, 0.08099365f);
        var gtm = expected.toGammaToneCurve();
        var actual = gtm.toParametricToneCurve();
        assertEquals(expected, actual);
    }
}