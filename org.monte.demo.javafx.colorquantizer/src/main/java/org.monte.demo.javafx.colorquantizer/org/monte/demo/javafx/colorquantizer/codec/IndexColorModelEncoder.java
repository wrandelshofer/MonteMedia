/*
 * @(#)IndexColorModelEncoder.java
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
import org.monte.media.color.dither.BlueNoiseDither128;
import org.monte.media.color.dither.Dither;
import org.monte.media.color.kmeans.Simple3DDistanceMatrix;
import org.monte.media.color.quant.KMeansColorQuantizer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Encodes a [BufferedImage] into a [BufferedImage] with a [IndexColorModel].
public class IndexColorModelEncoder extends AbstractCodec {
    /// The number of bits per color channel.
    ///
    /// The default value is 8.
    public final static FormatKey<Integer> BitsPerColorKey = new FormatKey<>("bitsPerColor", Integer.class);
    /// Dithering pattern is multiplied by the specified factor.
    public final static FormatKey<Double> DitheringFactorKey = new FormatKey<>("ditheringFactor", Double.class);
    /// Optional, the desired number of colors in the index color model.
    /// This value is only used when no [#IndexColorModelKey] has been provided.
    public final static FormatKey<DitheringMethod> DitheringMethodKey = new FormatKey<>("ditheringMethod", DitheringMethod.class);
    /// Optional, the desired index color model.
    /// If this value is null, an index color model is computed.
    public final static FormatKey<IndexColorModel> IndexColorModelKey = new FormatKey<>("indexColorModel", IndexColorModel.class);
    /// Optional, a color map with reserved colors.
    /// If this value is null, no colors are reserved.
    /// If this value is non-null and [#IndexColorModelKey] is null, the computed color model will start with the specified reserved colors.
    public final static FormatKey<IndexColorModel> ReservedColorsKey = new FormatKey<>("reservedColors", IndexColorModel.class);
    /// Optional, the desired number of colors in the index color model.
    /// This value is only used when no [#IndexColorModelKey] has been provided.
    /// The default value is 16.
    public final static FormatKey<Integer> NumberOfColorsKey = new FormatKey<>("numberOfColors", Integer.class);
    private final OKLabColorSpace oklab = new OKLabColorSpace();
    private Dither dither0, dither1, dither2;

    public IndexColorModelEncoder() {
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

    private IndexColorModel createIndexColorModel(BufferedImage data, Format outputFormat) {
        IndexColorModel indexColorModel = outputFormat.get(IndexColorModelKey);
        if (indexColorModel != null) {
            return indexColorModel;
        }
        Integer depth = outputFormat.get(DepthKey);
        Integer numberOfColors = outputFormat.get(NumberOfColorsKey);
        if (depth == null) {
            depth = numberOfColors == null ? 4 : 31 - Integer.numberOfLeadingZeros(numberOfColors);
        }
        if (numberOfColors == null) {
            numberOfColors = 1 << depth;
        }
        IndexColorModel reservedColors = outputFormat.get(ReservedColorsKey);
        int availableColors = numberOfColors;
        if (reservedColors != null) {
            availableColors = Math.max(0, availableColors - reservedColors.getMapSize());
        }
        IndexColorModel icm;
        if (availableColors > 0) {
            var e = new KMeansColorQuantizer(availableColors);
            e.addImage(data);
            icm = e.computeColorPalette();
            if (outputFormat.get(BitsPerColorKey, 8) == 4) {
                int[] map = new int[icm.getMapSize()];
                icm.getRGBs(map);
                for (int i = 0; i < map.length; i++) {
                    map[i] = (map[i] & 0xf0f0f0f0) | ((map[i] & 0xf0f0f0f0) >> 4);
                }
                icm = new IndexColorModel(icm.getPixelSize(), icm.getMapSize(), map, 0, icm.hasAlpha(), icm.getTransparentPixel(), icm.getTransferType());
            }
        } else {
            icm = null;
        }
        if (reservedColors != null) {
            int[] cmap = new int[Math.max(numberOfColors, reservedColors.getMapSize())];
            reservedColors.getRGBs(cmap);
            int[] cmap2 = new int[numberOfColors];
            if (icm != null) icm.getRGBs(cmap2);
            if (availableColors > 0) {
                System.arraycopy(cmap2, 0, cmap, reservedColors.getMapSize(), availableColors);
            }
            icm = new IndexColorModel(8, numberOfColors, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
        }
        if (icm == null) {
            icm = new IndexColorModel(8, numberOfColors, new int[numberOfColors], 0, false, -1, DataBuffer.TYPE_BYTE);
        }
        return icm;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;


        BufferedImage inputImage = (BufferedImage) in.data;
        IndexColorModel icm = createIndexColorModel(inputImage, outputFormat);
        BufferedImage outputImage = reuseOutputImage(inputImage, out.data instanceof BufferedImage b ? b : null, icm);
        if (dither0 == null) {
            renderOutputImageWithoutDithering(inputImage, outputImage, icm);
        } else {
            renderOutputImageWithDithering(inputImage, outputImage, icm, dither0, dither1, dither2);
        }
        out.data = outputImage;

        return CODEC_OK;
    }

    private void renderOutputImageWithDithering(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm, Dither dither0, Dither dither1, Dither dither2) {
        float[][] C = toOklabColors(icm);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        float[] rgbs = new float[3];
        float[] labf = new float[3];
        var in = inputImage.getRGB(0, 0, inputImage.getWidth(), inputImage.getHeight(), null, 0, inputImage.getWidth());
        var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
        int colorIndex = 0;
        int width = outputImage.getWidth();
        int x = 0, y = 0;

        for (int i = 0, n = Math.min(in.length, out.length); i < n; i++) {
            int pix = in[i];
            labf = oklab.from24BitRGB(pix, labf);
            labf[0] += dither0.get(x, y);
            labf[1] += dither1.get(x, y);
            labf[2] += dither2.get(x, y);
            colorIndex = M.findNearestCluster(labf, colorIndex);
            out[i] = (byte) colorIndex;
            x = x + 1;
            if (x > width) {
                x = 0;
                y++;
            }
        }
    }

    private void renderOutputImageWithoutDithering(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm) {
        float[][] C = toOklabColors(icm);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        float[] rgbs = new float[3];
        float[] labf = new float[3];
        var in = inputImage.getRGB(0, 0, inputImage.getWidth(), inputImage.getHeight(), null, 0, inputImage.getWidth());
        var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
        int colorIndex = 0;
        for (int i = 0, n = Math.min(in.length, out.length); i < n; i++) {
            var pix = in[i];
            labf = oklab.from24BitRGB(pix, labf);
            colorIndex = M.findNearestCluster(labf, colorIndex);
            out[i] = (byte) colorIndex;
        }
    }

    private BufferedImage reuseOutputImage(BufferedImage input, BufferedImage output, IndexColorModel icm) {
        if (output != null
                && output.getWidth() == input.getWidth()
                && output.getHeight() == input.getHeight()) {
            ColorModel outputColorModel = output.getColorModel();
            if (icm == outputColorModel) {
                return output;
            }
            if (output.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                return new BufferedImage(icm, output.getRaster(), false, null);
            }
        }
        return new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, icm);
    }

    @Override
    public Format setOutputFormat(Format f) {
        if (!f.containsKey(IndexColorModelKey)) {
            throw new IllegalArgumentException("Output format must specify index color model.");
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
                dither0 = new BlueNoiseDither128(0, spread);
                dither1 = new BlueNoiseDither128(1, spread);
                dither2 = new BlueNoiseDither128(2, spread);
            }
        }

        return super.setOutputFormat(f);
    }

    private float[][] toOklabColors(IndexColorModel icm) {
        int[] cmap = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        float[][] C = new float[cmap.length][3];
        for (int i = 0; i < cmap.length; i++) {
            C[i] = oklab.from24BitRGB(cmap[i], C[i]);
        }
        return C;
    }

}
