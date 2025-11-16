/*
 * @(#)ConvolveOpFloatTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.image;

import org.junit.jupiter.api.Test;
import org.monte.media.image.op.ConvolveOp;
import org.monte.media.image.op.GaussianKernelFactory;
import org.monte.media.image.op.LanczosKernelFactory;
import org.monte.media.image.op.MirrorEdgeAction;

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

public class ConvolveOpFloatTest {


    @Test
    public void shouldConvolveImageUsingGaussian() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        float s = 0.25f;
        float sigma = 0.5f / s;
        float radius = sigma * 3;

        float[] dataH = new GaussianKernelFactory().createKernel(radius);
        float[] dataV = new GaussianKernelFactory().createKernel(radius);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ConvolveOp opH = new ConvolveOp(new Kernel(dataH.length, 1, dataH), new MirrorEdgeAction());
        ConvolveOp opV = new ConvolveOp(new Kernel(1, dataV.length, dataV), new MirrorEdgeAction());
        var dstH = opH.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        var dstV = opV.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        dstH = opH.filter(src, dstH);
        dstV = opV.filter(dstH, dstV);
        BufferedImage dst = dstV;
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/convolve-gaussian.png"));
        assertTrue(success);
    }

    @Test
    public void shouldConvolveImageUsingLanczos() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        float s = 0.25f;
        float sigma = 0.5f / s;
        float radius = sigma * 3;
        float[] dataH = new LanczosKernelFactory().createKernel(radius);
        float[] dataV = new LanczosKernelFactory().createKernel(radius);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ConvolveOp opH = new ConvolveOp(new Kernel(dataH.length, 1, dataH), new MirrorEdgeAction());
        ConvolveOp opV = new ConvolveOp(new Kernel(1, dataV.length, dataV), new MirrorEdgeAction());
        var dstH = opH.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        var dstV = opV.createCompatibleDestImage(src, new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT));
        dstH = opH.filter(src, dstH);
        dstV = opV.filter(dstH, dstV);
        BufferedImage dst = dstV;
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/convolve-lanczos.png"));
        assertTrue(success);
    }

    @Test
    public void shouldConvolveImageUsingAwt() throws IOException {
        BufferedImage src = TestImageFactory.createHighFrequencyTestImage();
        BufferedImage srcSRgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = srcSRgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        // src = ImageIO.read(new File("/Users/wr/Documents/Virtualization/Amiga3000/Work/Media/DanDaDan1/Frames/0001.png"));
        float s = 0.25f;
        float sigma = 0.5f / s;
        float radius = sigma * 3;

        float[] dataH = new GaussianKernelFactory().createKernel(radius);
        float[] dataV = new GaussianKernelFactory().createKernel(radius);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        int width = src.getWidth();
        int height = src.getHeight();

        ColorModel cm = ColorModel.getRGBdefault();

        BufferedImage dstH = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        BufferedImage dstV = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        dstH = new java.awt.image.ConvolveOp(new Kernel(dataH.length, 1, dataH)).filter(srcSRgb, dstH);
        dstV = new java.awt.image.ConvolveOp(new Kernel(1, dataV.length, dataV)).filter(dstH, dstV);
        BufferedImage dst = dstV;
        BufferedImage dstSRgb = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_RGB);
        g = dstSRgb.createGraphics();
        g.drawImage(dst, 0, 0, null);
        g.dispose();
        boolean success = ImageIO.write(dstSRgb, "PNG", new File("target/convolve-gaussian-awt.png"));
        assertTrue(success);

    }

}