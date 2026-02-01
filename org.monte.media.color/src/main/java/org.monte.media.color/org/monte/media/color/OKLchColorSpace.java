/*
 * @(#)OKLchColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

public class OKLchColorSpace extends ParametricLchColorSpace {
    public static OKLchColorSpace getInstance() {
        class Holder {
            private static final OKLchColorSpace INSTANCE = new OKLchColorSpace();
        }
        return Holder.INSTANCE;
    }


    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }
}
