/*
 * @(#)OKLchColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

public class OKLchColorSpace extends ParametricLchColorSpace {
    public final static OKLchColorSpace INSTANCE = new OKLchColorSpace();

    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }
}
