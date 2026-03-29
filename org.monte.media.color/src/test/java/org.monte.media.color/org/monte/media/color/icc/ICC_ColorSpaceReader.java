/*
 * @(#)ICC_ColorSpaceReader.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import java.awt.color.ICC_ColorSpace;

public class ICC_ColorSpaceReader {
    private final ICC_ColorSpace cs;

    public ICC_ColorSpaceReader(ICC_ColorSpace cs) {
        this.cs = cs;
    }

    @Override
    public String toString() {
        return toString(cs);
    }

    public static String toString(ICC_ColorSpace cs) {
        StringBuilder sb = new StringBuilder();

        var black = cs.toRGB(new float[3]);
        var white = cs.toRGB(new float[]{1f, 1f, 1f});

        sb.append("ICC_ColorSpace {");
        sb.append(cs.toString());
        sb.append("}");
        return sb.toString();
    }
}
