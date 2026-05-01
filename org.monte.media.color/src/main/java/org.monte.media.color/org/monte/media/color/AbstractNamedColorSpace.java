/*
 * @(#)AbstractNamedColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.awt.color.ColorSpace;
import java.io.Serial;
import java.util.Objects;

/// `AbstractNamedColorSpace`.
public abstract class AbstractNamedColorSpace extends ColorSpace implements NamedColorSpace {
    @Override
    public ColorSpace toColorSpace() {
        return this;
    }

    @Serial
    private static final long serialVersionUID = 1L;

    public AbstractNamedColorSpace(int type, int numcomponents) {
        super(type, numcomponents);
    }


    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        return fromCIEXYZ(colorvalue, colorvalue.length >= getNumComponents() ? colorvalue : new float[getNumComponents()]);// we are allowed to reuse this array
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        return toRGB(colorvalue, colorvalue.length >= 3 ? colorvalue : new float[3]);// we are allowed to reuse this array
    }

    @Override
    public float[] fromRGB(float[] rgb) {
        return fromRGB(rgb, rgb.length >= getNumComponents() ? rgb : new float[getNumComponents()]);// we are allowed to reuse this arrayto reuse this array
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return toCIEXYZ(colorvalue, colorvalue.length >= 3 ? colorvalue : new float[3]);// we are allowed to reuse this array
    }


    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return SrgbColorSpace.getInstance().toCIEXYZ(toRGB(colorvalue, xyz), xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromRGB(SrgbColorSpace.getInstance().fromCIEXYZ(xyz, colorvalue), colorvalue);
    }


    @Override
    public String toString() {
        return getName();
    }

    /// Lazy-initialized names of components in the color space.
    private transient volatile String[] compName;

    public String getName(int component) {
        Objects.checkIndex(component, getNumComponents());
        if (compName == null) {
            compName = switch (getType()) {
                case NamedColorSpace.TYPE_LCH -> new String[]{"Lightness", "Chroma", "Hue"};
                default -> {
                    String[] tmp = new String[getNumComponents()];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = super.getName(i);
                    }
                    yield tmp;
                }
            };
        }
        return compName[component];
    }
}
