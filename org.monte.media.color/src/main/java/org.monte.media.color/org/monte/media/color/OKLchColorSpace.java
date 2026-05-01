/*
 * @(#)OKLchColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

public class OKLchColorSpace {
    public static ParametricLchColorSpace getInstance() {
        class Holder {
            private static final ParametricLchColorSpace INSTANCE = new ParametricLchColorSpace(
                    "OKLCH", new OKLabColorSpace()
            );
        }
        return Holder.INSTANCE;
    }
}
