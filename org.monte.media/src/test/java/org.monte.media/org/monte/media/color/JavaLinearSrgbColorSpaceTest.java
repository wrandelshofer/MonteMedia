/*
 * @(#)JavaLinearSrgbColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

/**
 * This test exists only for comparison with {@link LinearSrgbColorSpace}.
 */
@Disabled("this test succeeds - the java implementation is okay")
public class JavaLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("Linear RGB", ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB));
    }
}