/*
 * @(#)ColorModels.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.image.colormodel;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import static org.monte.media.util.MathUtil.clamp;

/// Utility methods for ColorModels.
///
/// @author Werner Randelshofer
public class ColorModels {

    /// Prevent instance creation.
    private ColorModels() {
    }

    /// The macintosh palette is arranged as follows: there are 256 colours to
    /// allocate, an even distribution of colors through the color cube might be
    /// desirable but 256 is not the cube of an integer. 6x6x6 is 216 and so the
    /// first 216 colors are an equal 6x6x6 sampling of the color cube.
    /// This leaves 40 colours to allocate, this has been done by choosing a ramp of
    /// 10 shades each for red, green, blue and grey.
    ///
    /// References:
    /// [http://paulbourke.net/texture_colour/colourramp/](http://paulbourke.net/texture_colour/colourramp/)
    ///
    /// @return The Macintosh color palette.
    public static IndexColorModel createMacColors() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        // Generate color cube with 216 colors
        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    r[index] = (byte) (255 - 51 * i);
                    g[index] = (byte) (255 - 51 * j);
                    b[index] = (byte) (255 - 51 * k);
                    index++;
                }
            }
        }

        index--; // overwrite last color (black) with color ramp

        // Generate red ramp
        byte[] ramp = {(byte) 238, (byte) 221, (byte) 187, (byte) 170, (byte) 136, (byte) 119, 85, 68, 34, 17};
        for (int i = 0; i < 10; i++) {
            r[index] = ramp[i];
            g[index] = (byte) (0);
            b[index] = (byte) (0);
            index++;
        }
        // Generate green ramp
        for (int j = 0; j < 10; j++) {
            r[index] = (byte) (0);
            g[index] = ramp[j];
            b[index] = (byte) (0);
            index++;
        }
        // Generate blue ramp
        for (int k = 0; k < 10; k++) {
            r[index] = (byte) (0);
            g[index] = (byte) (0);
            b[index] = ramp[k];
            index++;
        }
        // Generate gray ramp
        for (int ijk = 0; ijk < 10; ijk++) {
            r[index] = ramp[ijk];
            g[index] = ramp[ijk];
            b[index] = ramp[ijk];
            index++;
        }
        // last color is black (nothing to do)

        /*
        for (int i=0;i<256;i++) {
        if (i%6==0) System.out.println(); else System.out.print("  ");
        System.out.print(Integer.toHexString(r[i]&0xff)+","+Integer.toHexString(g[i]&0xff)+","+Integer.toHexString(b[i]&0xff));
        }*/

        return new IndexColorModel(8, 256, r, g, b);
    }

    /// Gray colors from dark to bright
    ///
    /// @param pixelSize should be 2, 4, or 8 (or at least enough to fit the number of colors)
    /// @param numColors number of bits 2=4, 4=16, or 8=256
    /// @return IndexColorModel
    public static IndexColorModel createGrayColorsDarkToBright(int pixelSize, int numColors) {
        byte[] r = new byte[numColors];

        // Generate gray ramp
        int step = numColors == 1 ? 255 : 255 / (numColors - 1);
        for (int i = 0; i < numColors; i++) {
            r[i] = (byte) (i * step);
        }
        return new IndexColorModel(pixelSize, numColors, r, r, r, Transparency.OPAQUE);
    }

    /// Gray colors from bright to dark
    ///
    /// @param pixelSize should be 2, 4, or 8 (or at least enough to fit the number of colors)
    /// @param numColors number of bits 2=4, 4=16, or 8=256
    /// @return IndexColorModel
    public static IndexColorModel createGrayColorsBrightToDark(int pixelSize, int numColors) {
        byte[] r = new byte[numColors];

        // Generate gray ramp
        int step = numColors == 1 ? 255 : 255 / (numColors - 1);
        for (int i = 0; i < numColors; i++) {
            r[numColors - i - 1] = (byte) (i * step);
        }
        return new IndexColorModel(pixelSize, numColors, r, r, r, Transparency.OPAQUE);
    }

    /// Returns a descriptive string for the provided color model.
    public static String toString(ColorModel cm) {
        StringBuilder buf = new StringBuilder();
        if (cm instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel) cm;
            buf.append("Direct Color Model ");

            int[] masks = dcm.getMasks();
            int totalBits = 0;
            MaskEntry[] entries = new MaskEntry[masks.length];
            for (int i = 0; i < masks.length; i++) {
                switch (i) {
                    case 0:
                        entries[i] = new MaskEntry(masks[i], "R");
                        break;
                    case 1:
                        entries[i] = new MaskEntry(masks[i], "G");
                        break;
                    case 2:
                        entries[i] = new MaskEntry(masks[i], "B");
                        break;
                    case 3:
                        entries[i] = new MaskEntry(masks[i], "A");
                        break;
                }
                totalBits += entries[i].getBits();
            }
            buf.append(totalBits);
            buf.append(" Bit ");
            Arrays.sort(entries);
            for (MaskEntry entry : entries) {
                buf.append(entry);
            }
        } else if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;
            buf.append("Index Color Model ");
            int mapSize = icm.getMapSize();
            buf.append(icm.getMapSize());
            buf.append(" Colors");
        } else {
            buf.append(cm.toString());
        }
        switch (cm.getTransparency()) {
            case Transparency.OPAQUE:
                break;
            case Transparency.BITMASK:
                buf.append(" with Alpha Bitmask");
                break;
            case Transparency.TRANSLUCENT:
                buf.append(" with Alpha Translucency");
                break;
        }
        return buf.toString();
    }

    private static class MaskEntry implements Comparable<MaskEntry> {
        private final static long serialVersionUID = 1L;

        private int mask;
        private int bits;
        private String name;

        public MaskEntry(int mask, String name) {
            this.mask = mask;
            this.name = name;

            for (int i = 0; i < 32; i++) {
                if (((mask >>> i) & 1) == 1) {
                    bits++;
                }
            }
        }

        public int getBits() {
            return bits;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(MaskEntry that) {
            return that.mask - this.mask;
        }
    }

    /// RGB in the range [0,1] to YCC in the range Y=[0,1], Cb=[-0.5,0.5],
    /// Cr=[-0.5,0.5]
    public static void RGBtoYCC(float[] rgb, float[] ycc) {
        float R = clamp(rgb[0], 0f, 1f);
        float G = clamp(rgb[1], 0f, 1f);
        float B = clamp(rgb[2], 0f, 1f);
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        float V = R - Y;
        float U = B - Y;
        float Cb = (U / 2f) /*+ 0.5f*/;
        float Cr = (V / 1.6f) /*+ 0.5f*/;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /// YCC in the range Y=[0,1], Cb=[-0.5,0.5], Cr=[-0.5,0.5]
    /// to RGB in the range [0,1]
    public static void YCCtoRGB(float[] ycc, float[] rgb) {
        float Y = clamp(ycc[0], 0f, 1f);
        float Cb = clamp(ycc[1], -0.5f, 0.5f);
        float Cr = clamp(ycc[2], -0.5f, 0.5f);
        float U = (Cb /*- 0.5f*/) * 2f;
        float V = (Cr /*- 0.5f*/) * 1.6f;
        float R = V + Y;
        float B = U + Y;
        float G = (Y - 0.3f * R - 0.1f * B) / 0.6f;
        rgb[0] = clamp(R, 0f, 1f);
        rgb[1] = clamp(G, 0f, 1f);
        rgb[2] = clamp(B, 0f, 1f);
    }

    /// RGB 8-bit per channel to YCC 16-bit per channel.
    public static void RGBtoYCC(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /// RGB 8-bit per channel to YCC 16-bit per channel.
    public static void RGBtoYCC(int rgb, int[] ycc) {
        int R = (rgb & 0xff0000) >>> 16;
        int G = (rgb & 0xff00) >>> 8;
        int B = rgb & 0xff;
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /// RGB in the range [0,1] to YUV in the range Y=[0,1], U=[-0.5,0.5],
    /// V=[-0.5,0.5]
    public static void RGBtoYUV(float[] rgb, float[] yuv) {
        float R = clamp(rgb[0], 0f, 1f);
        float G = clamp(rgb[1], 0f, 1f);
        float B = clamp(rgb[2], 0f, 1f);
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        yuv[0] = 0.299f * R + 0.587f * G + 0.114f * B;
        yuv[1] = -0.14713f * R - 0.28886f * G + 0.436f * B;
        yuv[2] = 0.615f * R - 0.51499f * G - 0.10001f * B;
    }

    /// YUV in the range Y=[0,1], U=[-0.5,0.5], V=[-0.5,0.5]
    /// to RGB in the range [0,1]
    public static void YUVtoRGB(float[] yuv, float[] rgb) {
        float Y = clamp(yuv[0], 0f, 1f);
        float U = clamp(yuv[1], -0.5f, 0.5f);
        float V = clamp(yuv[2], -0.5f, 0.5f);
        float R = 1 * Y + 0 * U + 1.13983f * V;
        float G = 1 * Y - 0.39465f * U - 0.58060f * V;
        float B = 1 * Y + 2.03211f * U + 0 * V;
        rgb[0] = clamp(R, 0, 1);
        rgb[1] = clamp(G, 0, 1);
        rgb[2] = clamp(B, 0, 1);
    }

    /// YCC 16-bit per channel to RGB 8-bit per channel.
    public static void YCCtoRGB(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = clamp((V + Y) / 256, 0, 255);
        int B = clamp((U + Y) / 256, 0, 255);
        int G = clamp((Y - 77 * R - 26 * B) / 153, 0, 255);
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    /// YCC 16-bit per channel to RGB 8-bit per channel.
    public static int YCCtoRGB(int[] ycc) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = clamp((V + Y) / 256, 0, 255);
        int B = clamp((U + Y) / 256, 0, 255);
        int G = clamp((Y - 77 * R - 26 * B) / 153, 0, 255);
        return R << 16 | G << 8 | B;
    }

    /// RGB in the range [0,1] to YIQ in the range Y in [0,1],
    /// I in [-0.5957,0.5957], Q in [-0.5226,0.5226].
    ///
    /// http://en.wikipedia.org/wiki/YIQ
    public static void RGBtoYIQ(float[] rgb, float[] yiq) {
        float R = clamp(rgb[0], 0f, 1f);
        float G = clamp(rgb[1], 0f, 1f);
        float B = clamp(rgb[2], 0f, 1f);
        float Y = 0.299f * R + 0.587f * G + 0.114f * B;
        float I = 0.595716f * R + -0.274453f * G + -0.321263f * B;
        float Q = 0.211456f * R + -0.522591f * G + 0.311135f * B;
        yiq[0] = Y;
        yiq[1] = I;
        yiq[2] = Q;
    }

    /// YIQ in the range Y in [0,1], I in [-0.5957,0.5957], Q in [-0.5226,0.5226]
    /// to RGB in the range [0,1]
    ///
    /// http://en.wikipedia.org/wiki/YIQ
    public static void YIQtoRGB(float[] yiq, float[] rgb) {
        float Y = clamp(yiq[0], 0f, 1f);
        float I = clamp(yiq[1], -0.5957f, 0.5957f);
        float Q = clamp(yiq[2], -0.5226f, 0.5226f);
        float R = Y + 0.9563f * I + 0.6210f * Q;
        float G = Y + -0.2721f * I + -0.6474f * Q;
        float B = Y + -1.1070f * I + 1.7046f * Q;
        rgb[0] = clamp(R, 0f, 1f);
        rgb[1] = clamp(G, 0f, 1f);
        rgb[2] = clamp(B, 0f, 1f);
    }
}
