/*
 * @(#)LinearDisplayP3ColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class LinearDisplayP3ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new DisplayP3ColorSpace().getLinearColorSpace();
    }
}