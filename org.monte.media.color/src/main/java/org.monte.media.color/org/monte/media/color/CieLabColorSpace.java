/*
 * @(#)CieLabColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;


import java.io.Serial;

/// The 1976 CIE `L*a*b*` color space (CIELAB).
///
/// The `L*` coordinate of an object
/// is the lightness intensity as measured on a scale from 0 to 100, where 0
/// represents black and 100 represents white.
///
/// The `a*` coordinate of an object
/// represents the position of the object’s color on a pure green and pure red
/// scale, where -127 represents pure green and +127 represents pure red.
///
/// The `b*` coordinate represents the position of the object’s color on a pure blue
/// and pure yellow scale, where -127 represents pure blue and +127 represents
/// pure yellow.
///
/// The `a*` and `b*` values are unbounded. CSS associates the range of ±125 to
/// the range of -100% to +100%.
///
/// The distance that can be calculated between two colors, is directly proportional to
/// the difference between the two colors as perceived by the human eye.
///
/// References:
///
/// CSS Color Module Level 4, §9.3 Specifying Lab and LCH: the lab() and lch() functional notations
/// : [csswg.org](https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#specifying-lab-lch)
///
public class CieLabColorSpace extends AbstractNamedColorSpace {

    public static CieLabColorSpace getInstance() {
        class Holder {
            private static final CieLabColorSpace INSTANCE = new CieLabColorSpace();
        }
        return Holder.INSTANCE;
    }


    @Serial
    private static final long serialVersionUID = 1L;

    /// The X coordinate of the reference white.
    final private double Xw;
    /// The Y coordinate of the reference white.
    final private double Yw;
    /// The Z coordinate of the reference white.
    final private double Zw;
    /// Epsilon
    private static final double eps = 216d / 24389d;
    private static final double k = 24389d / 27d;

    /// Creates a new instance with the  XYZ coordinates of the
    /// CIE Standard Illuminant D65 reference white.
    public CieLabColorSpace() {
        this(0.9505d, 1d, 1.0890d);
    }

    /// Creates a new instance with the given XYZ coordinates of the
    /// reference white.
    ///
    /// @param Xw X coordinate of the reference white
    /// @param Yw Y coordinate of the reference white
    /// @param Zw Z coordinate of the reference white
    public CieLabColorSpace(double Xw, double Yw, double Zw) {
        super(TYPE_Lab, 3);

        this.Xw = Xw;
        this.Yw = Yw;
        this.Zw = Zw;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return SrgbColorSpace.getInstance().fromCIEXYZ(toCIEXYZ(colorvalue, rgb), rgb);
    }

    @Override
    public int getEquivalentBuiltInColorSpace() {
        return -1;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] component) {
        return fromCIEXYZ(SrgbColorSpace.getInstance().toCIEXYZ(rgb, component), component);
    }

    /// Lab to XYZ.
    /// ```
    /// X = xr*Xw;
    /// Y = yr*Yw;
    /// Z = zr*Zw;
    /// ``` where
    /// ```
    /// xr = fx^3, if fx^3 &gt; eps
    ///    = (116*fx - 16)/k, if fx^3 &lt;= eps
    ///
    /// yr = ((L+16)/116)^3, if L &gt; k*eps
    ///    = L/k, if L &lt;= k*eps
    ///
    /// zr = fz^3, if fz^3 &gt; eps
    ///    = (116*fz - 16)/k, if fz^3 &lt;= eps
    ///
    /// fx = a/500+fy
    ///
    /// fz = fy - b / 200
    ///
    /// fy = (L+16)/116
    ///
    /// eps = 216/24389
    /// k = 24389/27
    /// ```
    ///
    /// Source: <a href="http://www.brucelindbloom.com/index.html?Equations.html"
    /// >http://www.brucelindbloom.com/index.html?Equations.html</a>
    ///
    /// @param colorvalue Lab color value.
    /// @return CIEXYZ color value.
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        double L = colorvalue[0];
        double a = colorvalue[1];
        double b = colorvalue[2];
        return toCIEXYZ(L, a, b, xyz);
    }

    protected float[] toCIEXYZ(double L, double a, double b, float[] xyz) {
        double fy = (L + 16d) / 116d;
        double fx = a / 500d + fy;
        double fz = fy - b / 200d;
        double xr, yr, zr;
        double fxp3 = fx * fx * fx;
        if (fxp3 > eps) {
            xr = fxp3;
        } else {
            xr = (116d * fx - 16d) / k;
        }
        if (L > k * eps) {
            yr = ((L + 16d) / 116d);
            yr = yr * yr * yr;
        } else {
            yr = L / k;
        }

        double fzp3 = fz * fz * fz;
        if (fzp3 > eps) {
            zr = fzp3;
        } else {
            zr = (116d * fz - 16d) / k;
        }

        double X = xr * Xw;
        double Y = yr * Yw;
        double Z = zr * Zw;

        xyz[0] = (float) X;
        xyz[1] = (float) Y;
        xyz[2] = (float) Z;
        return xyz;
    }

    /// XYZ to Lab.
    /// ```
    /// L = 116*fy - 16
    /// a = 500 * (fx - fy)
    /// b = 200 * (fy - fz)
    /// ``` where
    /// ```
    /// fx = xr^(1/3), if xr &gt; eps
    ///    = (k*xr + 16) / 116 if xr &lt;= eps
    ///
    /// fy = yr^(1/3), if yr &gt; eps
    ///    = (k*yr + 16) / 116 if yr &lt;= eps
    ///
    /// fz = zr^(1/3), if zr &gt; eps
    ///    = (k*zr + 16) / 116 if zr &lt;= eps
    ///
    /// xr = X / Xw
    /// yr = Y / Yw
    /// zr = Z / Zw
    ///
    /// eps = 216/24389
    /// k = 24389/27
    /// ```
    ///
    /// Source: <a href="http://www.brucelindbloom.com/index.html?Equations.html"
    /// >http://www.brucelindbloom.com/index.html?Equations.html</a>
    ///
    /// @param xyz CIEXYZ color value.
    /// @return Lab color value.
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] component) {
        double X = xyz[0];
        double Y = xyz[1];
        double Z = xyz[2];

        double xr = X / Xw;
        double yr = Y / Yw;
        double zr = Z / Zw;

        double fx, fy, fz;
        if (xr > eps) {
            fx = Math.pow(xr, 1d / 3d);
        } else {
            fx = (k * xr + 16d) / 116d;
        }
        if (yr > eps) {
            fy = Math.pow(yr, 1d / 3d);
        } else {
            fy = (k * yr + 16d) / 116d;
        }
        if (zr > eps) {
            fz = Math.pow(zr, 1d / 3d);
        } else {
            fz = (k * zr + 16) / 116;
        }

        double L = 116d * fy - 16;
        double a = 500d * (fx - fy);
        double b = 200d * (fy - fz);

        component[0] = (float) L;
        component[1] = (float) a;
        component[2] = (float) b;
        return component;
    }

    @Override
    public String getName() {
        return "CIE 1976 L*a*b*";
    }

    @Override
    public float getMinValue(int component) {
        return switch (component) {
            case 0 -> 0f;
            case 1, 2 -> -125f;
            default -> throw new IllegalArgumentException("Illegal component=" + component);
        };
    }

    @Override
    public float getMaxValue(int component) {
        return switch (component) {
            case 0 -> 100f;
            case 1, 2 -> 125f;
            default -> throw new IllegalArgumentException("Illegal component=" + component);
        };
    }
}
