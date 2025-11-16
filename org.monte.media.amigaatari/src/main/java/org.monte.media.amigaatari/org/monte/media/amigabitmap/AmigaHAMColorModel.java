/*
 * @(#)AmigaHAMColorModel.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

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
/// Color selection in hold-and-modify mode with 8 bitplanes:
///
/// | Bitplane 8 | Bitplane 7 | Result                                        |
/// |------------|------------|-----------------------------------------------|
/// | 0          | 0          | Select palette color, index = Bitplane 6-1    |
/// | 0          | 1          | Hold and modify, blue bits 8-3 = Bitplane 6-1 |
/// | 1          | 0          | Hold and modify, red bits 8-3 = Bitplane 6-1  |
/// | 1          | 1          | Hold and modify, green bits 8-3 = Bitplane 6-1|
///
/// For more details refer to "Amiga Hardware Reference, 3rd Edition. Addison Wesley".
///
/// @author Werner Randelshofer
public class AmigaHAMColorModel extends IndexColorModel {
    public enum Type {
        HAM6, HAM8
    }

    private final Type type;

    public AmigaHAMColorModel(Type type, int[] cmap) {
        super(type == Type.HAM6 ? 6 : 8, type == Type.HAM6 ? 16 : 64, fixColorMap(type, cmap), 0, false, -1, DataBuffer.TYPE_BYTE);
        this.type = type;
    }

    public AmigaHAMColorModel(Type type, byte[] r, byte[] g, byte[] b) {
        this(type, fixColorMap(type, r, g, b));
    }

    public AmigaHAMColorModel(Type type, IndexColorModel icm) {
        this(type, fixColorMap(type, icm));
    }

    private static int[] fixColorMap(Type type, IndexColorModel icm) {
        int[] rgbs = new int[icm.getMapSize()];
        icm.getRGBs(rgbs);
        return fixColorMap(type, rgbs);
    }

    private static int[] fixColorMap(Type type, int[] rgbs) {
        rgbs = Arrays.copyOf(rgbs, type == Type.HAM6 ? 16 : 64);
        if (type == Type.HAM6) {
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = (rgbs[i] & 0xf0f0f0) | ((rgbs[i] & 0xf0f0f0) >>> 4);
            }
        }
        return rgbs;
    }

    private static int[] fixColorMap(Type type, byte[] r, byte[] g, byte[] b) {
        int[] rgbs;
        if (type == Type.HAM6) {
            rgbs = new int[16];
            for (int i = 0; i < Math.min(rgbs.length, r.length); i++) {
                rgbs[i] = ((r[i] & 0xf0) << 16) | ((r[i] & 0xf0) << 12)
                        | ((g[i] & 0xf0) << 8) | ((g[i] & 0xf0) << 4)
                        | ((b[i] & 0xf0)) | ((b[i] & 0xf0) >>> 4);
            }
        } else {
            rgbs = new int[64];
            for (int i = 0; i < Math.min(rgbs.length, r.length); i++) {
                rgbs[i] = ((r[i] & 0xff) << 16)
                        | ((g[i] & 0xff) << 8)
                        | ((b[i] & 0xff));
            }
        }
        return rgbs;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AmigaHAMColorModel{"
                + "type=" + type
                + " pixelBits = " + pixel_bits
                + " numComponents = " + getNumComponents()
                + " color space = " + getColorSpace()
                + " transparency = " + getTransparency()
                + " transIndex   = " + getTransparentPixel()
                + " has alpha = " + hasAlpha()
                + " isAlphaPre = " + isAlphaPremultiplied()
                + '}';
    }
}
