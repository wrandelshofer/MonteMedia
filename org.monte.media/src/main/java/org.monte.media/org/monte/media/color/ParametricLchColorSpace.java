/*
 * @(#)ParametricLchColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.io.Serial;

import static java.lang.Math.PI;

/**
 * A parametric LCH color space based on a LAB color space.
 * <p>
 * The {@code L*} coordinate of an object is the lightness intensity as measured on a
 * scale from 0 to 100, where 0 represents black and 100 represents white.
 * <p>
 * The {@code C} and {@code H} coordinates are projections of the
 * {@code a*} and {@code b*} colors of the CIE
 * {@code L*a*b*} color space into polar coordinates.
 * <pre>
 * a = C * cos(H)
 * b = C * sin(H)
 * </pre>
 *
 */
public class ParametricLchColorSpace extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 1L;
    private final NamedColorSpace labColorSpace;
    private final String name;

    public ParametricLchColorSpace(String name, NamedColorSpace labColorSpace) {
        super(TYPE_LCH, 3);
        assert (labColorSpace.getType() == TYPE_Lab);
        this.name = name;
        this.labColorSpace = labColorSpace;
    }

    /**
     * LCH to XYZ.
     *
     * @param colorvalue LCH color value.
     * @return CIEXYZ color value.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return labColorSpace.toCIEXYZ(lchToLab(colorvalue, xyz), xyz);
    }

    protected float[] lchToLab(float[] lch, float[] lab) {
        double L = lch[0];
        double C = lch[1];
        double H = lch[2] * PI / 180;

        double a = C * Math.cos(H);
        double b = C * Math.sin(H);
        lab[0] = (float) L;
        lab[1] = (float) a;
        lab[2] = (float) b;
        return lab;
    }

    /**
     * XYZ to LCH.
     *
     * @param lch CIEXYZ color value.
     * @return LCH color value.
     */
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lch) {
        return labToLch(labColorSpace.fromCIEXYZ(xyz, lch), lch);
    }


    protected float[] labToLch(float[] lab, float[] lch) {
        double L = lab[0];
        double a = lab[1];
        double b = lab[2];

        double C = Math.sqrt(a * a + b * b);
        double H = Math.atan2(b, a);
        if (H < 0) {
            H += Math.PI * 2;
        }

        lch[0] = (float) L;
        lch[1] = (float) C;
        lch[2] = (float) (H * 180 / PI);

        return lch;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toRGB(float[] lch, float[] rgb) {
        return labColorSpace.toRGB(lchToLab(lch, rgb), rgb);
    }

    @Override
    public float getMaxValue(int component) {
        return switch (component) {
            case 0, 1 -> labColorSpace.getMaxValue(component);
            case 2 -> 360f;
            default -> throw new IllegalArgumentException("component:" + component);
        };
    }


    @Override
    public float[] fromRGB(float[] rgb, float[] lch) {
        return labToLch(labColorSpace.fromRGB(rgb, lch), lch);
    }
}
