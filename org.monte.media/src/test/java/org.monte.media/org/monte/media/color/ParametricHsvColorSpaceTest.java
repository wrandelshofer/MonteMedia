/*
 * @(#)ParametricHsvColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class ParametricHsvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHsvColorSpace getInstance() {
        return new ParametricHsvColorSpace("HSV", new SrgbColorSpace());
    }
}