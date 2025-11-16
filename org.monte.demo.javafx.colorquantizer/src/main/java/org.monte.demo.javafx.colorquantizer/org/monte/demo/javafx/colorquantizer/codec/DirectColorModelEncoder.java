/*
 * @(#)DirectColorModelEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.codec;

import org.monte.demo.javafx.colorquantizer.model.DitheringMethod;
import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys;
import org.monte.media.color.OKLabColorSpace;
import org.monte.media.color.dither.BayerDither;
import org.monte.media.color.dither.BlueNoiseDither;
import org.monte.media.color.dither.Dither;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/**
 * Encodes a {@link BufferedImage} into a {@link BufferedImage} with a different {@link java.awt.image.DirectColorModel}.
 */
public class DirectColorModelEncoder extends AbstractCodec {
    private final OKLabColorSpace oklab = new OKLabColorSpace();

    /**
     * Optional, the desired index color model.
     * If this value is null, an index color model is computed.
     */
    public final static FormatKey<DirectColorModel> DirectColorModelKey = new FormatKey<>("directColorModel", DirectColorModel.class);
    /**
     * Dithering pattern is multiplied by the specified factor.
     */
    public final static FormatKey<Double> DitheringFactorKey = new FormatKey<>("ditheringFactor", Double.class);
    /**
     * Optional, the desired number of colors in the index color model.
     * This value is only used when no {@link #DirectColorModelKey} has been provided.
     */
    public final static FormatKey<DitheringMethod> DitheringMethodKey = new FormatKey<>("ditheringMethod", DitheringMethod.class);

