/*
 * @(#)LinearRec2020ColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class LinearRec2020ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new Rec2020ColorSpace().getLinearColorSpace();
    }

}