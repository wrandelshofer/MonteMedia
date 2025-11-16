/*
 * @(#)RgbBitConverters.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

/**
 * Provides conversion functions between RGB bit representations.
 */
public class RgbBitConverters {
    private static final float EPSILON = 1f / 512;

    /**
     * Don't let anyone instantiate this class.
     */
    private RgbBitConverters() {

    }

    /**
     * Convert 12-bit RGB 444 to 24-bit RGB 888.
     * <pre>
     * RGB 444 BE:          . . . . . . . . . . . . R₃R₂R₁R₀G₃G₂G₁G₀B₃B₂B₁B₀
     * Expand to 24 bit:    R₃R₂R₁R₀. . . . G₃G₂G₁G₀. . . . B₃B₂B₁B₀. . . .
     * Replicate high bits: R₃R₂R₁R₀R₃R₂R₁R₀G₃G₂G₁G₀G₃G₂G₁G₀B₃B₂B₁B₀B₃B₂B₁B₀
     * </pre>
     *
     * @param v a RGB 555 value
     * @return the value converted to RGB 888
     */
    public static int rgb12to24(int v) {
        int e = (v & 0b1111_0000_0000) << 12
                | (v & 0b0000_1111_0000) << 8
                | (v & 0b0000_0000_1111) << 4;
        return e | (e >>> 4);
    }

    public static float[] rgb24ToRgbFloat(int rgb, float[] rgbf) {
        int r = (rgb >>> 16) & 0xff, g = (rgb >>> 8) & 0xff, b = rgb & 0xff;
        rgbf[0] = r / 255f;
        rgbf[1] = g / 255f;
        rgbf[2] = b / 255f;
        return rgbf;
    }

    /**
     * Convert 6-bit RGB 222 to 24-bit RGB 888.
     * <pre>
     * RGB 444 BE:          . . . . . . . . . . . . . . . . . . R₁R₀G₁G₀B₁B₀
     * Expand to 24 bit:    R₁R₀. . . . . . G₁G₀. . . . . . B₁B₀. . . . . .
     * Replicate high bits: R₁R₀R₁R₀R₁R₀R₁R₀G₁G₀G₁G₀G₁G₀G₁G₀B₁B₀B₁B₀B₁B₀B₁B₀
     * </pre>
     *
     * @param v a RGB 555 value
     * @return the value converted to RGB 888
     */
    public static int rgb6to24(int v) {
        int e = (v & 0b110000) << 18
                | (v & 0b001100) << 12
                | (v & 0b000011) << 6;
        return e | (e >>> 2) | (e >>> 4) | (e >>> 6);
    }

    /**
     * Convert 15-bit RGB 555 to 24-bit RGB 888.
     * <pre>
     * RGB 555 BE:          . . . . . . . . . R₄R₃R₂R₁R₀G₄G₃G₂G₁G₀B₄B₃B₂B₁B₀
     * Expand to 24 bit:    R₄R₃R₂R₁R₀. . . G₄G₃G₂G₁G₀. . . B₄B₃B₂B₁B₀. . .
     * Replicate high bits: R₄R₃R₂R₁R₀R₄R₃R₂G₄G₃G₂G₁G₀G₄G₃G₂B₄B₃B₂B₁B₀B₄B₃B₂
     * </pre>
     *
     * @param v a RGB 555 value
     * @return the value converted to RGB 888
     */
    public static int rgb15to24(int v) {
        int e = (v & 0b11111_00000_00000) << 9
                | (v & 0b00000_11111_00000) << 6
                | (v & 0b00000_00000_11111) << 3;
        return e | ((e & 0b11100000_11100000_11100000) >>> 5);
    }

    /**
     * Convert 16-bit RGB 565 to 24-bit RGB 888.
     * <pre>
     * RGB 555 BE:          . . . . . . . . R₄R₃R₂R₁R₀G₅G₄G₃G₂G₁G₀B₄B₃B₂B₁B₀
     * Expand to 24 bit:    R₄R₃R₂R₁R₀. . . G₅G₄G₃G₂G₁G₀. . B₄B₃B₂B₁B₀. . .
     * Replicate high bits: R₄R₃R₂R₁R₀R₄R₃R₂G₅G₄G₃G₂G₁G₀G₅G₄B₄B₃B₂B₁B₀B₄B₃B₂
     * </pre>
     *
     * @param v a RGB 555 value
     * @return the value converted to RGB 888
     */
    public static int rgb16to24(int v) {
        return (v & 0b11111_000000_00000) << 8 | (v & 0b11100_000000_00000) << 3 // red
                | (v & 0b00000_111111_00000) << 5 | (v & 0b110000_00000) >>> 1 // green
                | (v & 0b00000_000000_11111) << 3 | (v & 0b11100) >>> 2; // blue
    }

    /**
     * Convert 24-bit RGB 888 to 12-bit RGB 444.
     * <pre>
     * RGB 888 BE:          R₇R₆R₅R₄R₃R₂R₁R₀G₇G₆G₅G₄G₃G₂G₁G₀B₇B₆B₅B₄B₃B₂B₁B₀
     * Compress to 12 bit:  . . . . . . . . . . . . R₇R₆R₅R₄G₇G₆G₅G₄B₇B₆B₅B₄
     * </pre>
     *
     * @param v a RGB 888 value
     * @return the value converted to RGB 555
     */
    public static int rgb24to12(int v) {
        return (v & 0b11110000_00000000_00000000) >>> 12 // red
                | (v & 0b11110000_00000000) >>> 8  // green
                | (v & 0b11110000) >>> 4; // blue
    }

