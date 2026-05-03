/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.monte.media.color;

public class ParametricScaledColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new ParametricScaledColorSpace("sRGB*255", 255f, SrgbColorSpace.getInstance(), -1);
    }
}