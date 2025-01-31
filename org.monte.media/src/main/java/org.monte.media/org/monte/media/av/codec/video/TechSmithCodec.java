/*
 * @(#)TechSmithCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.util.ArrayUtil;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.BufferFlag.SAME_DATA;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code TechSmithCodec} (tscc) encodes a BufferedImage as a byte[] array.
 * <p>
 * The TechSmith codec works with AVI and QuickTime.
 * <p>
 * This codec supports encoding from a {@code BufferedImage} into the file
 * format, and decoding from the file format to a {@code BufferedImage}.
 * <p>
 * This codec does not encode the color palette of an image. This must be done
 * separately.
 * <p>
 * Supported input formats:
 * <ul><li> {@code Format} with
 * {@code BufferedImage.class}, any width, any height, depth=8,16 or 24.</li>
 * </ul>
 * Supported output formats:
 * <ul><li> {@code Format} with {@code byte[].class}, same
 * width and height as input format, depth=8,16 or 24. </li>
 * </ul>
 * The codec supports
 * lossless delta- and key-frame encoding of images with 8, 16 or 24 bits per
 * pixel.
 * <p>
 * Compression of a frame is performed in two steps: In the first, step a frame
 * is compressed line by line from bottom to top. In the second step the
 * resulting data is compressed again using zlib compression.
 * <p>
 * Apart from the second compression step and the support for 16- and 24-bit
 * data, this encoder is identical to the {@link org.monte.media.avi.codec.video.RunLengthCodec}.
 * <p>
 * Each line of a frame is compressed individually. A line consists of two-byte
 * op-codes optionally followed by data. The end of the line is marked with the
 * EOL op-code.
 * <p>
 * The following op-codes are supported: <ul> <li>{@code 0x00 0x00}
 * <br>Marks the end of a line.</li>
 *
 * <li>{@code  0x00 0x01} <br>Marks the end of the bitmap.</li>
 *
 * <li>{@code 0x00 0x02 x y} <br> Marks a delta (skip). {@code x} and {@code y}
 * indicate the horizontal and vertical offset from the current position.
 * {@code x} and {@code y} are unsigned 8-bit values.</li>
 *
 * <li>{@code 0x00 n pixel{n} 0x00?} <br> Marks a literal run. {@code n} gives
 * the number of 8-, 16- or 24-bit pixels that follow. {@code n} must be between
 * 3 and 255. If n is odd and 8-bit pixels are used, a pad byte with the value
 * 0x00 must be added. </li> <li>{@code n pixel} <br> Marks a repetition.
 * {@code n} gives the number of times the given pixel is repeated. {@code n}
 * must be between 1 and 255. </li> </ul> Example:
 * <pre>
 * Compressed data         Expanded data
 *
 * 03 04                   04 04 04
 * 05 06                   06 06 06 06 06
 * 00 03 45 56 67 00       45 56 67
 * 02 78                   78 78
 * 00 02 05 01             Move 5 right and 1 down
 * 02 78                   78 78
 * 00 00                   End of line
 * 09 1E                   1E 1E 1E 1E 1E 1E 1E 1E 1E
 * 00 01                   End of RLE bitmap
 * </pre>
 * <p>
 * References:<br> <a
 * href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
 * >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a><br>
 *
 * @author Werner Randelshofer
 */
public class TechSmithCodec extends AbstractVideoCodec {

    private TechSmithCodecCore state;
    private Object previousPixels;
    private int frameCounter;
    private ColorModel previousColorModel;
    private Object newPixels;