    /**
     * Convert 24-bit RGB 888 to 6-bit RGB 222.
     * <pre>
     * RGB 888 BE:          R₇R₆R₅R₄R₃R₂R₁R₀G₇G₆G₅G₄G₃G₂G₁G₀B₇B₆B₅B₄B₃B₂B₁B₀
     * Compress to 12 bit:  . . . . . . . . . . . . . . . . . . R₇R₆G₇G₆B₇B₆
     * </pre>
     *
     * @param v a RGB 888 value
     * @return the value converted to RGB 555
     */
    public static int rgb24to6(int v) {
        return (v & 0b11000000_00000000_00000000) >>> 18 // red
                | (v & 0b11000000_00000000) >>> 12  // green
                | (v & 0b11000000) >>> 6; // blue
    }

    /**
     * Convert 24-bit RGB 888 to 15-bit RGB 555.
     * <pre>
     * RGB 888 BE:          R₇R₆R₅R₄R₃R₂R₁R₀G₇G₆G₅G₄G₃G₂G₁G₀B₇B₆B₅B₄B₃B₂B₁B₀
     * Compress to 15 bit:  . . . . . . . . . R₇R₆R₅R₄R₃G₇G₆G₅G₄G₃B₇B₆B₅B₄B₃
     * </pre>
     *
     * @param v a RGB 888 value
     * @return the value converted to RGB 555
     */
    public static int rgb24to15(int v) {
        return (v & 0b11111000_00000000_00000000) >>> 9 // red
                | (v & 0b00000000_11111000_00000000) >>> 6  // green
                | (v & 0b00000000_00000000_11111000) >>> 3; // blue
    }

    /**
     * Convert 24-bit RGB 888 to 16-bit RGB 565.
     * <pre>
     * RGB 888 BE:          R₇R₆R₅R₄R₃R₂R₁R₀G₇G₆G₅G₄G₃G₂G₁G₀B₇B₆B₅B₄B₃B₂B₁B₀
     * Compress to 16 bit:  . . . . . . . . R₇R₆R₅R₄R₃G₇G₆G₅G₄G₃G₂B₇B₆B₅B₄B₃
     * </pre>
     *
     * @param v a RGB 888 value
     * @return the value converted to RGB 555
     */
    public static int rgb24to16(int v) {
        return (v & 0b11111000_00000000_00000000) >>> 8 // red
                | (v & 0b11111100_00000000) >>> 5  // green
                | (v & 0b11111000) >>> 3; // blue
    }

    /**
     * Converts from float to ARGB with 32 bit color depth.
     * The alpha value os 0xff if the float values are in range,
     * and 0x00 otherwise.
     *
     * @param rgb floating point RGB color values
     * @return 32-bit ARGB color value
     */
    public static int rgbFloatToArgb32(float[] rgb) {
        return rgbFloatToArgb32(rgb, 1f);
    }

    public static int rgbFloatToArgb32(float[] rgb, float alpha) {
        // If the color is not displayable in RGB, we return transparent black.
        /*
        if (rgb[0] < -EPSILON || rgb[1] < -EPSILON || rgb[2] < -EPSILON
                || rgb[0] > EPSILON + 1 || rgb[1] > EPSILON + 1 || rgb[2] > EPSILON + 1) {
            return 0;
        }*/
        return (Math.clamp((int) (alpha * 255), 0, 255) << 24) | rgbFloatToRgb24(rgb);
    }

    public static int rgbFloatToPreArgb32(float[] rgb, float alpha, float[] pre) {
        // If the color is not displayable in RGB, we return transparent black.
        /*
        if (rgb[0] < -EPSILON || rgb[1] < -EPSILON || rgb[2] < -EPSILON
                || rgb[0] > EPSILON + 1 || rgb[1] > EPSILON + 1 || rgb[2] > EPSILON + 1) {
            return 0;
        }*/
        alpha = Math.clamp(alpha, (float) 0, (float) 1);
        pre[0] = rgb[0] * alpha;
        pre[1] = rgb[1] * alpha;
        pre[2] = rgb[2] * alpha;
        return (Math.clamp((int) (alpha * 255), 0, 255) << 24) | rgbFloatToRgb24(pre);
    }

    /**
     * Converts from float to RGB with 24 bit color depth.
     *
     * @param rgb floating point RGB color values
     * @return 24-bit RGB color value
     */
    public static int rgbFloatToRgb24(float[] rgb) {
        return (Math.clamp((int) ((rgb[0] + 1f / 512) * 255f), 0, 255) << 16)
                | (Math.clamp((int) ((rgb[1] + 1f / 512) * 255f), 0, 255) << 8)
                | Math.clamp((int) ((rgb[2] + 1f / 512) * 255f), 0, 255);
    }

    /**
     * Converts from 32 bit ARGB to 32 bit pre-multiplied ARGB.
     *
     * @param argb an ARGB value
     * @return the corresponding pre-multiplied ARGB value
     */
    public static int argb32ToPreArgb32(int argb) {
        int alpha = argb >>> 24;
        int red = (argb >>> 16) & 0xff;
        int green = (argb >>> 8) & 0xff;
        int blue = argb & 0xff;
        int pred = (red * alpha >>> 8) + 1;
        int pgreen = (green * alpha >>> 8) + 1;
        int pblue = (blue * alpha >>> 8) + 1;
        return (alpha << 24) | (pred << 16) | (pgreen << 8) | pblue;
    }

}