    public DirectColorModelEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                }//
        );
        name = "Crop Image";
    }

    private Dither dither0, dither1, dither2;

    @Override
    public Format setOutputFormat(Format f) {
        if (!f.containsKey(DirectColorModelKey)) {
            throw new IllegalArgumentException("Output format must specify direct color model.");
        }
        DitheringMethod ditheringMethod = f.get(DitheringMethodKey);
        if (ditheringMethod == null) {
            ditheringMethod = DitheringMethod.BLUE_NOISE;
        }

        float spread = f.get(DitheringFactorKey, 4.0).floatValue();

        switch (ditheringMethod) {
            case NONE -> {
                dither0 = dither1 = dither2 = null;
            }
            case BAYER_4x4 -> {
                dither0 = dither1 = dither2 = new BayerDither(4, spread);
            }
            case BAYER_8x8 -> {
                dither0 = dither1 = dither2 = new BayerDither(8, spread);
            }
            case BLUE_NOISE -> {
                dither0 = new BlueNoiseDither(0, spread);
                dither1 = new BlueNoiseDither(1, spread);
                dither2 = new BlueNoiseDither(2, spread);
            }
        }


        return super.setOutputFormat(f);
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;

        BufferedImage inputImage = (BufferedImage) in.data;
        DirectColorModel dcm = outputFormat.get(DirectColorModelKey);
        BufferedImage outputImage = reuseOutputImage(inputImage, out.data instanceof BufferedImage b ? b : null, dcm);
        if (dither0 == null) {
            renderOutputImageWithoutDithering(inputImage, outputImage, dcm);
        } else {
            renderOutputImageWithDithering(inputImage, outputImage, dcm, dither0, dither1, dither2);
        }

        out.data = outputImage;

        return CODEC_OK;
    }

    private void renderOutputImageWithoutDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm) {
        Graphics2D g = outputImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.drawImage(inputImage, 0, 0, null);
    }

    private void renderOutputImageWithDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            renderOutputImageInt(inputImage, outputImage, dcm, dither0, dither1, dither2);
        }
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferShort
                || outputImage.getRaster().getDataBuffer() instanceof DataBufferUShort) {
            renderOutputImageShort(inputImage, outputImage, dcm, dither0, dither1, dither2);
        }
    }

    private void renderOutputImageInt(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int[] in = new int[width * height];
        inputImage.getRGB(0, 0, width, height, in, 0, width);
        int[] out;
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt db) {
            out = db.getData();
        } else {
            return;
        }
        float[] rgbs = new float[3];
        float[] rgbs2 = new float[3];
        float[] lab = new float[3];
        int rm = dcm.getRedMask();
        int gm = dcm.getGreenMask();
        int bm = dcm.getBlueMask();
        int compressMask =
                (((1 << Integer.bitCount(rm)) - 1) << (24 - Integer.bitCount(rm)))
                        | (((1 << Integer.bitCount(gm)) - 1) << (16 - Integer.bitCount(gm)))
                        | (((1 << Integer.bitCount(bm)) - 1) << (8 - Integer.bitCount(bm)));
        int x = 0, y = 0;
        for (int i = 0, n = Math.min(in.length, out.length); i < n; i++) {
            var pix = in[i];
            rgbs[0] = (pix & 0xff0000) >> 16;
            rgbs[1] = (pix & 0xff00) >> 8;
            rgbs[2] = (pix & 0xff);
            lab = oklab.fromRGB(rgbs, lab);
            lab[0] += dither0.get(x, y);
            lab[1] += dither1.get(x, y);
            lab[2] += dither2.get(x, y);
            oklab.toRGB(lab, rgbs2);
            var dpix = (Math.clamp((int) rgbs2[0], 0, 255) << 16)
                    | (Math.clamp((int) rgbs2[1], 0, 255) << 8)
                    | (Math.clamp((int) rgbs2[2], 0, 255));
            out[i] = Integer.compress(dpix, compressMask);
            x = x + 1;
            if (x > width) {
                x = 0;
                y++;
            }
        }
    }

    private void renderOutputImageShort(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int[] in = new int[width * height];
        inputImage.getRGB(0, 0, width, height, in, 0, width);
        short[] out;
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferShort db) {
            out = db.getData();
        } else if (outputImage.getRaster().getDataBuffer() instanceof DataBufferUShort db) {
            out = db.getData();
        } else {
            return;
        }
        float[] rgbs = new float[3];
        float[] rgbs2 = new float[3];
        float[] labf = new float[3];
        int rm = dcm.getRedMask();
        int gm = dcm.getGreenMask();
        int bm = dcm.getBlueMask();
        int compressMask =
                (((1 << Integer.bitCount(rm)) - 1) << (24 - Integer.bitCount(rm)))
                        | (((1 << Integer.bitCount(gm)) - 1) << (16 - Integer.bitCount(gm)))
                        | (((1 << Integer.bitCount(bm)) - 1) << (8 - Integer.bitCount(bm)));
        int x = 0, y = 0;
        for (int i = 0, n = Math.min(in.length, out.length); i < n; i++) {
            var pix = in[i];
            rgbs[0] = (pix & 0xff0000) >> 16;
            rgbs[1] = (pix & 0xff00) >> 8;
            rgbs[2] = (pix & 0xff);
            labf = oklab.fromRGB(rgbs, labf);
            labf[0] += dither0.get(x, y);
            labf[1] += dither1.get(x, y);
            labf[2] += dither2.get(x, y);
            oklab.toRGB(labf, rgbs2);
            var dpix = (Math.clamp((int) rgbs2[0], 0, 255) << 16)
                    | (Math.clamp((int) rgbs2[1], 0, 255) << 8)
                    | (Math.clamp((int) rgbs2[2], 0, 255));
            out[i] = (short) Integer.compress(dpix, compressMask);
            x = x + 1;
            if (x > width) {
                x = 0;
                y++;
            }
        }
    }

    private BufferedImage reuseOutputImage(BufferedImage input, BufferedImage output, DirectColorModel dcm) {
        int w = input.getWidth();
        int h = input.getHeight();
        if (output != null
                && output.getWidth() == w
                && output.getHeight() == h
                && output.getColorModel() == dcm) {
            return output;
        }
        WritableRaster raster = dcm.createCompatibleWritableRaster(w, h);
        return new BufferedImage(dcm, raster, false, null);
    }

}
