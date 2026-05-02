/*
 * @(#)ParametricIccColorSpace.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class ParametricIccColorSpace extends AbstractNamedColorSpace {
    public ParametricIccColorSpace(int type, int numcomponents) {
        super(type, numcomponents);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return new float[0];
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return new float[0];
    }
}
