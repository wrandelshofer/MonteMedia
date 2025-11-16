/*
 * @(#)JavaPyccColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

@Disabled("BROKEN")
public class JavaPyccColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("PYCC", ColorSpace.getInstance(ColorSpace.CS_PYCC));
    }
}