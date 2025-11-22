/*
 * @(#)OKLabColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OKLabColorSpaceTest extends AbstractNamedColorSpaceTest {


    @Override
    protected OKLabColorSpace getInstance() {
        return new OKLabColorSpace();
    }

    /// Table of example XYZ and Oklab pairs.
    ///
    /// Provided to test Oklab implementations. Computed by transforming the XYZ coordinates to Oklab and rounding to three decimals.
    ///
    /// References:
    /// <dl>
    ///     <dt>Björn Ottosson, A perceptual color space for image processing.
    ///     [MIT License](https://github.com/bottosson/bottosson.github.io/blob/3d3f17644d7f346e1ce1ca08eb8b01782eea97af/misc/colorpicker/License.txt)</dt>
    ///     <dd>[github.io](https://bottosson.github.io/posts/oklab/)</dd>
    /// </dl>
    ///
    /// @param x X value
    /// @param y Y value
    /// @param z Z value
    /// @param l L value
    /// @param a a value
    /// @param b b value
    @ParameterizedTest
    @CsvSource({
            // X Y Z L a b
            "0.950,1.000,1.089,	1.000,0.000,0.000",
            "1.000,0.000,0.000,	0.450,1.236,-0.019",
            "0.000,1.000,0.000,	0.922,-0.671,0.263",
            "0.000,0.000,1.000,	0.153,-1.415,-0.449"
    })
    public void shouldConvertFromXYZ(float x, float y, float z, float l, float a, float b) {
        float eps = 0x1p-3f;//the values are rounded to three decimals


        OKLabColorSpace instance = getInstance();
        float[] xyzD65 = {x, y, z};
        D65XyzColorSpace d65cs = new D65XyzColorSpace();
        float[] xyzD50 = d65cs.toCIEXYZ(xyzD65);
        float[] actualLab = instance.fromCIEXYZ(xyzD50);
        assertEquals(l, actualLab[0], eps, "L");
        assertEquals(a, actualLab[1], eps, "a");
        assertEquals(b, actualLab[2], eps, "b");

        float[] actualXyzD50 = instance.toCIEXYZ(new float[]{l, a, b});
        float[] actualXyzD65 = d65cs.fromCIEXYZ(actualXyzD50);
        assertEquals(x, actualXyzD65[0], eps, "X");
        assertEquals(y, actualXyzD65[1], eps, "Y");
        assertEquals(z, actualXyzD65[2], eps, "Z");
    }

    @ParameterizedTest
    @CsvSource({
            // RGB L a b
            "1,1,1, 1,0,0",
    })
    public void shouldConvertFromRGB(float red, float green, float blue, float l, float a, float b) {
        float eps = 0x1p-3f;//the values are rounded to three decimals


        OKLabColorSpace instance = getInstance();
        float[] rgb = {red, green, blue};
        float[] actualLab = instance.fromRGB(rgb);
        assertEquals(l, actualLab[0], eps, "L");
        assertEquals(a, actualLab[1], eps, "a");
        assertEquals(b, actualLab[2], eps, "b");
    }
}