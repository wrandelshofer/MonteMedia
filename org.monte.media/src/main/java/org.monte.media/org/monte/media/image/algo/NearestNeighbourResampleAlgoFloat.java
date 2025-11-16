/*
 * @(#)NearestNeighbourResampleAlgoFloat.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.algo;

/**
 * Resamples an image by picking the nearest neighbour pixel
 * of the source image.
 */
public class NearestNeighbourResampleAlgoFloat implements ResampleAlgoFloat {
    @Override
    public void resample(float[] srcPixels, int srcWidth, int srcHeight, int srcOffset, int srcScanline, float[] dstPixels, int dstWidth, int dstHeight, int dstOffset, int dstScanline) {
        // scale factors
        float sx = srcWidth / (float) dstWidth;
        float sy = srcHeight / (float) dstHeight;

        // translation
        int tx = (int) (sx * 0.5f);
        int ty = (int) (sy * 0.5f);

        for (int destY = 0; destY < dstHeight; destY++) {
            int srcY = (int) (destY * sy) + ty;
            int srcIndex = srcY * srcScanline + srcOffset;
            int dstIndex = destY * dstScanline;
            for (int destX = 0; destX < dstWidth; destX++) {
                int srcX = (int) (destX * sx) + tx;
                dstPixels[dstIndex + destX] = srcPixels[srcIndex + srcX];
            }
        }
    }
}
