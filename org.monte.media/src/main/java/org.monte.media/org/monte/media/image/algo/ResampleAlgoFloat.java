/*
 * @(#)ResampleAlgoFloat.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.algo;

/**
 * Image resampling algorithm for float values.
 */
public interface ResampleAlgoFloat {
    /**
     * Resamples the provided image.
     *
     * @param srcPixels   the source pixels
     * @param srcWidth    the width of the source image
     * @param srcHeight   the height of the source image
     * @param srcOffset   the offset of the source image in the source pixels
     * @param srcScanline the scanline length of the source image
     * @param dstPixels   the destination pixels
     * @param dstWidth    the width of the destination image
     * @param dstHeight   the height of the destination image
     * @param dstOffset   the offset of the destination image in the destination pixels
     * @param dstScanline the scanline length of the destination image
     */
    void resample(float[] srcPixels, int srcWidth, int srcHeight, int srcOffset, int srcScanline, float[] dstPixels, int dstWidth, int dstHeight, int dstOffset, int dstScanline);
}
