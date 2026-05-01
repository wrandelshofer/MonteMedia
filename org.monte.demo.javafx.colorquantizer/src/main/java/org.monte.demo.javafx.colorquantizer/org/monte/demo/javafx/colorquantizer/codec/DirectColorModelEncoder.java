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
import java.util.stream.IntStream;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Encodes a [BufferedImage] into a [BufferedImage] with a different [java.awt.image.DirectColorModel].
public class DirectColorModelEncoder extends AbstractCodec {
    private final OKLabColorSpace oklab = new OKLabColorSpace();

    /// Optional, the desired index color model.
    /// If this value is null, an index color model is computed.
    public final static FormatKey<DirectColorModel> DirectColorModelKey = new FormatKey<>("directColorModel", DirectColorModel.class);
    /// Dithering pattern is multiplied by the specified factor.
    public final static FormatKey<Double> DitheringFactorKey = new FormatKey<>("ditheringFactor", Double.class);
    /// Optional, the desired number of colors in the index color model.
    /// This value is only used when no [#DirectColorModelKey] has been provided.
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

        // Divide by 255f because we dither in the oklab domain
        float spread = f.get(DitheringFactorKey, 4.0).floatValue() / 255f;

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
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            renderOutputImageIntWithoutDithering(inputImage, outputImage, dcm);
            return;
        }
        Graphics2D g = outputImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
    }

    private void renderOutputImageWithoutDitheringFake(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm) {
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            renderOutputImageIntWithoutDitheringFake(inputImage, outputImage, dcm);
            return;
        }
        Graphics2D g = outputImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
    }

    private void renderOutputImageWithDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            renderOutputImageIntWithDithering(inputImage, outputImage, dcm, dither0, dither1, dither2);
        } else if (outputImage.getRaster().getDataBuffer() instanceof DataBufferShort
                || outputImage.getRaster().getDataBuffer() instanceof DataBufferUShort) {
            renderOutputImageShortWithDithering(inputImage, outputImage, dcm, dither0, dither1, dither2);
        }
    }

    private void renderOutputImageIntWithDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {
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
        int block = 64;
        IntStream.range(0, height / block + 1).parallel().forEach(yy -> {
            float[] buf = new float[3];
            int rmask = dcm.getRedMask();
            int gmask = dcm.getGreenMask();
            int bmask = dcm.getBlueMask();
            int rshift = Integer.numberOfTrailingZeros(rmask);
            int gshift = Integer.numberOfTrailingZeros(gmask);
            int bshift = Integer.numberOfTrailingZeros(bmask);
            int rfactor = (1 << Integer.bitCount(rmask)) - 1;
            int gfactor = (1 << Integer.bitCount(gmask)) - 1;
            int bfactor = (1 << Integer.bitCount(bmask)) - 1;

            for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    int pix = in[i];

                    buf = oklab.from24BitRGB(pix, buf);
                    buf[0] += dither0.get(x, y);
                    buf[1] += dither1.get(x, y);
                    buf[2] += dither2.get(x, y);
                    buf = oklab.toRGB(buf);
                    var dpix = (Math.clamp((int) (buf[0] * rfactor), 0, rfactor) << rshift) & rmask
                            | (Math.clamp((int) (buf[1] * gfactor), 0, gfactor) << gshift) & gmask
                            | (Math.clamp((int) (buf[2] * bfactor), 0, bfactor) << bshift) & bmask;
                    out[i] = dpix;
                }
            }
        });
    }

    private void renderOutputImageIntWithoutDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm) {
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
        int block = 64;
        // IntStream.range(0, height / block + 1).parallel().forEach(yy -> {
        float[] rgbs = new float[3];
        int rmask = dcm.getRedMask();
        int gmask = dcm.getGreenMask();
        int bmask = dcm.getBlueMask();
        int rshift = Integer.numberOfTrailingZeros(rmask);
        int gshift = Integer.numberOfTrailingZeros(gmask);
        int bshift = Integer.numberOfTrailingZeros(bmask);
        int rfactor = (1 << Integer.bitCount(rmask)) - 1;
        int gfactor = (1 << Integer.bitCount(gmask)) - 1;
        int bfactor = (1 << Integer.bitCount(bmask)) - 1;

        //  for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                int pix = in[i];
                rgbs[0] = ((pix & 0xff0000) >> 16) * (1f / 255f);
                rgbs[1] = ((pix & 0xff00) >> 8) * (1f / 255f);
                rgbs[2] = (pix & 0xff) * (1f / 255f);

                var dpix = (Math.clamp((int) (rgbs[0] * rfactor), 0, rfactor) << rshift) & rmask
                        | (Math.clamp((int) (rgbs[1] * gfactor), 0, gfactor) << gshift) & gmask
                        | (Math.clamp((int) (rgbs[2] * bfactor), 0, bfactor) << bshift) & bmask;
                out[i] = dpix;
            }
        }
    }

    private void renderOutputImageIntWithoutDitheringFake(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int[] in = new int[width * height];
        //now we have converted the image to sRGB!
        inputImage.getRGB(0, 0, width, height, in, 0, width);
        int[] out;
        if (outputImage.getRaster().getDataBuffer() instanceof DataBufferInt db) {
            out = db.getData();
        } else {
            return;
        }
        int rmask = dcm.getRedMask();
        int gmask = dcm.getGreenMask();
        int bmask = dcm.getBlueMask();
        int rshift = Integer.numberOfTrailingZeros(rmask);
        int gshift = Integer.numberOfTrailingZeros(gmask);
        int bshift = Integer.numberOfTrailingZeros(bmask);
        int rfactor = (1 << Integer.bitCount(rmask)) - 1;
        int gfactor = (1 << Integer.bitCount(gmask)) - 1;
        int bfactor = (1 << Integer.bitCount(bmask)) - 1;

        //  for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                int pix = in[i];
                int dpix = (pix & 0xf0f0f0f0) | ((pix & 0xf0f0f0f0) >>> 8);
                out[i] = 0xff00ff00 | pix;
            }
        }
    }

    private void renderOutputImageShortWithDithering(BufferedImage inputImage, BufferedImage outputImage, DirectColorModel dcm, Dither dither0, Dither dither1, Dither dither2) {

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
        int block = 64;
        IntStream.range(0, height / block + 1).parallel().forEach(yy -> {
            float[] rgbs = new float[3];
            int rmask = dcm.getRedMask();
            int gmask = dcm.getGreenMask();
            int bmask = dcm.getBlueMask();
            int rshift = Integer.numberOfTrailingZeros(rmask);
            int gshift = Integer.numberOfTrailingZeros(gmask);
            int bshift = Integer.numberOfTrailingZeros(bmask);
            int rfactor = (1 << Integer.bitCount(rmask)) - 1;
            int gfactor = (1 << Integer.bitCount(gmask)) - 1;
            int bfactor = (1 << Integer.bitCount(bmask)) - 1;

            for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    var pix = in[i];
                    rgbs[0] = ((pix & 0xff0000) >> 16) * (1f / 255f);
                    rgbs[1] = ((pix & 0xff00) >> 8) * (1f / 255f);
                    rgbs[2] = (pix & 0xff) * (1f / 255f);
                    rgbs = oklab.fromRGB(rgbs);
                    rgbs[0] += dither0.get(x, y);
                    rgbs[1] += dither1.get(x, y);
                    rgbs[2] += dither2.get(x, y);
                    rgbs = oklab.toRGB(rgbs);
                    var dpix = (Math.clamp((int) (rgbs[0] * rfactor), 0, rfactor) << rshift) & rmask
                            | (Math.clamp((int) (rgbs[1] * gfactor), 0, gfactor) << gshift) & gmask
                            | (Math.clamp((int) (rgbs[2] * bfactor), 0, bfactor) << bshift) & bmask;
                    out[i] = (short) dpix;
                }
            }
        });
    }

    private BufferedImage reuseOutputImage(BufferedImage input, BufferedImage output, DirectColorModel dcm) {
        int w = input.getWidth();
        int h = input.getHeight();
        if (output != null
                && output.getWidth() == w
                && output.getHeight() == h
                && output.getColorModel() == dcm
                && output.getType() == input.getType()) {
            return output;
        }
        WritableRaster raster = dcm.createCompatibleWritableRaster(w, h);
        return new BufferedImage(dcm, raster, false, null);
    }

}
