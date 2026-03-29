package org.monte.media.color;

import org.junit.jupiter.api.Disabled;

@Disabled("BROKEN")
public class OKHlsColorSpaceTest extends AbstractNamedColorSpaceTest {

    protected OKHlsColorSpace getInstance() {
        return new OKHlsColorSpace();
    }
}