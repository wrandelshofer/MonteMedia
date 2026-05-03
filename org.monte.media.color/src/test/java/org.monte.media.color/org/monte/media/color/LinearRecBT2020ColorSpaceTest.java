/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.monte.media.color;

public class LinearRecBT2020ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return RecBT2020ColorSpace.getInstance().getLinearColorSpace();
    }

}