/*
 * @(#)CssLinearSrgbColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class CssLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }


}