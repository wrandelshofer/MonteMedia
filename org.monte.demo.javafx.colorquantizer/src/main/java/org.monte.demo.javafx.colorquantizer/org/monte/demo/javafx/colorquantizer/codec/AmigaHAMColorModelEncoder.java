/*
 * @(#)AmigaHAMColorModelEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.codec;

import org.monte.demo.javafx.colorquantizer.model.DitheringMethod;
import org.monte.media.amigabitmap.AmigaHAMColorModel;
import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys;
import org.monte.media.color.OKLabColorSpace;
import org.monte.media.color.RgbBitConverters;
import org.monte.media.color.dither.BayerDither;
import org.monte.media.color.dither.BlueNoiseDither;
import org.monte.media.color.dither.Dither;
import org.monte.media.color.kmeans.Simple3DDistanceMatrix;
import org.monte.media.color.quant.KMeansColorQuantizer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Encodes a [BufferedImage] into a [BufferedImage] with a [AmigaHAMColorModel].
public class AmigaHAMColorModelEncoder extends AbstractCodec {
    /// The number of bits per color channel.
    ///
    /// The default value is 8.
    ///
    ///
    ///   - 4 will select HAM6
    ///   - 6 will select HAM8 but the low bits of every color will be set to 01.
    ///     This helps to prevent flickering in videos.
    ///
    ///   - 8 and every other value will select HAM8
    ///
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

    public AmigaHAMColorModelEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                }//
        );
        name = "Amiga HAM Codec";
    }

    private AmigaHAMColorModel createHamColorModel(BufferedImage data, Format outputFormat) {
        IndexColorModel indexColorModel = outputFormat.get(IndexColorModelKey);
        if (indexColorModel != null) {
            AmigaHAMColorModel.Type type = indexColorModel.getMapSize() <= 16 ? AmigaHAMColorModel.Type.HAM6 : AmigaHAMColorModel.Type.HAM8;
            return new AmigaHAMColorModel(type, indexColorModel);
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
        var e = new KMeansColorQuantizer(availableColors);
        e.addImage(data);
        var icm = e.computeColorPalette();
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
        var bitsPerColor = outputFormat.get(BitsPerColorKey, 8);
        if (bitsPerColor == 18) {
            int[] cmap = new int[64];
            icm.getRGBs(cmap);
            for (int i = 0; i < cmap.length; i++) {
                cmap[i] = (cmap[i] & 0xfcfcfc) | 0x010101;
            }
            icm = new IndexColorModel(8, numberOfColors, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
        }


        AmigaHAMColorModel acm;
        acm = new AmigaHAMColorModel((outputFormat.get(BitsPerColorKey, 8) == 4) ? AmigaHAMColorModel.Type.HAM6 : AmigaHAMColorModel.Type.HAM8, icm);
        return acm;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;


        BufferedImage inputImage = reuseInputImage((BufferedImage) in.data);
        AmigaHAMColorModel icm = createHamColorModel(inputImage, outputFormat);
        BufferedImage outputImage = reuseOutputImage(inputImage, out.data instanceof BufferedImage b ? b : null, icm);
        if (dither0 == null) {
            if (icm.getType() == AmigaHAMColorModel.Type.HAM6) {
                renderOutputImageWithoutDitheringHam6(inputImage, outputImage, icm);
            } else {
                renderOutputImageWithoutDitheringHam8(inputImage, outputImage, icm);
            }
        } else {

            if (icm.getType() == AmigaHAMColorModel.Type.HAM6) {
                renderOutputImageWithDitheringHam6(inputImage, outputImage, icm, dither0, dither1, dither2);
            } else {
                renderOutputImageWithDitheringHam8(inputImage, outputImage, icm, dither0, dither1, dither2);
            }
        }
        out.data = outputImage;

        return CODEC_OK;
    }

    private void renderOutputImageWithDitheringHam6(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm, Dither dither0, Dither dither1, Dither dither2) {
        float[][] C = toOklabColors(icm);
        int[] cmap = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        float[] lab = new float[3];
        float[] tmpLab = new float[3];
        float[] rgbs2 = new float[3];
        int height = inputImage.getHeight();
        int width = inputImage.getWidth();
        var in = inputImage.getRGB(0, 0, width, height, null, 0, inputImage.getWidth());
        var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
        int colorIndex = 0;
        int i = 0;
        for (int y = 0; y < height; y++) {
            int register = cmap[0];
            for (int x = 0; x < width; x++) {
                var pix = in[i];
                lab = oklab.from24BitRGB(pix, lab);
                lab[0] += dither0.get(x, y);
                lab[1] += dither1.get(x, y);
                lab[2] += dither2.get(x, y);
                colorIndex = M.findNearestCluster(lab, colorIndex);
                var newPix = cmap[colorIndex];
                oklab.toRGB(lab, rgbs2);
                int ditheredPix = RgbBitConverters.rgbFloatToRgb24(rgbs2);
                int redValue = (ditheredPix & 0xf0_00_00) >>> 20;
                int greenValue = (ditheredPix & 0xf0_00) >>> 12;
                int blueValue = (ditheredPix & 0xf0) >>> 4;
                int hamRed = (register & 0x00ffff) | (redValue << 20) | (redValue << 16);
                int hamGreen = (register & 0xff00ff) | (greenValue << 12) | (greenValue << 8);
                int hamBlue = (register & 0xffff00) | (blueValue << 4) | (blueValue);
                float distanceNewPix = squaredDistance(lab, newPix, tmpLab);
                float distanceRed = squaredDistance(lab, hamRed, tmpLab);
                float distanceGreen = squaredDistance(lab, hamGreen, tmpLab);
                float distanceBlue = squaredDistance(lab, hamBlue, tmpLab);
                float minDistance = Math.min(Math.min(Math.min(distanceNewPix, distanceRed), distanceGreen), distanceBlue);
                int command;
                if (distanceNewPix == minDistance) {
                    register = cmap[colorIndex];
                    command = colorIndex;
                } else if (distanceRed == minDistance) {
                    command = (2 << 4) | redValue;
                    register = hamRed;
                } else if (distanceGreen == minDistance) {
                    command = (3 << 4) | greenValue;
                    register = hamGreen;
                } else {
                    command = (1 << 4) | blueValue;
                    register = hamBlue;
                }

                out[i++] = (byte) command;
            }
        }
    }

    private void renderOutputImageWithDitheringHam8(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm, Dither dither0, Dither dither1, Dither dither2) {
        float[][] C = toOklabColors(icm);
        int[] cmap = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        int block = 64;
        int height = inputImage.getHeight();
        IntStream.range(0, height / block + 1).parallel().forEach(yy -> {

            float[] lab = new float[3];
            float[] rgbs2 = new float[3];
            float[] tmpLab = new float[3];
            int width = inputImage.getWidth();
            var in = inputImage.getRGB(0, 0, width, height, null, 0, inputImage.getWidth());
            var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
            int colorIndex = 0;

            for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
                int register = cmap[0];
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    var pix = in[i];
                    lab = oklab.from24BitRGB(pix, lab);
                    lab[0] += dither0.get(x, y);
                    lab[1] += dither1.get(x, y);
                    lab[2] += dither2.get(x, y);
                    colorIndex = M.findNearestCluster(lab, colorIndex);
                    var newPix = cmap[colorIndex];
                    oklab.toRGB(lab, rgbs2);
                    int ditheredPix = RgbBitConverters.rgbFloatToRgb24(rgbs2);
                    int redValue = (ditheredPix & 0xfc_00_00);
                    int greenValue = (ditheredPix & 0xfc_00);
                    int blueValue = (ditheredPix & 0xfc);
                    int hamRed = (register & 0x03ffff) | (redValue);
                    int hamGreen = (register & 0xff03ff) | (greenValue);
                    int hamBlue = (register & 0xffff03) | (blueValue);
                    float distanceNewPix = squaredDistance(lab, newPix, tmpLab);
                    float distanceRed = squaredDistance(lab, hamRed, tmpLab);
                    float distanceGreen = squaredDistance(lab, hamGreen, tmpLab);
                    float distanceBlue = squaredDistance(lab, hamBlue, tmpLab);
                    float minDistance = Math.min(Math.min(Math.min(distanceNewPix, distanceRed), distanceGreen), distanceBlue);
                    int command;
                    if (distanceNewPix == minDistance) {
                        command = colorIndex;
                        register = cmap[colorIndex];
                    } else if (distanceRed == minDistance) {
                        command = (2 << 6) | (redValue >>> 18);
                        register = hamRed;
                    } else if (distanceGreen == minDistance) {
                        command = (3 << 6) | (greenValue >>> 10);
                        register = hamGreen;
                    } else {
                        command = (1 << 6) | (blueValue >>> 2);
                        register = hamBlue;
                    }

                    out[i] = (byte) command;
                }
            }
        });
    }

    private float squaredDistance(float[] lab, int pix, float[] tmp) {
        tmp = oklab.from24BitRGB(pix, tmp);
        float dl = lab[0] - tmp[0];
        float da = lab[1] - tmp[1];
        float db = lab[2] - tmp[2];
        return dl * dl + da * da + db * db;
    }

    private void renderOutputImageWithoutDitheringHam6(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm) {
        float[][] C = toOklabColors(icm);
        int[] cmap = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        float[] pixLab = new float[3];
        float[] tmpLab = new float[3];
        int height = inputImage.getHeight();
        int width = inputImage.getWidth();
        var in = inputImage.getRGB(0, 0, width, height, null, 0, inputImage.getWidth());
        var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
        int colorIndex = 0;
        int i = 0;
        for (int y = 0; y < height; y++) {
            int register = cmap[0];
            for (int x = 0; x < width; x++) {
                var pix = in[i];
                pixLab = oklab.from24BitRGB(pix, pixLab);
                colorIndex = M.findNearestCluster(pixLab, colorIndex);
                var newPix = cmap[colorIndex];

                int redValue = (pix & 0xf0_00_00) >>> 20;
                int greenValue = (pix & 0xf0_00) >>> 12;
                int blueValue = (pix & 0xf0) >>> 4;
                int hamRed = (register & 0x00ffff) | (redValue << 20) | (redValue << 16);
                int hamGreen = (register & 0xff00ff) | (greenValue << 12) | (greenValue << 8);
                int hamBlue = (register & 0xffff00) | (blueValue << 4) | (blueValue);
                float distanceNewPix = squaredDistance(pixLab, newPix, tmpLab);
                float distanceRed = squaredDistance(pixLab, hamRed, tmpLab);
                float distanceGreen = squaredDistance(pixLab, hamGreen, tmpLab);
                float distanceBlue = squaredDistance(pixLab, hamBlue, tmpLab);
                float minDistance = Math.min(Math.min(Math.min(distanceNewPix, distanceRed), distanceGreen), distanceBlue);
                int code;
                if (distanceNewPix == minDistance) {
                    register = cmap[colorIndex];
                    code = colorIndex;
                } else if (distanceRed == minDistance) {
                    code = (2 << 4) | redValue;
                    register = hamRed;
                } else if (distanceGreen == minDistance) {
                    code = (3 << 4) | greenValue;
                    register = hamGreen;
                } else {
                    code = (1 << 4) | blueValue;
                    register = hamBlue;
                }

                out[i++] = (byte) code;
            }
        }
    }

    private void renderOutputImageWithoutDitheringHam8(BufferedImage inputImage, BufferedImage outputImage, IndexColorModel icm) {
        float[][] C = toOklabColors(icm);
        int[] cmap = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        var M = new Simple3DDistanceMatrix(C.length);
        M.updateMatrix(C);
        int block = 64;
        int height = inputImage.getHeight();
        IntStream.range(0, height / block + 1).parallel().forEach(yy -> {
            float[] pixLab = new float[3];
            float[] tmpLab = new float[3];
            int width = inputImage.getWidth();
            var in = inputImage.getRGB(0, 0, width, height, null, 0, inputImage.getWidth());
            var out = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
            int colorIndex = 0;

            for (int y = yy * block; y < Math.min(height, yy * block + block); y++) {
                int register = cmap[0];
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    var pix = in[i];
                    pixLab = oklab.from24BitRGB(pix, pixLab);
                    colorIndex = M.findNearestCluster(pixLab, colorIndex);
                    var newPix = cmap[colorIndex];

                    int redValue = (pix & 0xfc_00_00);
                    int greenValue = (pix & 0xfc_00);
                    int blueValue = (pix & 0xfc);
                    int hamRed = (register & 0x03ffff) | (redValue);
                    int hamGreen = (register & 0xff03ff) | (greenValue);
                    int hamBlue = (register & 0xffff03) | (blueValue);
                    float distanceNewPix = squaredDistance(pixLab, newPix, tmpLab);
                    float distanceRed = squaredDistance(pixLab, hamRed, tmpLab);
                    float distanceGreen = squaredDistance(pixLab, hamGreen, tmpLab);
                    float distanceBlue = squaredDistance(pixLab, hamBlue, tmpLab);
                    float minDistance = Math.min(Math.min(Math.min(distanceNewPix, distanceRed), distanceGreen), distanceBlue);
                    int code;
                    if (distanceNewPix == minDistance) {
                        code = colorIndex;
                        register = cmap[colorIndex];
                    } else if (distanceRed == minDistance) {
                        code = (2 << 6) | (redValue >>> 18);
                        register = hamRed;
                    } else if (distanceGreen == minDistance) {
                        code = (3 << 6) | (greenValue >>> 10);
                        register = hamGreen;
                    } else {
                        code = (1 << 6) | (blueValue >>> 2);
                        register = hamBlue;
                    }

                    out[i] = (byte) code;
                }
            }
        });
    }

    private BufferedImage reuseInputImage(BufferedImage input) {
        if (input.getColorModel().getColorSpace().isCS_sRGB()) {
            return input;
        }
        int width = input.getWidth();
        int height = input.getHeight();
        ColorModel cm = ColorModel.getRGBdefault();
        var r = cm.createCompatibleWritableRaster(width, height);
        int[] rgbs = ((DataBufferInt) r.getDataBuffer()).getData();
        input.getRGB(0, 0, width, height, rgbs, 0, width);
        return new BufferedImage(cm, r, false, null);
    }

    private BufferedImage reuseOutputImage(BufferedImage input, BufferedImage output, AmigaHAMColorModel icm) {
        int[] inRgbs = new int[icm.getMapSize()];
        if (output != null
                && output.getWidth() == input.getWidth()
                && output.getHeight() == input.getHeight()) {
            ColorModel outputColorModel = output.getColorModel();
            if (outputColorModel instanceof AmigaHAMColorModel acm) {
                int[] rgbs = new int[acm.getMapSize()];
                acm.getRGBs(rgbs);
                icm.getRGBs(inRgbs);
                if (Arrays.equals(rgbs, inRgbs) && icm.getType() == acm.getType()) {
                    return output;
                }
            }
            AmigaHAMColorModel acm = new AmigaHAMColorModel(inRgbs.length <= 16 ? AmigaHAMColorModel.Type.HAM6 : AmigaHAMColorModel.Type.HAM8, inRgbs);

            if (output.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                return new BufferedImage(acm, output.getRaster(), false, null);
            }
            return new BufferedImage(icm, output.getRaster(), false, null);
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
