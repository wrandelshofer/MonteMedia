/*
 * @(#)ConvolveOpFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image.op;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.image.TestImageFactory;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConvolveOpTest {


    @ParameterizedTest
    @ValueSource(floats = {4, 10})
    public void shouldConvolveImageUsingGaussian(float sigma) throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();

        float[] dataH = new GaussianKernelFactory().createKernel(sigma);
        float[] dataV = new GaussianKernelFactory().createKernel(sigma);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ConvolveOp opH = new ConvolveOp(new Kernel(dataH.length, 1, dataH), new MirrorEdgeAction());
        ConvolveOp opV = new ConvolveOp(new Kernel(1, dataV.length, dataV), new MirrorEdgeAction());
        var destH = opH.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        var destV = opV.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        destH = opH.filter(src, destH);
        destV = opV.filter(destH, destV);
        BufferedImage dest = destV;
        BufferedImage destSRgb = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = destSRgb.createGraphics();
        g.drawImage(dest, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(destSRgb, "PNG", new File("target/ConvolveOp-gaussian-" + sigma + ".png"));
        assertTrue(success);
    }

    @ParameterizedTest
    @ValueSource(floats = {4, 10})
    public void shouldConvolveImageUsingLanczos(float sigma) throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        float[] dataH = new LanczosKernelFactory().createKernel(sigma);
        float[] dataV = new LanczosKernelFactory().createKernel(sigma);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ConvolveOp opH = new ConvolveOp(new Kernel(dataH.length, 1, dataH), new MirrorEdgeAction());
        ConvolveOp opV = new ConvolveOp(new Kernel(1, dataV.length, dataV), new MirrorEdgeAction());
        var destH = opH.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        var destV = opV.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        destH = opH.filter(src, destH);
        destV = opV.filter(destH, destV);
        BufferedImage dest = destV;
        BufferedImage destSRgb = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = destSRgb.createGraphics();
        g.drawImage(dest, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(destSRgb, "PNG", new File("target/ConvolveOp-lanczos-" + sigma + ".png"));
        assertTrue(success);
    }

    @ParameterizedTest
    @ValueSource(floats = {4, 10})
    public void shouldConvolveImageUsingAwt(float sigma) throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        BufferedImage srcSRgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = srcSRgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        // src = ImageIO.read(new File("/Users/wr/Documents/Virtualization/Amiga3000/Work/Media/DanDaDan1/Frames/0001.png"));

        float[] dataH = new GaussianKernelFactory().createKernel(sigma);
        float[] dataV = new GaussianKernelFactory().createKernel(sigma);
        int width = src.getWidth();
        int height = src.getHeight();

        ColorModel cm = ColorModel.getRGBdefault();

        BufferedImage destH = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        BufferedImage destV = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        destH = new java.awt.image.ConvolveOp(new Kernel(dataH.length, 1, dataH)).filter(srcSRgb, destH);
        destV = new java.awt.image.ConvolveOp(new Kernel(1, dataV.length, dataV)).filter(destH, destV);
        BufferedImage dest = destV;
        BufferedImage destSRgb = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_INT_RGB);
        g = destSRgb.createGraphics();
        g.drawImage(dest, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(destSRgb, "PNG", new File("target/AwtConvolveOp-gaussian-" + sigma + ".png"));
        assertTrue(success);

    }

}