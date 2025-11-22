/*
 * @(#)ResampleAlgoFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.api.Test;
import org.monte.media.image.FloatImages;
import org.monte.media.image.TestImageFactory;
import org.monte.media.image.algo.BilinearInterpolationResampleAlgoFloat;
import org.monte.media.image.algo.NearestNeighbourResampleAlgoFloat;
import org.monte.media.image.algo.ResampleAlgoFloat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferFloat;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResampleAlgoFloatTest {
    int dstWidth = 640;
    int dstHeight = 480;

    @Test
    public void shouldResampleImageUsingNearestNeighborAlgo() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();

        src = FloatImages.reuseSourceImage(src, src.getColorModel());
        BufferedImage dst = FloatImages.reuseDestImage(null, dstWidth, dstHeight, src.getColorModel());

        ResampleAlgoFloat algo = new NearestNeighbourResampleAlgoFloat();
        int numBanks = src.getRaster().getDataBuffer().getNumBanks();
        for (int i = 0; i < numBanks; i++) {
            float[] srcPixels = ((DataBufferFloat) src.getRaster().getDataBuffer()).getData(i);
            float[] dstPixels = ((DataBufferFloat) dst.getRaster().getDataBuffer()).getData(i);
            algo.resample(
                    srcPixels, src.getWidth(), src.getHeight(), 0, src.getWidth(),
                    dstPixels, dst.getWidth(), dst.getHeight(), 0, dst.getWidth()
            );
        }

        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/resample-output-nearest-neighbor.png"));
        assertTrue(success);
    }

    @Test
    public void shouldResampleImageUsingBiLinearAlgo() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();

        src = FloatImages.reuseSourceImage(src, src.getColorModel());
        BufferedImage dst = FloatImages.reuseDestImage(null, dstWidth, dstHeight, src.getColorModel());


        ResampleAlgoFloat algo = new BilinearInterpolationResampleAlgoFloat();
        int numBanks = src.getRaster().getDataBuffer().getNumBanks();
        for (int i = 0; i < numBanks; i++) {
            float[] srcPixels = ((DataBufferFloat) src.getRaster().getDataBuffer()).getData(i);
            float[] dstPixels = ((DataBufferFloat) dst.getRaster().getDataBuffer()).getData(i);
            algo.resample(
                    srcPixels, src.getWidth(), src.getHeight(), 0, src.getWidth(),
                    dstPixels, dst.getWidth(), dst.getHeight(), 0, dst.getWidth()
            );
        }


        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/resample-output-bilinear.png"));
        assertTrue(success);
    }
}