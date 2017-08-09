/* @(#)WhiteBalance.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.color;

import org.monte.media.color.ColorModels;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import org.monte.media.image.Images;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import org.monte.media.math.LinearEquations;

/**
 * {@code WhiteBalance}.
 * <p>
 * References:<br>
 * [Ken09] Kenfack, Pierre Marie (2009). Implementierung und Vergleich 
 * verschiedener Algorithmen 	zur Bildsensorkalibrierung. Fraunhofer ITWM.
 * http://www.itwm.fraunhofer.de/fileadmin/ITWM-Media/Abteilungen/BV/Pdf/
Diplomarbeit_Kenfack.pdf
 * <p>
 * [Lam05] Edmund Lam, Combining gray world and retinex theory for automatic 
 * white balance in	digital photography, Consumer Electronics, 2005. 
 * (ISCE 2005). Proceedings of the Ninth International Symposium on (2005), pp.134–139.
 * <p>
 * [Huo05] Huo Yun-yan, Chang Yi-lin, Wang Jing, Wei Xiao-xia. (2005). Robust 
 * Automatic White 	Balance Algorithm using Gray Color Points in Images. 
 * 
 * @author Werner Randelshofer
 * @version $Id: WhiteBalance.java 364 2016-11-09 19:54:25Z werner $
 */
public class WhiteBalance {

    private WhiteBalance() {
    }

    ;
    
    /** Performs white balance adjustment using the "grey world" assumption
     * as described in [Ken09]. */
    public static BufferedImage whiteBalanceGreyworld(BufferedImage img) {
        img = Images.toIntImage(img);
        Histogram hist = new Histogram();
        hist.countPixels(img.getRaster());

        double[] m = whiteBalanceGreyworld(hist);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[] p = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int[] q = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < p.length; i++) {
            int px = p[i];
            double R = (px & 0xff0000) >> 16;
            double G = (px & 0xff00) >> 8;
            double B = (px & 0xff) >> 0;
            double Rq = m[0] * R + m[1] * B + m[2] * G;
            double Gq = m[3] * R + m[4] * B + m[5] * G;
            double Bq = m[6] * R + m[7] * B + m[8] * G;
            q[i] = ((min(255, max(0, (int) Rq))) & 0xff) << 16
                    | ((min(255, max(0, (int) Gq))) & 0xff) << 8
                    | ((min(255, max(0, (int) Bq))) & 0xff) << 0;
        }

