/*
 * @(#)NamedColorSpaceAdapter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


import java.awt.color.ColorSpace;
import java.io.Serial;

/// Implements conversions from/to linear RGB color space.
///
/// This class should give identical results as
/// `ColorSpace.getInstance(` [#CS_LINEAR_RGB]`);`
///
/// References:
/// <dl>
///     <dt>A close look at the sRGB formula. Copyright Jason Summers.</dt><dd>
///         [
///             entropymine.como](https://entropymine.com/imageworsener/srgbformula/)
///     <dt>Color Conversion Algorithms.  Eugene Vishnevsky.</dt><dd>
///         [
///             www.cs.rit.edu](https://www.cs.rit.edu/~ncs/color/t_convert.html#RGB%20to%20XYZ%20&%20XYZ%20to%20RGB)
///     </dd>
/// </dl>
public class NamedColorSpaceAdapter extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 0L;
    private final ColorSpace cs;
    private final String name;
    private final int equivalentBuiltInColorSpace;

    public NamedColorSpaceAdapter(String name, ColorSpace cs, int equivalentBuiltInColorSpace) {
        super(cs.getType(), cs.getNumComponents());
        this.cs = cs;
        this.name = name;
        this.equivalentBuiltInColorSpace = equivalentBuiltInColorSpace;
    }

    @Override
    public int getEquivalentBuiltInColorSpace() {
        return equivalentBuiltInColorSpace;
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
