/*
 * @(#)AmigaHAMColorModel.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.image.ColorModel;

/// ColorModel for images that use colors in hold-and-modify mode (HAM).
///
/// At the beginning of each scanline, the color output register is initialized
/// with color 0 of the color palette.
///
/// On each consecutive pixel,
/// the color output register can either be set to a color from the color palette,
/// or it can be held, and one of its components (red, green, or blue) are modified.
///
/// Hold-and-modify works with 6, or 8 bitplanes. The two most significand bitplanes
/// contain an op-code. The remaining bitplanes are used as an index into the color
/// palette, or as component values.
///
/// Color selection in hold-and-modify mode with 6 bitplanes:
///
/// | Bitplane 6 | Bitplane 5 | Result                                     |
/// |------------|------------|--------------------------------------------|
/// | 0          | 0          | Select palette color, index = Bitplane 4-1 |
/// | 0          | 1          | Hold and modify, blue = Bitplane 4-1       |
/// | 1          | 0          | Hold and modify, red = Bitplane 4-1        |
/// | 1          | 1          | Hold and modify, green = Bitplane 4-1      |
///
/// For more details refer to "Amiga Hardware Reference, 3rd Edition. Addison Wesley".
///
/// @author Werner Randelshofer
public class AmigaHAMColorModel extends ColorModel {
    //insert class definition here
    public final static int
            HAM6 = 6,
            HAM8 = 8;

    protected int HAMType;
    protected int map_size;
    protected boolean opaque;
    protected int[] rgb;
    protected boolean isOCS;

    /**
     * Creates a new HAM Color model using the specified base colors.
     *
     * @param aHAMType Type, must be HAM6 or HAM 8.
     * @param size     The size of the color palette.
     * @param r        The red colors as 8 bit or as 4 bit values.
     * @param g        The green colors as 8 bit or as 4 bit values.
     * @param b        The blue colors as 8 bit or as 4 bit values.
     * @param isOCS    Set this to true if the colors are 4 bit values.
     */
    public AmigaHAMColorModel(int aHAMType, int size, byte r[], byte g[], byte b[], boolean isOCS) {
        super(aHAMType);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }
        HAMType = aHAMType;
        this.isOCS = isOCS;
        if (isOCS) {
            byte[] r8 = new byte[size];
            byte[] g8 = new byte[size];
            byte[] b8 = new byte[size];
            for (int i = 0; i < size; i++) {
                r8[i] = (byte) (((r[i] & 0xf) << 4) | (r[i] & 0xf));
                g8[i] = (byte) (((g[i] & 0xf) << 4) | (g[i] & 0xf));
                b8[i] = (byte) (((b[i] & 0xf) << 4) | (b[i] & 0xf));
            }
            setRGBs(size, r8, g8, b8, null);
        } else {
            setRGBs(size, r, g, b, null);
        }
    }

    @Override
    public int getRed(int pixel) {
        return (rgb[pixel] >>> 12) & 0xff;
    }

    @Override
    public int getGreen(int pixel) {
        return (rgb[pixel] >>> 8) & 0xff;
    }

    @Override
    public int getBlue(int pixel) {
        return rgb[pixel] & 0xff;
    }

    @Override
    public int getAlpha(int pixel) {
        return rgb[pixel] >>> 24;
    }

    public boolean isOCS() {
        return isOCS;
    }

    /**
     * Creates a new HAM Color model using the specified base colors.
     *
     * @param aHAMType Type, must be HAM6 or HAM 8.
     * @param size     The size of the color palette.
     * @param rgb      The rgb colors.
     * @param isOCS    Set this to true if the colors are 12 bit precision only.
     */
    public AmigaHAMColorModel(int aHAMType, int size, int rgb[], boolean isOCS) {
        super(aHAMType);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }

        HAMType = aHAMType;
        if (isOCS) {
            byte[] r = new byte[rgb.length];
            byte[] g = new byte[rgb.length];
            byte[] b = new byte[rgb.length];
            for (int i = 0; i < rgb.length; i++) {
                r[i] = (byte) (((rgb[i] & 0xf00) >>> 8) |
                        (rgb[i] & 0xf00) >>> 4);
                g[i] = (byte) (((rgb[i] & 0xf0) >>> 4) |
                        (rgb[i] & 0xf0));
                b[i] = (byte) (((rgb[i] & 0xf)) |
                        (rgb[i] & 0xf) << 4);
            }
            setRGBs(size, r, g, b, null);
        } else {
            byte[] r = new byte[size];
            byte[] g = new byte[size];
            byte[] b = new byte[size];
            for (int i = 0; i < size; i++) {
                r[i] = (byte) ((rgb[i] & 0xff0000) >>> 16);
                g[i] = (byte) ((rgb[i] & 0xff00) >>> 8);
                b[i] = (byte) (rgb[i] & 0xff);
            }
            setRGBs(size, r, g, b, null);
        }
    }

    /**
     * Returns the HAM Type of this AmigaHAMColorModel: HAM8 or HAM6.
     */
    public int getHAMType() {
        return HAMType;
    }

    /**
     * Returns the number of planes required to represent
     * this AmigaHAMColorModel in a Bitmap.
     */
    public int getDepth() {
        return HAMType;
    }

    /**
     * Sets the HAM base colors.
     *
     * @param size The size of the color palette.
     * @param r    The red colors as 8 bit values.
     * @param g    The green colors as 8 bit values.
     * @param b    The blue colors as 8 bit values.
     * @param a    The alpha channels as 8 bit values.
     */
    protected void setRGBs(int size, byte r[], byte g[], byte b[], byte a[]) {
        if (size > 256) {
            throw new ArrayIndexOutOfBoundsException();
        }
        map_size = size;
        rgb = new int[256];
        int alpha = 0xff;
        opaque = true;
        for (int i = 0; i < size; i++) {
            if (a != null) {
                alpha = (a[i] & 0xff);
                if (alpha != 0xff) {
                    opaque = false;
                }
            }
            rgb[i] = (alpha << 24)
                    | ((r[i] & 0xff) << 16)
                    | ((g[i] & 0xff) << 8)
                    | (b[i] & 0xff);
        }
    }

    /**
     * Copies the array of red color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() are
     * written.
     */
    final public void getReds(byte r[]) {
        for (int i = 0; i < map_size; i++) {
            r[i] = (byte) (rgb[i] >> 16);
        }
    }

    /**
     * Copies the array of green color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() are
     * written.
     */
    final public void getGreens(byte g[]) {
        for (int i = 0; i < map_size; i++) {
            g[i] = (byte) (rgb[i] >> 8);
        }
    }

    /**
     * Copies the array of blue color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() will
     * be written.
     */
    final public void getBlues(byte b[]) {
        for (int i = 0; i < map_size; i++) {
            b[i] = (byte) rgb[i];
        }
    }

    /**
     * Copies the array of color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() will
     * be written.
     */
    final public void getRGBs(int rgbs[]) {
        for (int i = 0; i < map_size; i++) {
            rgbs[i] = rgb[i];
        }
    }

    /**
     * Returns the size of the color component arrays in this IndexColorModel.
     */
    final public int getMapSize() {
        return map_size;
    }
}
