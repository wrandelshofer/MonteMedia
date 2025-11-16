/*
 * @(#)NamedColorSpaceAdapter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


import java.awt.color.ColorSpace;
import java.io.Serial;

/**
 * Implements conversions from/to linear RGB color space.
 * <p>
 * This class should give identical results as
 * {@code ColorSpace.getInstance(} {@link ColorSpace#CS_LINEAR_RGB}{@code );}
 * <p>
 * References:
 * <dl>
 *     <dt>A close look at the sRGB formula. Copyright Jason Summers.</dt><dd>
 *         <a href="https://entropymine.com/imageworsener/srgbformula/">
 *             entropymine.como</a>
 *     <dt>Color Conversion Algorithms.  Eugene Vishnevsky.</dt><dd>
 *         <a href="https://www.cs.rit.edu/~ncs/color/t_convert.html#RGB%20to%20XYZ%20&%20XYZ%20to%20RGB">
 *             www.cs.rit.edu</a>
 *     </dd>
 * </dl>
 */
public class NamedColorSpaceAdapter extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 0L;
    private final ColorSpace cs;
    private final String name;

    public NamedColorSpaceAdapter(String name, ColorSpace cs) {
        super(cs.getType(), cs.getNumComponents());
        this.cs = cs;
        this.name = name;
    }

    @Override
    public int getType() {
        return cs.getType();
    }

    @Override
    public int getNumComponents() {
        return cs.getNumComponents();
    }

    @Override
    public String getName(int component) {
        return cs.getName(component);
    }

    @Override
    public float getMinValue(int component) {
        return cs.getMinValue(component);
    }

    @Override
    public float getMaxValue(int component) {
        return cs.getMaxValue(component);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toRGB(float[] lrgb, float[] srgb) {
        float[] floats = cs.toRGB(lrgb);
        System.arraycopy(floats, 0, srgb, 0, floats.length);
        return srgb;
    }


    @Override
    public float[] fromRGB(float[] srgb, float[] lrgb) {
        float[] floats = cs.fromRGB(srgb);
        System.arraycopy(floats, 0, lrgb, 0, floats.length);
        return lrgb;
    }

    @Override
    public float[] toCIEXYZ(float[] lrgb, float[] xyz) {
        float[] floats = cs.toCIEXYZ(lrgb);
        System.arraycopy(floats, 0, xyz, 0, floats.length);
        return xyz;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lrgb) {
        float[] floats = cs.fromCIEXYZ(xyz);
        System.arraycopy(floats, 0, lrgb, 0, floats.length);
        return lrgb;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz) {
        return cs.fromCIEXYZ(xyz);
    }

    @Override
    public float[] toRGB(float[] lrgb) {
        return cs.toRGB(lrgb);
    }

    @Override
    public float[] fromRGB(float[] srgb) {
        return cs.fromRGB(srgb);
    }

    @Override
    public float[] toCIEXYZ(float[] lrgb) {
        return cs.toCIEXYZ(lrgb);
    }

}
