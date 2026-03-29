package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

@Disabled("this test succeeds - the java implementation is okay")
public class JavaCieXyzColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("CIE XYZ", ColorSpace.getInstance(ColorSpace.CS_CIEXYZ));
    }
}