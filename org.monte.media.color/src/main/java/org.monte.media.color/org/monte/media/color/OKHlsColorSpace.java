/*
 * @(#)OKHlsColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


import java.awt.color.ColorSpace;

/// The OK HLS Color Space.
///
/// Components:
/// <dl>
///     <dt>hue</dt><dd>0 to 360 degrees</dd>
///     <dt>lightness</dt><dd>0 to 1 percentage</dd>
///     <dt>saturation</dt><dd>0 to 1 percentage</dd>
/// </dl>
///
/// References:
/// <dl>
///     <dt>Börn Ottosson, Okhsv and Okhsl. Two new color spaces for color picking.
///     [MIT License](https://github.com/bottosson/bottosson.github.io/blob/3d3f17644d7f346e1ce1ca08eb8b01782eea97af/misc/colorpicker/License.txt)</dt>
///     <dd>[github.io](https://bottosson.github.io/posts/colorpicker/)</dd>
/// </dl>
/// <dl>
///     <dt>Börn Ottosson, ok_color.h/dt>
///     [MIT License](https://github.com/bottosson/bottosson.github.io/blob/3d3f17644d7f346e1ce1ca08eb8b01782eea97af/misc/colorpicker/License.txt)</dt>
///     <dd>[github.io](http://bottosson.github.io/misc/ok_color.h)</dd>
/// </dl>
public class OKHlsColorSpace extends AbstractNamedColorSpace {
    public static OKHlsColorSpace getInstance() {
        class Holder {
            private static final OKHlsColorSpace INSTANCE = new OKHlsColorSpace();
        }
        return Holder.INSTANCE;
    }

    @Override
    public int getEquivalentBuiltInColorSpace() {
        return -1;
    }

    private final static OKLabColorSpace oklab = new OKLabColorSpace();

    public OKHlsColorSpace() {
        super(ColorSpace.TYPE_HLS, 3);
    }

    @Override
    public String getName() {
        return "OKHLS";
    }

    @Override
    public float[] toRGB(float[] hls, float[] rgb) {
        double h = hls[0];
        double l = hls[1];
        double s = hls[2];

        if (l == 1.0) {
            rgb[0] = 1;
            rgb[1] = 1;
            rgb[2] = 1;
            return rgb;
        } else if (l == 0.0) {
            rgb[0] = 0;
            rgb[1] = 0;
            rgb[2] = 0;
            return rgb;
        }

        double a_ = Math.cos(h * Math.PI / 180.0);
        double b_ = Math.sin(h * Math.PI / 180.0);
        double L = toe_inv(l);

        Cs cs = get_Cs(L, a_, b_);
        double C_0 = cs.C_0;
        double C_mid = cs.C_mid;
        double C_max = cs.C_max;

        double mid = 0.8;
        double mid_inv = 1.25;

        double C, t, k_0, k_1, k_2;

        if (s < mid) {
            t = mid_inv * s;

            k_1 = mid * C_0;
            k_2 = (1.0 - k_1 / C_mid);

            C = t * k_1 / (1.0 - k_2 * t);
        } else {
            t = (s - mid) / (1 - mid);

            k_0 = C_mid;
            k_1 = (1.0 - mid) * C_mid * C_mid * mid_inv * mid_inv / C_0;
            k_2 = (1.0 - (k_1) / (C_max - C_mid));

            C = k_0 + t * k_1 / (1.0 - k_2 * t);
        }

        float[] lab = rgb;
        lab[0] = (float) L;
        lab[1] = (float) (C * a_);
        lab[2] = (float) (C * b_);
        return oklab.toRGB(lab, rgb);
    }

    private static double toe_inv(double x) {
        double k_1 = 0.206;
        double k_2 = 0.03;
        double k_3 = (1.0 + k_1) / (1.0 + k_2);
        return (x * x + k_1 * x) / (k_3 * (x + k_2));
    }

    record Cs(double C_0, double C_mid, double C_max) {
    }


    /// Alternative representation of (L_cusp, C_cusp)
    /// Encoded so S = C_cusp/L_cusp and T = C_cusp/(1-L_cusp)
    /// The maximum value for C in the triangle is then found as fmin(S*L, T*(1-L)), for a given L
    record ST(double S, double T) {
    }


    private static Cs get_Cs(double L, double a_, double b_) {
        LC cusp = find_cusp(a_, b_);

        double C_max = find_gamut_intersection(a_, b_, L, 1, L, cusp);
        ST ST_max = to_ST(cusp);

        // Scale factor to compensate for the curved part of gamut shape:
        double k = C_max / Math.min((L * ST_max.S), (1 - L) * ST_max.T);

        double C_mid;
        {
            ST ST_mid = get_ST_mid(a_, b_);

            // Use a soft minimum function, instead of a sharp triangle shape to get a smooth value for chroma.
            double C_a = L * ST_mid.S;
            double C_b = (1.0 - L) * ST_mid.T;
            C_mid = 0.9 * k * Math.sqrt(Math.sqrt(1.0 / (1.0 / (C_a * C_a * C_a * C_a) + 1.0 / (C_b * C_b * C_b * C_b))));
        }

        double C_0;
        {
            // for C_0, the shape is independent of hue, so ST are constant. Values picked to roughly be the average values of ST.
            double C_a = L * 0.4;
            double C_b = (1.0 - L) * 0.8;

            // Use a soft minimum function, instead of a sharp triangle shape to get a smooth value for chroma.
            C_0 = Math.sqrt(1.0 / (1.0 / (C_a * C_a) + 1.0 / (C_b * C_b)));
        }

        return new Cs(C_0, C_mid, C_max);
    }

    private static ST to_ST(LC cusp) {
        double L = cusp.L;
        double C = cusp.C;
        return new ST(C / L, C / (1 - L));
    }

    record LC(double L, double C) {
    }


    /// finds L_cusp and C_cusp for a given hue
    /// a and b must be normalized so a^2 + b^2 == 1
    private static LC find_cusp(double a, double b) {
        // First, find the maximum saturation (saturation S = C/L)
        double S_cusp = compute_max_saturation(a, b);

        // Convert to linear sRGB to find the first point where at least one of r,g or b >= 1:
        float[] rgb_at_max = oklab.toLinearRGB(new float[]{1f, (float) (S_cusp * a), (float) (S_cusp * b)}, new float[3]);
        double L_cusp = Math.cbrt(1.0 / Math.max(Math.max(rgb_at_max[0], rgb_at_max[1]), rgb_at_max[2]));
        double C_cusp = L_cusp * S_cusp;

        return new LC(L_cusp, C_cusp);
    }

    /// Finds the maximum saturation possible for a given hue that fits in sRGB
    /// Saturation here is defined as S = C/L
    /// a and b must be normalized so a^2 + b^2 == 1
    private static double compute_max_saturation(double a, double b) {
        // Max saturation will be when one of r, g or b goes below zero.

        // Select different coefficients depending on which component goes below zero first
        double k0, k1, k2, k3, k4, wl, wm, ws;

        if (-1.88170328 * a - 0.80936493 * b > 1) {
            // Red component
            k0 = 1.19086277;
            k1 = 1.76576728;
            k2 = 0.59662641;
            k3 = 0.75515197;
            k4 = 0.56771245;
            wl = 4.0767416621;
            wm = -3.3077115913;
            ws = 0.2309699292;
        } else if (1.81444104 * a - 1.19445276 * b > 1) {
            // Green component
            k0 = 0.73956515;
            k1 = -0.45954404;
            k2 = 0.08285427;
            k3 = 0.12541070;
            k4 = 0.14503204;
            wl = -1.2684380046;
            wm = 2.6097574011;
            ws = -0.3413193965;
        } else {
            // Blue component
            k0 = 1.35733652;
            k1 = -0.00915799;
            k2 = -1.15130210;
            k3 = -0.50559606;
            k4 = 0.00692167;
            wl = -0.0041960863;
            wm = -0.7034186147;
            ws = 1.7076147010;
        }

        // Approximate max saturation using a polynomial:
        double S = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;

        // Do one-step Halley's method to get closer
        // this gives an error less than 10e6, except for some blue hues where the dS/dh is close to infinite
        // this should be sufficient for most applications, otherwise do two/three steps

        double k_l = 0.3963377774 * a + 0.2158037573 * b;
        double k_m = -0.1055613458 * a - 0.0638541728 * b;
        double k_s = -0.0894841775 * a - 1.2914855480 * b;

        {
            double l_ = 1.0 + S * k_l;
            double m_ = 1.0 + S * k_m;
            double s_ = 1.0 + S * k_s;

            double l = l_ * l_ * l_;
            double m = m_ * m_ * m_;
            double s = s_ * s_ * s_;

            double l_dS = 3.0 * k_l * l_ * l_;
            double m_dS = 3.0 * k_m * m_ * m_;
            double s_dS = 3.0 * k_s * s_ * s_;

            double l_dS2 = 6.0 * k_l * k_l * l_;
            double m_dS2 = 6.0 * k_m * k_m * m_;
            double s_dS2 = 6.0 * k_s * k_s * s_;

            double f = wl * l + wm * m + ws * s;
            double f1 = wl * l_dS + wm * m_dS + ws * s_dS;
            double f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;

            S = S - f * f1 / (f1 * f1 - 0.5 * f * f2);
        }

        return S;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public float[] fromRGB(float[] rgb, float[] hls) {
        float[] lab = oklab.fromRGB(rgb, hls);
        double labL = lab[0];
        double laba = lab[1];
        double labb = lab[2];

        double C = Math.sqrt(laba * laba + labb * labb);
        double a_ = laba / C;
        double b_ = labb / C;

        double L = labL;
        double h = 180 + 180 * Math.atan2(-labb, -laba) / Math.PI;

        Cs cs = get_Cs(L, a_, b_);
        double C_0 = cs.C_0;
        double C_mid = cs.C_mid;
        double C_max = cs.C_max;

        // Inverse of the interpolation in okhsl_to_srgb:

        double mid = 0.8;
        double mid_inv = 1.25;

        double s;
        if (C < C_mid) {
            double k_1 = mid * C_0;
            double k_2 = (1.0 - k_1 / C_mid);

            double t = C / (k_1 + k_2 * C);
            s = t * mid;
        } else {
            double k_0 = C_mid;
            double k_1 = (1.0 - mid) * C_mid * C_mid * mid_inv * mid_inv / C_0;
            double k_2 = (1.0 - (k_1) / (C_max - C_mid));

            double t = (C - k_0) / (k_1 + k_2 * (C - k_0));
            s = mid + (1.0 - mid) * t;
        }

        double l = toe(L);
        hls[0] = (float) h;
        hls[1] = (float) l;
        hls[2] = (float) s;
        return hls;

    }

    /// Finds intersection of the line defined by
    /// L = L0 * (1 - t) + t * L1;
    /// C = t * C1;
    /// a and b must be normalized so a^2 + b^2 == 1
    private static double find_gamut_intersection(double a, double b, double L1, double C1, double L0, LC cusp) {
        // Find the intersection for upper and lower half seprately
        double t;
        if (((L1 - L0) * cusp.C - (cusp.L - L0) * C1) <= 0.0) {
            // Lower half

            t = cusp.C * L0 / (C1 * cusp.L + cusp.C * (L0 - L1));
        } else {
            // Upper half

            // First intersect with triangle
            t = cusp.C * (L0 - 1.0) / (C1 * (cusp.L - 1.0) + cusp.C * (L0 - L1));

            // Then one-step Halley's method
            {
                double dL = L1 - L0;
                double dC = C1;

                double k_l = 0.3963377774 * a + 0.2158037573 * b;
                double k_m = -0.1055613458 * a - 0.0638541728 * b;
                double k_s = -0.0894841775 * a - 1.2914855480 * b;

                double l_dt = dL + dC * k_l;
                double m_dt = dL + dC * k_m;
                double s_dt = dL + dC * k_s;


                // If higher accuracy is required, 2 or 3 iterations of the following block can be used:
                {
                    double L = L0 * (1.0 - t) + t * L1;
                    double C = t * C1;

                    double l_ = L + C * k_l;
                    double m_ = L + C * k_m;
                    double s_ = L + C * k_s;

                    double l = l_ * l_ * l_;
                    double m = m_ * m_ * m_;
                    double s = s_ * s_ * s_;

                    double ldt = 3 * l_dt * l_ * l_;
                    double mdt = 3 * m_dt * m_ * m_;
                    double sdt = 3 * s_dt * s_ * s_;

                    double ldt2 = 6 * l_dt * l_dt * l_;
                    double mdt2 = 6 * m_dt * m_dt * m_;
                    double sdt2 = 6 * s_dt * s_dt * s_;

                    double r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s - 1;
                    double r1 = 4.0767416621 * ldt - 3.3077115913 * mdt + 0.2309699292 * sdt;
                    double r2 = 4.0767416621 * ldt2 - 3.3077115913 * mdt2 + 0.2309699292 * sdt2;

                    double u_r = r1 / (r1 * r1 - 0.5 * r * r2);
                    double t_r = -r * u_r;

                    double g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s - 1;
                    double g1 = -1.2684380046 * ldt + 2.6097574011 * mdt - 0.3413193965 * sdt;
                    double g2 = -1.2684380046 * ldt2 + 2.6097574011 * mdt2 - 0.3413193965 * sdt2;

                    double u_g = g1 / (g1 * g1 - 0.5 * g * g2);
                    double t_g = -g * u_g;

                    double b0 = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s - 1;
                    double b1 = -0.0041960863 * ldt - 0.7034186147 * mdt + 1.7076147010 * sdt;
                    double b2 = -0.0041960863 * ldt2 - 0.7034186147 * mdt2 + 1.7076147010 * sdt2;

                    double u_b = b1 / (b1 * b1 - 0.5 * b0 * b2);
                    double t_b = -b0 * u_b;

                    t_r = u_r >= 0.0 ? t_r : Float.MAX_VALUE;
                    t_g = u_g >= 0.0 ? t_g : Float.MAX_VALUE;
                    t_b = u_b >= 0.0 ? t_b : Float.MAX_VALUE;

                    t += Math.min(t_r, Math.min(t_g, t_b));
                }
            }
        }

        return t;
    }

    /// Returns a smooth approximation of the location of the cusp
    /// This polynomial was created by an optimization process
    /// It has been designed so that S_mid < S_max and T_mid < T_max
    private static ST get_ST_mid(double a_, double b_) {
        double S = 0.11516993 + 1.0 / (
                7.44778970 + 4.15901240 * b_
                        + a_ * (-2.19557347 + 1.75198401 * b_
                        + a_ * (-2.13704948 - 10.02301043 * b_
                        + a_ * (-4.24894561 + 5.38770819 * b_ + 4.69891013 * a_
                )))
        );

        double T = 0.11239642 + 1.0 / (
                1.61320320 - 0.68124379 * b_
                        + a_ * (0.40370612 + 0.90148123 * b_
                        + a_ * (-0.27087943 + 0.61223990 * b_
                        + a_ * (0.00299215 - 0.45399568 * b_ - 0.14661872 * a_
                )))
        );

        return new ST(S, T);
    }

    private static double toe(double x) {
        double k_1 = 0.206;
        double k_2 = 0.03;
        double k_3 = (1.0 + k_1) / (1.0 + k_2);
        return 0.5 * (k_3 * x - k_1 + Math.sqrt((k_3 * x - k_1) * (k_3 * x - k_1) + 4 * k_2 * k_3 * x));
    }

    @Override
    public float getMaxValue(int component) {
        return component == 0 ? 360 : 1;
    }
}
