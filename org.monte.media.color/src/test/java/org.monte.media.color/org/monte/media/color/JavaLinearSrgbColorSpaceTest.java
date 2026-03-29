package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

/**
 * This test exists only for comparison with {@link LinearSrgbColorSpace}.
 */
@Disabled("this test fails - the java implementation is mostly okay")
public class JavaLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("Linear RGB", ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB));
    }
}