    public TechSmithCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                });
        name = "TechSmith Screen Capture";
    }

    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);
        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey));
            }
        }
        return this.outputFormat;
    }

    @Override
    public void reset() {
        state = null;
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (state == null) {
            state = new TechSmithCodecCore();
        }
        if (in.isFlag(BufferFlag.DISCARD)) {
            out.setMetaTo(in);
            return CODEC_OK;
        }

        if (outputFormat.get(EncodingKey).equals(ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE)) {
            return encode(in, out);
        } else {
            return decode(in, out);
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        out.length = 1;
        out.offset = 0;

        if (state == null) {
            state = new TechSmithCodecCore();
        }

        int width = outputFormat.get(WidthKey);
        int height = outputFormat.get(HeightKey);
        int inputDepth = inputFormat.get(DepthKey);
        int outputDepth = outputFormat.get(DepthKey, inputDepth);

        boolean isKeyFrame;
        try {
            if (outputDepth == 8) {
                newPixels = ArrayUtil.reuseByteArray(newPixels, width * height);
                isKeyFrame = state.decode8((byte[]) in.data, in.offset, in.length, (byte[]) newPixels, (byte[]) newPixels, width, height, false);
            } else {
                newPixels = ArrayUtil.reuseIntArray(newPixels, width * height);
                if (inputDepth == 8) {
                    isKeyFrame = state.decode8((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                } else if (inputDepth == 16) {
                    isKeyFrame = state.decode16((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                } else {
                    isKeyFrame = state.decode24((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                }
            }
        } catch (IOException e) {
            out.exception = e;
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        BufferedImage img = (out.data instanceof BufferedImage) ? (BufferedImage) out.data : null;

        switch (outputDepth) {
            case 8: {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    ColorModel cm = getColorModel(in);
                    if (cm == null) {
                        cm = new IndexColorModel(8, 256, new int[256], 0, false, -1, DataBuffer.TYPE_BYTE);
                    }
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((byte[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 15: {
                int imgType = BufferedImage.TYPE_USHORT_555_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(15, 0x1f << 10, 0x1f << 5, 0x1f);
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 16:
            case 24: {
                int imgType = BufferedImage.TYPE_INT_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(24, 0xff << 16, 0xff << 8, 0xff);
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((int[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            default:
                throw new UnsupportedOperationException("Unsupported depth:" + outputDepth);
        }

        out.setFlag(KEYFRAME, isKeyFrame);

        out.data = img;
        return CODEC_OK;
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        ByteArrayImageOutputStream tmp = new ByteArrayImageOutputStream(ArrayUtil.reuseByteArray(out.data, 32));
        tmp.clear();

        boolean isKeyframe = frameCounter++ % outputFormat.get(KeyFrameIntervalKey, 60) == 0;
        out.setFlag(KEYFRAME, isKeyframe);
        out.clearFlag(SAME_DATA);

        // Handle sub-image
        Rectangle r;
        int scanlineStride;
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, outputFormat.get(WidthKey), outputFormat.get(HeightKey));
            scanlineStride = outputFormat.get(WidthKey);
            out.header = null;
        }
        int offset = r.x + r.y * scanlineStride;

        try {
            switch (outputFormat.get(DepthKey)) {
                case 8: {
                    byte[] pixels = getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    ColorModel newColorModel = getColorModel(in);
                    if (previousColorModel == null || !previousColorModel.equals(newColorModel)) {
                        out.header = newColorModel;
                        previousColorModel = newColorModel;
                    }

                    if (isKeyframe) {
                        state.encodeKey8(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta8(tmp, pixels, (byte[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta8(tmp, pixels, (byte[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (byte[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 16: {
                    short[] pixels = getRGB15(in); // 16-bit TSCC is actually just 15-bit
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    if (isKeyframe) {
                        state.encodeKey16(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta16(tmp, pixels, (short[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta16(tmp, pixels, (short[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (short[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 24: {
                    int[] pixels = getRGB24(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    if (isKeyframe) {
                        state.encodeKey24(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        out.setFlag(KEYFRAME);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta24(tmp, pixels, (int[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta24(tmp, pixels, (int[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (int[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                default: {
                    out.setFlag(DISCARD);
                    return CODEC_FAILED;
                }
            }

            out.format = outputFormat;
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.sampleCount = 1;
            out.length = (int) tmp.length();
            return CODEC_OK;
        } catch (IOException ex) {
            out.exception = ex;
            out.setFlag(DISCARD);
            return CODEC_OK;
        }
    }

}
