package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

/**
 * This test exists only for comparison with {@link SrgbColorSpace}.
 */
@Disabled("this test mostly succeeds - the java implementation is almost okay")
public class JavaSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("sRGB", ColorSpace.getInstance(ColorSpace.CS_sRGB), -1);
    }
}