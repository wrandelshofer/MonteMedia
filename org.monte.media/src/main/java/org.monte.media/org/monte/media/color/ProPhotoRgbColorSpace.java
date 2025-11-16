/*
 * @(#)ProPhotoRgbColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.monte.media.math.Point2D;

import static org.monte.media.color.ParametricLinearRgbColorSpace.ILLUMINANT_D50_XYZ;

/**
 * ProPhoto RGB Color Space.
 * <p>
 * The ProPhoto RGB color space, also known as ROMM RGB (Reference Output Medium Metric), is an output referred RGB
 * color space developed by Kodak. It offers an especially large gamut designed for use with photographic output in
 * mind.
 * <p>
 * References:
 * <dl>
 *     <dt>Wikipedia: ProPhoto RGB color space.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/ProPhoto_RGB_color_space">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. The Predefined ProPhoto RGB Color Space: the prophoto-rgb keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-prophoto-rgb">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class ProPhotoRgbColorSpace extends ParametricNonLinearRgbColorSpace {
    public final static ProPhotoRgbColorSpace INSTANCE = new ProPhotoRgbColorSpace();

    public ProPhotoRgbColorSpace() {
        super("ProPhoto RGB", new ParametricLinearRgbColorSpace("Linear ProPhoto RGB",
                        new Point2D(0.734699, 0.265301),
                        new Point2D(0.159597, 0.840403),
                        new Point2D(0.036598, 0.000105),
                        ILLUMINANT_D50_XYZ
                ),
                ProPhotoRgbColorSpace::toLinear,
                ProPhotoRgbColorSpace::fromLinear
        );
    }

    /**
     * Convert an array of linear-light prophoto-rgb  in the range 0.0-1.0
     * to gamma corrected form.
     * Transfer curve is gamma 1.8 with a small linear portion.
     */
    public static float fromLinear(float cl) {
        float E = 1 / 512f;
        float sign = Math.signum(cl);
        float abs = Math.abs(cl);

        float c;
        if (abs < E) {
            c = cl * 16f;
        } else {
            c = sign * (float) Math.pow(abs, 1 / 1.8);
        }
        return c;
    }

    /**
     * Convert an array of prophoto-rgb values
     * where in-gamut colors are in the range [0.0 - 1.0]
     * to linear light (un-companded) form.
     * Transfer curve is gamma 1.8 with a small linear portion.
     * Extended transfer function.
     */
    public static float toLinear(float c) {
        float E = 16 / 512f;
        float sign = Math.signum(c);
        float abs = Math.abs(c);

        float cl;
        if (abs <= E) {
            cl = c / 16f;
        } else {
            cl = sign * (float) Math.pow(abs, 1.8);
        }
        return cl;
    }
}
