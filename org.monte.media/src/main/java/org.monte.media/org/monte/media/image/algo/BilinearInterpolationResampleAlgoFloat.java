/*
 * @(#)BilinearInterpolationResampleAlgoFloat.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.algo;

/**
 * Resamples an image by averaging 4 pixels of the source image.
 * <pre>
 *               │(⌊x⌋,⌊y⌋)    │(⌊x+1,⌊y⌋)    │
 *              ─┼─────────────┼──────────────┼─
 *               │        ╎β   │              │
 *               │        ╎    │              │
 *               │  (x,y) ↓    │              │
 *               │╌╌╌╌╌╌╌→XXXXX│XXXXXXXX      │
 *               │ɑ       XXXXX│XXXXXXXX      │
 *              ─┼─────────────┼──────────────┼
 *               │(⌊x⌋,⌊y+1⌋)  │(⌊x+1⌋,⌊y+1⌋) │
 *               │        XXXXX│XXXXXXXX      │
 *               │        XXXXX│XXXXXXXX      │
 *               │        XXXXX│XXXXXXXX      │
 *               │             │              │
 *              ─┼─────────────┼──────────────┼
 * </pre>
 */
public class BilinearInterpolationResampleAlgoFloat implements ResampleAlgoFloat {
    @Override
    public void resample(float[] srcPixels, int srcWidth, int srcHeight, int srcOffset, int srcScanline, float[] dstPixels, int dstWidth, int dstHeight, int dstOffset, int dstScanline) {
        // scale factors
        float sx = srcWidth / (float) dstWidth;
        float sy = srcHeight / (float) dstHeight;

        // translation
        int tx = (int) (sx * 0.5f);
        int ty = (int) (sy * 0.5f);

        for (int destY = 0; destY < dstHeight; destY++) {
            // Compute the exact coordinate of the pixel in the source image
            float srcY = destY * sy;
            float beta = srcY - (float) Math.floor(srcY);

            // Compute the integer coordinates of the pixel in the source image
            int srcY1 = (int) srcY + ty;
            int srcY2 = Math.min(srcY1 + 1, srcHeight - 1);// clamp image
            int srcIndex1 = srcY1 * srcScanline + srcOffset;
            int srcIndex2 = srcY2 * srcScanline + srcOffset;

            int dstIndex = destY * dstScanline;
            for (int destX = 0; destX < dstWidth; destX++) {
                // Compute the exact coordinate of the pixel in the source image
                float srcX = destX * sx;
                float alpha = srcX - (float) Math.floor(srcX);

                // Compute the integer coordinates of the pixel in the source image
                int srcX1 = (int) (destX * sx) + tx;
                int srcX2 = Math.min(srcX1 + 1, srcWidth - 1);// clamp image

                // Perform bilinear interpolation
                dstPixels[dstIndex + destX] =
                        (1 - alpha) * (1 - beta) * srcPixels[srcIndex1 + srcX1]
                                + alpha * (1 - beta) * srcPixels[srcIndex1 + srcX2]
                                + (1 - alpha) * beta * srcPixels[srcIndex2 + srcX1]
                                + alpha * beta * srcPixels[srcIndex2 + srcX2]
                ;
            }
        }
    }
}
