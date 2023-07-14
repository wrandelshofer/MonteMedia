/*
 * @(#)YccConverters.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class YccConverters {
    /**
     * Don't let anyone instantiate this class.
     */
    private YccConverters() {
    }

    /**
     * YCC 8-bit per channel to RGB 8-bit per channel.
     */
    private static void RGB24toYCC24(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        // Source: JPEG File Interchange Format Version 1.02, September 1, 1992
        //YCbCr (256 levels) can be computed directly from 8-bit RGB as follows:
        //Y = 0.299R +0.587G +0.114B
        //Cb = - 0.1687 R - 0.3313 G + 0.5 B + 128
        //Cr = 0.5 R - 0.4187 G - 0.0813 B + 128
        int Y = (299 * R + 587 * G + 114 * B) / 1000;
        int Cb = (-1687 * R - 3313 * G + 5000 * B) / 10000 + 128;
        int Cr = (5000 * R - 4187 * G - 813 * B) / 10000 + 128;
        ycc[0] = min(255, max(0, Y));
        ycc[1] = min(255, max(0, Cb));
        ycc[2] = min(255, max(0, Cr));
    }

    /**
     * RGB 8-bit per channel to YCC 16-bit per channel.
     */
    private static void RGB24toYCC48(int[] rgb, int[] ycc) {
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

    /**
     * RGB 8-bit per channel to YCC 16-bit per channel.
     */
    private static void RGB24toYCC48(int rgb, int[] ycc) {
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

    private static void RGBtoYCC(float[] rgb, float[] ycc) {
        float R = rgb[0];
        float G = rgb[1];
        float B = rgb[2];
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        float V = R - Y;
        float U = B - Y;
        float Cb = (U / 2f) + 0.5f;
        float Cr = (V / 1.6f) + 0.5f;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /**
     * YCC 8-bit per channel to RGB 8-bit per channel.
     */
    private static void YCC24toRGB24(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        // Source: JPEG File Interchange Format Version 1.02, September 1, 1992
        //RGB can be computed directly from YCbCr (256 levels) as follows:
        //R = Y + 1.402 (Cr-128)
        //G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128)
        //B = Y + 1.772 (Cb-128)
        int R = (1000 * Y + 1402 * (Cr - 128)) / 1000;
        int G = (100000 * Y - 34414 * (Cb - 128) - 71414 * (Cr - 128)) / 100000;
        int B = (1000 * Y + 1772 * (Cb - 128)) / 1000;
        rgb[0] = min(255, max(0, R));
        rgb[1] = min(255, max(0, G));
        rgb[2] = min(255, max(0, B));
    }

    /**
     * YCC 16-bit per channel to RGB 8-bit per channel.
     */
    private static void YCC48toRGB24(int[] ycc, int[] rgb) {
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

    private static void YCCtoRGB(float[] ycc, float[] rgb) {
        float Y = ycc[0];
        float Cb = ycc[1];
        float Cr = ycc[2];
        float U = (Cb - 0.5f) * 2f;
        float V = (Cr - 0.5f) * 1.6f;
        float R = V + Y;
        float B = U + Y;
        float G = (Y - 0.3f * R - 0.1f * B) / 0.6f;
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }
}
