package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

@Disabled
public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", SrgbColorSpace.getInstance());
    }
}