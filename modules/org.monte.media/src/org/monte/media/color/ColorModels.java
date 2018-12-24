/* @(#)Colors.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.Arrays;

/**
 * Utility methods for ColorModels.
 *
 * @author  Werner Randelshofer
 * @version $Id$
 */
public class ColorModels {

    /**
     * Prevent instance creation.
     */
    private ColorModels() {
    }

    /**
     * Returns a descriptive string for the provided color model.
     */
    public static String toString(ColorModel cm) {
        StringBuffer buf = new StringBuffer();
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
            for (int i = 0; i < entries.length; i++) {
                buf.append(entries[i]);
            }
        } else if (cm instanceof IndexColorModel) {
            buf.append("Index Color Model ");
            IndexColorModel icm = (IndexColorModel) cm;
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

    /** RGB in the range [0,1] to YCC in the range Y=[0,1], Cb=[-0.5,0.5],
     * Cr=[-0.5,0.5]
     */
    public static void RGBtoYCC(float[] rgb, float[] ycc) {
        float R = max(0f, min(1f, rgb[0]));
        float G = max(0f, min(1f, rgb[1]));
        float B = max(0f, min(1f, rgb[2]));
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        float V = R - Y;
        float U = B - Y;
        float Cb = (U / 2f) /*+ 0.5f*/;
        float Cr = (V / 1.6f) /*+ 0.5f*/;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /** YCC in the range Y=[0,1], Cb=[-0.5,0.5], Cr=[-0.5,0.5] 
     * to RGB in the range [0,1] */
    public static void YCCtoRGB(float[] ycc, float[] rgb) {
        float Y = max(0f, min(1f, ycc[0]));
        float Cb = max(-0.5f, min(0.5f, ycc[1]));
        float Cr = max(-0.5f, min(0.5f, ycc[2]));
        float U = (Cb /*- 0.5f*/) * 2f;
        float V = (Cr /*- 0.5f*/) * 1.6f;
        float R = V + Y;
        float B = U + Y;
        float G = (Y - 0.3f * R - 0.1f * B) / 0.6f;
        rgb[0] = max(0f, min(1f, R));
        rgb[1] = max(0f, min(1f, G));
        rgb[2] = max(0f, min(1f, B));
    }

    /** RGB 8-bit per channel to YCC 16-bit per channel. */
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

    /** RGB 8-bit per channel to YCC 16-bit per channel. */
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

    /** RGB in the range [0,1] to YUV in the range Y=[0,1], U=[-0.5,0.5],
     * V=[-0.5,0.5]
     */
    public static void RGBtoYUV(float[] rgb, float[] yuv) {
        float R = max(0f, min(1f, rgb[0]));
        float G = max(0f, min(1f, rgb[1]));
        float B = max(0f, min(1f, rgb[2]));
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        yuv[0] = 0.299f * R + 0.587f * G + 0.114f * B;
        yuv[1] = -0.14713f * R - 0.28886f * G + 0.436f * B;
        yuv[2] = 0.615f * R - 0.51499f * G - 0.10001f * B;
    }

    /** YUV in the range Y=[0,1], U=[-0.5,0.5], V=[-0.5,0.5] 
     * to RGB in the range [0,1] */
    public static void YUVtoRGB(float[] yuv, float[] rgb) {
        float Y = max(0f, min(1f, yuv[0]));
        float U = max(-0.5f, min(0.5f, yuv[1]));
        float V = max(-0.5f, min(0.5f, yuv[2]));
        float R = 1 * Y + 0 * U + 1.13983f * V;
        float G = 1 * Y - 0.39465f * U - 0.58060f * V;
        float B = 1 * Y + 2.03211f * U + 0 * V;
        rgb[0]=min(1,max(0,R));
        rgb[1]=min(1,max(0,G));
        rgb[2]=min(1,max(0,B));
    }

    /** YCC 16-bit per channel to RGB 8-bit per channel. */
    public static void YCCtoRGB(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = min(255, max(0, (V + Y) / 256));
        int B = min(255, max(0, (U + Y) / 256));
        int G = min(255, max(0, (Y - 77 * R - 26 * B) / 153));
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    /** YCC 16-bit per channel to RGB 8-bit per channel. */
    public static int YCCtoRGB(int[] ycc) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = min(255, max(0, (V + Y) / 256));
        int B = min(255, max(0, (U + Y) / 256));
        int G = min(255, max(0, (Y - 77 * R - 26 * B) / 153));
        return R << 16 | G << 8 | B;
    }

    /** RGB in the range [0,1] to YIQ in the range Y in [0,1],
     * I in [-0.5957,0.5957], Q in [-0.5226,0.5226].
     * <p>
     * http://en.wikipedia.org/wiki/YIQ
     */
    public static void RGBtoYIQ(float[] rgb, float[] yiq) {
        float R = max(0f, min(1f, rgb[0]));
        float G = max(0f, min(1f, rgb[1]));
        float B = max(0f, min(1f, rgb[2]));
        float Y = 0.299f * R + 0.587f * G + 0.114f * B;
        float I = 0.595716f * R + -0.274453f * G + -0.321263f * B;
        float Q = 0.211456f * R + -0.522591f * G + 0.311135f * B;
        yiq[0] = Y;
        yiq[1] = I;
        yiq[2] = Q;
    }

    /** YIQ in the range Y in [0,1], I in [-0.5957,0.5957], Q in [-0.5226,0.5226] 
     * to RGB in the range [0,1] 
     * <p>
     * http://en.wikipedia.org/wiki/YIQ
     */
    public static void YIQtoRGB(float[] yiq, float[] rgb) {
        float Y = max(0f, min(1f, yiq[0]));
        float I = max(-0.5957f, min(0.5957f, yiq[1]));
        float Q = max(-0.5226f, min(0.5226f, yiq[2]));
        float R = Y + 0.9563f * I + 0.6210f * Q;
        float G = Y + -0.2721f * I + -0.6474f * Q;
        float B = Y + -1.1070f * I + 1.7046f * Q;
        rgb[0] = max(0f, min(1f, R));
        rgb[1] = max(0f, min(1f, G));
        rgb[2] = max(0f, min(1f, B));
    }
}
