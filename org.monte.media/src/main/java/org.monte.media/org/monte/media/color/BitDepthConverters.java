/*
 * @(#)RgbBitDepthConverter.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

public class BitDepthConverters {
    /**
     * Don't let anyone instantiate this class.
     */
    private BitDepthConverters() {

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
}
