/*
 * @(#)ParametricLchColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class ParametricLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricLchColorSpace getInstance() {
        return new ParametricLchColorSpace("CIE LCH", new CieLabColorSpace());
    }


}