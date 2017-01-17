/* @(#)CMYKImages
 * Copyright (c) 2016 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import ru.sbtqa.monte.media.color.ICCPackedColorModel;
import ru.sbtqa.monte.media.util.stream.BiIntConsumer;
import ru.sbtqa.monte.media.util.stream.RangeStream;

/**
 * Provides utility methods for images in the CMYK color space..
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class CMYKImages {

    public static final DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

    /**
     * Creates a CMYK image from a raster in the YCCK color space.
     *
     * @param ycckRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile this profile is assigned to the resulting CMYK image
     * @return a BufferedImage in the CMYK color space
     */
    public static BufferedImage createImageFromYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        return createImageFromCMYK(convertYCCKtoCMYK(ycckRaster), cmykProfile);
    }

    /**
     * Creates a buffered image from a raster in the inverted YCCK color space,
     * converting the colors to RGB using the provided CMYK ICC_Profile.
     *
     * @param ycckRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage
     */
    public static BufferedImage createImageFromInvertedYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        return createImageFromCMYK(convertInvertedYCCKToCMYK(ycckRaster), cmykProfile);
    }

    /**
     * Creates a buffered image from a raster in the color space specified by
     * the given ICC_Profile.
     *
     * @param raster A raster.
     * @param profile An ICC_Profile specifying the color space of the raster.
     * @return a BufferedImage in the color space specified by the profile.
     */
    public static BufferedImage createImageFromICCProfile(Raster raster, ICC_Profile profile) {
        ICC_ColorSpace cs = new ICC_ColorSpace(profile);
        WritableRaster r = (WritableRaster) raster;

        ColorModel cm;
        if (raster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, raster.getTransferType());
        } else {
            cm = new ICCPackedColorModel(cs, raster);
        }
        return new BufferedImage(cm, (WritableRaster) raster, cm.isAlphaPremultiplied(), null);
    }

    /**
     * Creates a buffered image from a CMYK raster using the provided CMYK
     * ICC_Profile.
     *
     * @param cmykRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile An ICC_Profile in the CMYK color space. If this
     * parameter is null, a default profile is used, and the returned image will
     * be an RGB image. !!!! FIXME always create a CMYK image!
     * @return a BufferedImage.
     */
    public static BufferedImage createImageFromCMYK(final Raster cmykRaster, ICC_Profile cmykProfile) {
        if (cmykProfile != null) {
            return createImageFromICCProfile(cmykRaster, cmykProfile);
        } else {
            // => There is no color profile. 
            // Convert image to RGB using a simple conversion algorithm.
            final int w = cmykRaster.getWidth();
            final int h = cmykRaster.getHeight();

            final int[] rgb = new int[w * h];

            final int[][] cmyk = new int[4][0];
            RangeStream.
                    range(0, 4).
                    forEach(new IntConsumer() {
                        @Override
                        public void accept(int i) {
                            cmyk[i] = cmykRaster.getSamples(0, 0, w, h, i, (int[]) null);
                        }
                    });
            final int[] C = cmyk[0];
            final int[] M = cmyk[1];
            final int[] Y = cmyk[2];
            final int[] K = cmyk[3];

            // Split the array into bands and process each band in parallel.
            // for (int i=0;i<rgb.length;i++) {
            RangeStream.range(0, rgb.length).parallel().forEach(new BiIntConsumer() {
                @Override
                public void accept(int lo, int hi) {
                    for (int i = lo; i < hi; i++) {
                        int k = min(255, K[i]);
                        rgb[i] = (255 - min(255, C[i] + k)) << 16
                                | (255 - min(255, M[i] + k)) << 8
                                | (255 - min(255, Y[i] + k));
                    }
                }
            });

            Hashtable<Object, Object> properties = new Hashtable<Object, Object>();
            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorModel cm = RGB;//new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);
            return new BufferedImage(cm, (WritableRaster) rgbRaster, cm.isAlphaPremultiplied(), properties);
        }
    }

    /**
     * Creates a buffered image from a raster in the RGBW color space.
     *
     * As seen from a comment made by 'phelps' at
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799903
     *
     * @param rgbwRaster A raster with inverted CMYK values (=RGBW).
     * @param cmykProfile An ICC_Profile. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage in the RGB color space.
     */
    public static BufferedImage createImageFromInvertedCMYK(final Raster rgbwRaster, ICC_Profile cmykProfile) {
        final int w = rgbwRaster.getWidth();
        final int h = rgbwRaster.getHeight();

        try {

            CompletableFuture<int[]> cfR = CompletableFuture.supplyAsync(new Supplier<int[]>() {
                @Override
                public int[] get() {
                    return rgbwRaster.getSamples(0, 0, w, h, 0, (int[]) null);
                }

            });

            CompletableFuture<int[]> cfG = CompletableFuture.supplyAsync(new Supplier<int[]>() {
                @Override
                public int[] get() {
                    return rgbwRaster.getSamples(0, 0, w, h, 1, (int[]) null);
                }
            });

            CompletableFuture<int[]> cfB = CompletableFuture.supplyAsync(new Supplier<int[]>() {
                @Override
                public int[] get() {
                    return rgbwRaster.getSamples(0, 0, w, h, 2, (int[]) null);
                }
            });

            CompletableFuture<int[]> cfW = CompletableFuture.supplyAsync(new Supplier<int[]>() {
                @Override
                public int[] get() {
                    return rgbwRaster.getSamples(0, 0, w, h, 3, (int[]) null);
                }
            });

            final int[] rgb = new int[w * h];
            final int[] R = cfR.get();
            final int[] G = cfG.get();
            final int[] B = cfB.get();
            final int[] W = cfW.get();

            // Split the rgb array into bands and process each band in parallel.
            // for (int i=0;i<rgb.length;i++) {
            RangeStream.range(0, rgb.length).parallel().forEach(
                    new BiIntConsumer() {
                @Override
                public void accept(int lo, int hi) {
                    for (int i = lo; i < hi; i++) {
                        rgb[i] = (255 - W[i]) << 24 | (255 - R[i]) << 16 | (255 - G[i]) << 8 | (255 - B[i]) << 0;
                    }
                }
            });

            Raster packedRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
            return createImageFromCMYK(packedRaster, cmykProfile);
        } catch (ExecutionException | InterruptedException e) {
            throw new InternalError(e);
        }
    }

    private static Supplier<int[]> getSupplier(final Raster rgbRaster, final int z) {
        return new Supplier<int[]>() {
            @Override
            public int[] get() {
                return rgbRaster.getSamples(0, 0, rgbRaster.getWidth(), rgbRaster.getHeight(), z, (int[]) null);
            }
        };
    }

    public static BufferedImage createImageFromRGB(Raster rgbRaster, ICC_Profile rgbProfile) {
        if (rgbProfile != null) {
            return createImageFromICCProfile(rgbRaster, rgbProfile);
        } else {
            BufferedImage image;
            int w = rgbRaster.getWidth();
            int h = rgbRaster.getHeight();

            try {
                CompletableFuture<int[]> cfR = CompletableFuture.supplyAsync(getSupplier(rgbRaster, 0));
                CompletableFuture<int[]> cfG = CompletableFuture.supplyAsync(getSupplier(rgbRaster, 1));
                CompletableFuture<int[]> cfB = CompletableFuture.supplyAsync(getSupplier(rgbRaster, 2));
                final int[] rgb = new int[w * h];
                final int[] R = cfR.get();
                final int[] G = cfG.get();
                final int[] B = cfB.get();

                // Split the rgb array into bands and process each band in parallel.
                // for (int i=0;i<rgb.length;i++) {
                RangeStream.range(0, rgb.length).parallel().forEach(new BiIntConsumer() {
                    @Override
                    public void accept(int lo, int hi) {
                        for (int i = lo; i < hi; i++) {
                            rgb[i] = 0xff << 24 | R[i] << 16 | G[i] << 8 | B[i];
                        }
                    }
                });

                WritableRaster packedRaster = Raster.createPackedRaster(
                        new DataBufferInt(rgb, rgb.length),
                        w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
                Hashtable<Object, Object> properties = new Hashtable<>();
                return new BufferedImage(cm, packedRaster, cm.isAlphaPremultiplied(), properties);
            } catch (ExecutionException | InterruptedException e) {
                throw new InternalError(e);
            }
        }
    }

    public static BufferedImage createImageFromYCC(Raster yccRaster, ICC_Profile yccProfile) {
        if (yccProfile != null) {
            return createImageFromICCProfile(yccRaster, yccProfile);
        } else {
            BufferedImage image;
            int w = yccRaster.getWidth();
            int h = yccRaster.getHeight();

            try {
                CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(getSupplier(yccRaster, 0));
                CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(getSupplier(yccRaster, 1));
                CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(getSupplier(yccRaster, 2));
                final int[] rgb = new int[w * h];
                final int[] Y = cfY.get();
                final int[] Cb = cfCb.get();
                final int[] Cr = cfCr.get();

                // Split the rgb array into bands and process each band in parallel.
                // for (int i=0;i<rgb.length;i++) {
                RangeStream.range(0, rgb.length).parallel().forEach(new BiIntConsumer() {
                    @Override
                    public void accept(int lo, int hi) {
                        for (int i = lo; i < hi; i++) {
                            int Yi, Cbi, Cri;
                            int R, G, B;

                            //RGB can be computed directly from YCbCr (256 levels) as follows:
                            //R = Y + 1.402 (Cr-128)
                            //G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128) 
                            //B = Y + 1.772 (Cb-128)
                            Yi = Y[i];
                            Cbi = Cb[i];
                            Cri = Cr[i];
                            R = (1000 * Yi + 1402 * (Cri - 128)) / 1000;
                            G = (100000 * Yi - 34414 * (Cbi - 128) - 71414 * (Cri - 128)) / 100000;
                            B = (1000 * Yi + 1772 * (Cbi - 128)) / 1000;

                            R = min(255, max(0, R));
                            G = min(255, max(0, G));
                            B = min(255, max(0, B));

                            rgb[i] = 0xff << 24 | R << 16 | G << 8 | B;
                        }
                    }
                });
                Hashtable<Object, Object> properties = new Hashtable<>();
                Raster rgbRaster = Raster.createPackedRaster(
                        new DataBufferInt(rgb, rgb.length),
                        w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
                return new BufferedImage(cm, (WritableRaster) rgbRaster, cm.isAlphaPremultiplied(), properties);
            } catch (ExecutionException | InterruptedException e) {
                throw new InternalError(e);
            }
        }
    }
    /**
     * Define tables for YCC->RGB color space conversion.
     */
    private final static int SCALEBITS = 16;
    private final static int MAXJSAMPLE = 255;
    private final static int CENTERJSAMPLE = 128;
    private final static int ONE_HALF = 1 << (SCALEBITS - 1);
    private final static int[] Cr_r_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_b_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cr_g_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_g_tab = new int[MAXJSAMPLE + 1];

    /*
     * Initialize tables for YCC->RGB colorspace conversion.
     */
    private static synchronized void buildYCCtoRGBtable() {
        if (Cr_r_tab[0] == 0) {
            for (int i = 0, x = -CENTERJSAMPLE; i <= MAXJSAMPLE; i++, x++) {
                // i is the actual input pixel value, in the range 0..MAXJSAMPLE/
                // The Cb or Cr value we are thinking of is x = i - CENTERJSAMPLE 
                // Cr=>R value is nearest int to 1.40200 * x
                Cr_r_tab[i] = (int) ((1.40200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
                // Cb=>B value is nearest int to 1.77200 * x 
                Cb_b_tab[i] = (int) ((1.77200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
                // Cr=>G value is scaled-up -0.71414 * x 
                Cr_g_tab[i] = -(int) (0.71414 * (1 << SCALEBITS) + 0.5) * x;
                // Cb=>G value is scaled-up -0.34414 * x 
                // We also add in ONE_HALF so that need not do it in inner loop 
                Cb_g_tab[i] = -(int) ((0.34414) * (1 << SCALEBITS) + 0.5) * x + ONE_HALF;
            }
        }
    }

    /*
     * Adobe-style YCCK->CMYK conversion.
     * We convert YCbCr to C, M, Y, while passing K (black) unchanged.
     */
    static Raster convertInvertedYCCKToCMYK(Raster ycckRaster) {
        return convertInvertedYCCKToCMYK_byBytes(ycckRaster);
    }

    /**
     * Fastest method but may not always work.
     *
     * @param ycckRaster a YCCK raster
     * @return a CMYK raster
     */
    private static Raster convertInvertedYCCKToCMYK_byBytes(Raster ycckRaster) {
        buildYCCtoRGBtable();
        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

        if (!(ycckRaster.getDataBuffer() instanceof DataBufferByte)) {
            return convertInvertedYCCKToCMYK_byPixels(ycckRaster);
        }

        // XXX foolishly assume that raster width = w, raster height=h, and scanline stride = 4*w
        final byte[] ycck = ((DataBufferByte) ycckRaster.getDataBuffer()).getData();
        final int[] cmyk = new int[w * h];

        // Split the cmyk array into bands and process each band in parallel.
        // for (int i=0;i<cmyk.length;i++) {
        RangeStream.range(0, cmyk.length).parallel().forEach(new BiIntConsumer() {
            @Override
            public void accept(int lo, int hi) {
                for (int i = lo; i < hi; i++) {
                    int j = i * 4;
                    int y = 255 - (ycck[j] & 0xff);
                    int cb = 255 - (ycck[j + 1] & 0xff);
                    int cr = 255 - (ycck[j + 2] & 0xff);
                    int k = 255 - (ycck[j + 3] & 0xff);
                    // Range-limiting is essential due to noise introduced by DCT losses.
                    int cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
                    int cmykM = MAXJSAMPLE - (y + (Cb_g_tab[cb] + Cr_g_tab[cr] >> SCALEBITS));
                    int cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);
                    // k passes through unchanged 
                    cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                            | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                            | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                            | k;
                }
            }
        });

        Raster cmykRaster = Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
        return cmykRaster;

    }

    /**
     * This is slightly faster than _bySamples and does not use any internal
     * APIs.
     */
    private static Raster convertInvertedYCCKToCMYK_byPixels(Raster ycckRaster) {
        buildYCCtoRGBtable();
        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

        final int[] ycck = ycckRaster.getPixels(0, 0, w, h, (int[]) null);

        final int[] cmyk = new int[w * h];

        // Split the cmyk array into bands and process each band in parallel.
        // for (int i=0;i<cmyk.length;i++) {
        RangeStream.range(0, cmyk.length).parallel().forEach(new BiIntConsumer() {
            @Override
            public void accept(int lo, int hi) {
                for (int i = lo; i < hi; i++) {
                    int j = i * 4;
                    int y = 255 - ycck[j];
                    int cb = 255 - ycck[j + 1];
                    int cr = 255 - ycck[j + 2];
                    int cmykC, cmykM, cmykY;
                    // Range-limiting is essential due to noise introduced by DCT losses.
                    cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
                    cmykM = MAXJSAMPLE - (y + (Cb_g_tab[cb] + Cr_g_tab[cr] >> SCALEBITS));
                    cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);
                    // K passes through unchanged 
                    cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                            | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                            | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                            | 255 - ycck[j + 3];
                }
            }
        });
//      }
        Raster cmykRaster = Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
        return cmykRaster;
    }

    /**
     * This is slower but does not use any internal APIs.
     */
    private static Raster convertInvertedYCCKToCMYK_bySamples(Raster ycckRaster) {
        buildYCCtoRGBtable();
        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

        try {
            CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 0));
            CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 1));
            CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 2));
            CompletableFuture<int[]> cfK = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 3));
            final int[] cmyk = new int[w * h];
            final int[] ycckY = cfY.get();
            final int[] ycckCb = cfCb.get();
            final int[] ycckCr = cfCr.get();
            final int[] ycckK = cfK.get();

            // Split the cmyk array into bands and process each band in parallel.
            // for (int i=0;i<cmyk.length;i++) {
            RangeStream.range(0, cmyk.length).parallel().forEach(new BiIntConsumer() {
                @Override
                public void accept(int lo, int hi) {
                    for (int i = lo; i < hi; i++) {
                        int y = 255 - ycckY[i];
                        int cb = 255 - ycckCb[i];
                        int cr = 255 - ycckCr[i];
                        int cmykC, cmykM, cmykY;
                        // Range-limiting is essential due to noise introduced by DCT losses.
                        cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
                        cmykM = MAXJSAMPLE - (y
                                + // green
                                (Cb_g_tab[cb] + Cr_g_tab[cr]
                                >> SCALEBITS));
                        cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
                        // K passes through unchanged 
                        cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                                | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                                | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                                | 255 - ycckK[i];
                    }
                }
            });

            Raster cmykRaster = Raster.createPackedRaster(
                    new DataBufferInt(cmyk, cmyk.length),
                    w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
            return cmykRaster;
        } catch (InterruptedException | ExecutionException ex) {
            throw new InternalError(ex);
        }
    }

    private static Raster convertYCCKtoCMYK(Raster ycckRaster) {
        buildYCCtoRGBtable();

        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
        try {
            CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 0));
            CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 1));
            CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 2));
            CompletableFuture<int[]> cfK = CompletableFuture.supplyAsync(getSupplier(ycckRaster, 3));
            final int[] cmyk = new int[w * h];
            final int[] ycckY = cfY.get();
            final int[] ycckCb = cfCb.get();
            final int[] ycckCr = cfCr.get();
            final int[] ycckK = cfK.get();

            // Split the cmyk array into bands and process each band in parallel.
            // for (int i=0;i<cmyk.length;i++) {
            RangeStream.range(0, cmyk.length).parallel().forEach(new BiIntConsumer() {
                @Override
                public void accept(int lo, int hi) {
                    for (int i = lo; i < hi; i++) {
                        int y = ycckY[i];
                        int cb = ycckCb[i];
                        int cr = ycckCr[i];
                        int cmykC, cmykM, cmykY;
                        // Range-limiting is essential due to noise introduced by DCT losses.
                        cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
                        cmykM = MAXJSAMPLE - (y
                                + // green
                                (Cb_g_tab[cb] + Cr_g_tab[cr]
                                >> SCALEBITS));
                        cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
                        // K passes through unchanged
                        cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                                | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                                | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                                | ycckK[i];
                    }
                }
            });

            return Raster.createPackedRaster(
                    new DataBufferInt(cmyk, cmyk.length),
                    w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
        } catch (InterruptedException | ExecutionException e) {
            throw new InternalError(e);
        }
    }
}
