/*
 * @(#)ParametricHlsColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", new SrgbColorSpace());
    }
}