        return out;
    }

    /** Performs white balance adjustment using the "grey world" assumption
     * as described in [Huo05], but using the YCbCr color space instead of YUV. */
    public static BufferedImage whiteBalanceGreyworldYCC(BufferedImage img, float[] ccAdjust, boolean all) {
        img = Images.toIntImage(img);
        Histogram hist = new Histogram();
        hist.countPixels(img.getRaster());

        double[] m = whiteBalanceGreyworld(hist);

        int[] p = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();


        float[] rgb = new float[3];
        float[] ycc = new float[3];
        float T = 0.097f;
        int NGray = 0, NColor = 0;
        double cbGraySum = 0, cbColorSum = 0;
        double crGraySum = 0, crColorSum = 0;
        for (int i = 0; i < p.length; i++) {
            int px = p[i];
            rgb[0] = ((px & 0xff0000) >> 16) / 255f;
            rgb[1] = ((px & 0xff00) >> 8) / 255f;
            rgb[2] = ((px & 0xff) >> 0) / 255f;
            ColorModels.RGBtoYCC(rgb, ycc);
            if ((abs(ycc[1]) + abs(ycc[2])) / ycc[0] > T) {
                NColor++;
                cbColorSum += ycc[1];
                crColorSum += ycc[2];
            } else {
                NGray++;
                cbGraySum += ycc[1];
                crGraySum += ycc[2];
            }
        }

        float cbGrayAdj = -(float) (cbGraySum / NGray);
        float crGrayAdj = -(float) (crGraySum / NGray);
        float cbAllAdj = -(float) ((cbColorSum / NColor) + cbGraySum / NGray);
        float crAllAdj = -(float) (crColorSum / NColor + crGraySum / NGray);
        float cbAdj = cbGrayAdj;
        float crAdj = crGrayAdj;
        
                if (all) {
            cbAdj = cbAllAdj;
            crAdj = crAllAdj;
        }

        System.out.println("WhiteBalance.YCC GRAY cb=" + cbGrayAdj + " cr=" + crGrayAdj + " N=" + NGray);
        System.out.println("WhiteBalance.YCC ALL cb=" + cbAllAdj + " cr=" + crAllAdj + " N=" + NColor);
        if (ccAdjust != null) {
            ccAdjust[0] = cbAdj;
            ccAdjust[1] = crAdj;
            return null;
        } else {
            BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            int[] q = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < p.length; i++) {
                int px = p[i];
                rgb[0] = ((px & 0xff0000) >> 16) / 255f;
                rgb[1] = ((px & 0xff00) >> 8) / 255f;
                rgb[2] = ((px & 0xff) >> 0) / 255f;
                ColorModels.RGBtoYCC(rgb, ycc);
                ycc[1] += cbAdj;
                ycc[2] += crAdj;
                ColorModels.YCCtoRGB(ycc, rgb);
                q[i] = ((int) (rgb[0] * 255) << 16)//
                        | ((int) (rgb[1] * 255) << 8)//
                        | ((int) (rgb[2] * 255) << 0);

            }
            return out;
        }
    }

    /** Performs white balance adjustment using the "grey world" assumption
     * as described in [Huo05]. */
    public static BufferedImage whiteBalanceGreyworldYUV(BufferedImage img, float[] uvAdjust, boolean all) {
        img = Images.toIntImage(img);
        Histogram hist = new Histogram();
        hist.countPixels(img.getRaster());

        double[] m = whiteBalanceGreyworld(hist);

        int[] p = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();


        float[] rgb = new float[3];
        float[] yuv = new float[3];
        float T = 0.097f;
        int NGray = 0, NColor = 0;
        double UGraySum = 0, UColorSum = 0;
        double VGraySum = 0, VColorSum = 0;
        for (int i = 0; i < p.length; i++) {
            int px = p[i];
            rgb[0] = ((px & 0xff0000) >> 16) / 255f;
            rgb[1] = ((px & 0xff00) >> 8) / 255f;
            rgb[2] = ((px & 0xff) >> 0) / 255f;
            ColorModels.RGBtoYUV(rgb, yuv);
            if ((abs(yuv[1]) + abs(yuv[2])) / yuv[0] > T) {
                NColor++;
                UColorSum += yuv[1];
                VColorSum += yuv[2];
            } else {
                NGray++;
                UGraySum += yuv[1];
                VGraySum += yuv[2];
            }
        }

        float UGrayAdj = -(float) (UGraySum / NGray);
        float VGrayAdj = -(float) (VGraySum / NGray);
        float UAllAdj = -(float) ((UColorSum / NColor) + UGraySum / NGray);
        float VAllAdj = -(float) (VColorSum / NColor + VGraySum / NGray);
        float UAdj = UGrayAdj;
        float VAdj = VGrayAdj;
        if (all) {
            UAdj = UAllAdj;
            VAdj = VAllAdj;
        }

        System.out.println("WhiteBalance.YUV GRAY cb=" + UGrayAdj + " cr=" + VGrayAdj + " N=" + NGray);
        System.out.println("WhiteBalance.YUV ALL cb=" + UAllAdj + " cr=" + VAllAdj + " N=" + NColor);
        if (uvAdjust != null) {
            uvAdjust[0] = UAdj;
            uvAdjust[1] = VAdj;
            return null;
        } else {
            BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            int[] q = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < p.length; i++) {
                int px = p[i];
                rgb[0] = ((px & 0xff0000) >> 16) / 255f;
                rgb[1] = ((px & 0xff00) >> 8) / 255f;
                rgb[2] = ((px & 0xff) >> 0) / 255f;
                ColorModels.RGBtoYUV(rgb, yuv);
                yuv[1] += UAdj;
                yuv[2] += VAdj;
                ColorModels.YUVtoRGB(yuv, rgb);
                q[i] = ((int) (rgb[0] * 255) << 16)//
                        | ((int) (rgb[1] * 255) << 8)//
                        | ((int) (rgb[2] * 255) << 0);

            }
            return out;
        }
    }

    public static BufferedImage whiteBalanceRetinex(BufferedImage img) {
        img = Images.toIntImage(img);
        Histogram hist = new Histogram();
        hist.countPixels(img.getRaster());

        double[] m = whiteBalanceRetinex(hist);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[] p = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int[] q = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < p.length; i++) {
            int px = p[i];
            double R = (px & 0xff0000) >> 16;
            double G = (px & 0xff00) >> 8;
            double B = (px & 0xff) >> 0;
            double Rq = m[0] * R + m[1] * B + m[2] * G;
            double Gq = m[3] * R + m[4] * B + m[5] * G;
            double Bq = m[6] * R + m[7] * B + m[8] * G;
            q[i] = ((max(0, (int) Rq)) & 0xff) << 16
                    | ((max(0, (int) Gq)) & 0xff) << 8
                    | ((max(0, (int) Bq)) & 0xff) << 0;
        }

        return out;
    }

    public static BufferedImage whiteBalanceQM(BufferedImage img) {
        img = Images.toIntImage(img);
        Histogram hist = new Histogram();
        hist.countPixels(img.getRaster());

        double[] m = whiteBalanceQM(hist);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] p = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int[] q = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

        float mur = (float) m[0];
        float nur = (float) m[1];
        float mub = (float) m[2];
        float nub = (float) m[3];

        System.out.println("WhiteBalance QM mur=" + mur + " nur=" + nur+" mub=" + mub + " nub=" + nub);
        for (int i = 0; i < p.length; i++) {
            int px = p[i];
            double R = (px & 0xff0000) >> 16;
            double B = (px & 0xff) >> 0;
            double Rq = mur * R * R + nur * R;
            double Bq = mub * B * B + nub * B;
            q[i] = ((min(255, max(0, (int) Rq)))) << 16
                    | (px & 0xff00)
                    | ((min(255, max(0, (int) Bq)))) << 0;
        }

        return out;
    }

    /**
     * Computes the white balance of an image using the Greyworld algorithm.
     * <p>
     * The Greyworld algorithm assumes that the average color of an image
     * should be a neutral gray: avg(R)=avg(G)=avg(B).
     * <p>
     * References:<br>
     * Edmund Lam, Combining gray world and retinex theory for automatic white 
     * balance in digital photography, Consumer Electronics, 2005. (ISCE 2005).
     * Proceedings of the Ninth International Symposium on (2005), pp.134–139.
     * <p>
     * Kenfack, Pierre Marie. (2009). Implementierung und Vergleich verschiedener
     * Algorithmen zur 	Bildsensorkalibrierung. Fraunhofer ITWM.
     * http://www.itwm.fraunhofer.de/fileadmin/ITWM
     * <p>
     * 
     * 
     * @param rgbHist 
     * @return a 3x3 matrix which performs the color correction matrix*[R,G,B].
     */
    public static double[] whiteBalanceGreyworld(Histogram rgbHist) {
        double[] mean_ = rgbHist.getMean();
        double Rmean = mean_[0],
                Gmean = mean_[1],
                Bmean = mean_[2];
        double RGBmean = (Rmean + Gmean + Bmean) / 3;

        double[] max_ = rgbHist.getHighValue();
        double Rmax = max_[0],
                Gmax = max_[1],
                Bmax = max_[2];
        double RGBmax = max(max(Rmax, Gmax), Bmax);

        double fr = RGBmean / Rmean;
        double fg = RGBmean / Bmean;
        double fb = RGBmean / Bmean;

        if (Double.isNaN(fr)) {
            fr = 1;
        }
        if (Double.isNaN(fg)) {
            fg = 1;
        }
        if (Double.isNaN(fb)) {
            fb = 1;
        }

        double[] matrix = {//
            fr, 0, 0,//
            0, fg, 0,//
            0, 0, fb};
        return matrix;
    }

    /**
     * Computes the white balance of an image using the Retinex algorithm.
     * <p>
     * References:<br>
     * Edmund Lam, Combining gray world and retinex theory for automatic white 
     * balance in digital photography, Consumer Electronics, 2005. (ISCE 2005).
     * Proceedings of the Ninth International Symposium on (2005), pp.134–139.
     * <p>
     * Kenfack, Pierre Marie. (2009). Implementierung und Vergleich verschiedener
     * Algorithmen zur 	Bildsensorkalibrierung. Fraunhofer ITWM.
     * http://www.itwm.fraunhofer.de/fileadmin/ITWM
     * <p>
     * 
     * 
     * @param rgbHist 
     * @return a 3x3 matrix which performs the color correction matrix*[R,G,B].
     */
    public static double[] whiteBalanceRetinex(Histogram rgbHist) {
        double[] mean_ = rgbHist.getMean();
        double Rmean = mean_[0],
                Gmean = mean_[1],
                Bmean = mean_[2];
        double RGBmean = (Rmean + Gmean + Bmean) / 3;

        double[] max_ = rgbHist.getHighValue();
        double Rmax = max_[0],
                Gmax = max_[1],
                Bmax = max_[2];
        double RGBmax = max(max(Rmax, Gmax), Bmax);

        double Rgain = Gmax / Rmax;
        double Bgain = Gmax / Bmax;

        if (Double.isNaN(Rgain)) {
            Rgain = 1;
        }
        if (Double.isNaN(Bgain)) {
            Bgain = 1;
        }

        double[] matrix = {//
            Rgain, 0, 0,//
            0, 1, 0,//
            0, 0, Bgain};
        return matrix;

    }

    /**
     * Computes the white balance of an image using the Quadratic Mapping (QM)
     * algorithm. QM is a combination of the Greyworld and the Retinex algorithm.
     * And usually gives better results.
     * <p>
     * References:<br>
     * Edmund Lam, Combining gray world and retinex theory for automatic white 
     * balance in digital photography, Consumer Electronics, 2005. (ISCE 2005).
     * Proceedings of the Ninth International Symposium on (2005), pp.134–139.
     * <p>
     * Kenfack, Pierre Marie. (2009). Implementierung und Vergleich verschiedener
     * Algorithmen zur 	Bildsensorkalibrierung. Fraunhofer ITWM.
     * http://www.itwm.fraunhofer.de/fileadmin/ITWM
     * <p>
     * 
     * Returns a vector with 4 values: Rmu, Rnu, Bmu, Bnu.
     * These values can be put into a 3x6 matrix as shown below:
     * <pre>
     * 
     *                                     [ R
     *                                       G
     *                                       B
     * [R'    [ Rnu  0   0  Rmu  0   0       R^2
     *  G'  =    0   1   0   0   0   0    *  G^2
     *  B']      0   0  Bnu  0   0  Bmu ]    B^2 ]
     * </pre>         
     * 
     * @param rgbHist 
     * @return a vector with the values {Rmu, Rnu, Bmu, Bnu}.
     */
    public static double[] whiteBalanceQM(Histogram rgbHist) {
        double[] max_ = rgbHist.getHighValue();
        double Rmax = max_[0],
                Gmax = max_[1],
                Bmax = max_[2];

        double R2max = Rmax * Rmax;
        double G2max = Gmax * Gmax;
        double B2max = Bmax * Bmax;

        double Rsum = 0, R2sum = 0;
        double Gsum = 0;
        double Bsum = 0, B2sum = 0;
        int[] bins = rgbHist.getBins(0);
        for (int i = 0; i < bins.length; i++) {
            Rsum += bins[i] * i;
            R2sum += bins[i] * i * i;
        }
        bins = rgbHist.getBins(1);
        for (int i = 0; i < bins.length; i++) {
            Gsum += bins[i] * i;
        }
        bins = rgbHist.getBins(2);
        for (int i = 0; i < bins.length; i++) {
            Bsum += bins[i] * i;
            B2sum += bins[i] * i * i;
        }

        double[] Rmunu = LinearEquations.solve(R2sum, Rsum, R2max, Rmax, Gsum, Gmax);
        double[] Bmunu = LinearEquations.solve(B2sum, Bsum, B2max, Bmax, Gsum, Gmax);

        double[] vector = {
            Rmunu[0], Rmunu[1], Bmunu[0], Bmunu[1]//
        };
        return vector;
    }

    public static double[] whiteBalanceQM(long[][] rgbBins) {
        double Rmax = 0,
                Gmax = 0,
                Bmax = 0;


        double Rsum = 0, R2sum = 0;
        double Gsum = 0;
        double Bsum = 0, B2sum = 0;
        long[] bins = rgbBins[0];
        for (int i = 0; i < bins.length; i++) {
            Rsum += bins[i] * i;
            R2sum += bins[i] * i * i;
            if (bins[i] != 0) {
                Rmax = i;
            }
        }
        bins = rgbBins[1];
        for (int i = 0; i < bins.length; i++) {
            Gsum += bins[i] * i;
            if (bins[i] != 0) {
                Gmax = i;
            }
        }
        bins = rgbBins[2];
        for (int i = 0; i < bins.length; i++) {
            Bsum += bins[i] * i;
            B2sum += bins[i] * i * i;
            if (bins[i] != 0) {
                Bmax = i;
            }
        }

        double R2max = Rmax * Rmax;
        double G2max = Gmax * Gmax;
        double B2max = Bmax * Bmax;


        double[] Rmunu = LinearEquations.solve(R2sum, Rsum, R2max, Rmax, Gsum, Gmax);
        double[] Bmunu = LinearEquations.solve(B2sum, Bsum, B2max, Bmax, Gsum, Gmax);

        double[] vector = {
            Rmunu[0], Rmunu[1], Bmunu[0], Bmunu[1]//
        };
        return vector;
    }
}
