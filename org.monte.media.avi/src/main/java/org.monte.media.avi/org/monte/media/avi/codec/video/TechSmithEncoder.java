/*
 * @(#)TechSmithEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.ImageBufferToArray;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.util.ArrayUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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

/// `TechSmithCodec` (tscc) encodes a BufferedImage as a byte[] array.
///
/// The TechSmith codec works with AVI and QuickTime.
///
/// This codec supports encoding from a `BufferedImage` into the file
/// format, and decoding from the file format to a `BufferedImage`.
///
/// This codec does not encode the color palette of an image. This must be done
/// separately.
///
/// Supported input formats:
///   -  `Format` with
///     `BufferedImage.class`, any width, any height, depth=8,16 or 24.
///
/// Supported output formats:
///   -  `Format` with `byte[].class`, same
///     width and height as input format, depth=8,16 or 24.
///
/// The codec supports
/// lossless delta- and key-frame encoding of images with 8, 16 or 24 bits per
/// pixel.
///
/// Compression of a frame is performed in two steps: In the first, step a frame
/// is compressed line by line from bottom to top. In the second step the
/// resulting data is compressed again using zlib compression.
///
/// Apart from the second compression step and the support for 16- and 24-bit
/// data, this encoder is identical to the [RunLengthEncoder].
///
/// Each line of a frame is compressed individually. A line consists of two-byte
/// op-codes optionally followed by data. The end of the line is marked with the
/// EOL op-code.
///
/// The following op-codes are supported:    - `0x00 0x00`
///
/// Marks the end of a line.
///   - `0x00 0x01`
/// Marks the end of the bitmap.
///   - `0x00 0x02 x y`
/// Marks a delta (skip). `x` and `y`
///     indicate the horizontal and vertical offset from the current position.
///     `x` and `y` are unsigned 8-bit values.
///   - `0x00 n pixel{n}0x00?`
/// Marks a literal run. `n` gives
///     the number of 8-, 16- or 24-bit pixels that follow. `n` must be between
///     3 and 255. If n is odd and 8-bit pixels are used, a pad byte with the value
///     0x00 must be added.    - `n pixel`
/// Marks a repetition.
///     `n` gives the number of times the given pixel is repeated. `n`
///     must be between 1 and 255.   Example:
/// ```
/// Compressed data         Expanded data
///
/// 03 04                   04 04 04
/// 05 06                   06 06 06 06 06
/// 00 03 45 56 67 00       45 56 67
/// 02 78                   78 78
/// 00 02 05 01             Move 5 right and 1 down
/// 02 78                   78 78
/// 00 00                   End of line
/// 09 1E                   1E 1E 1E 1E 1E 1E 1E 1E 1E
/// 00 01                   End of RLE bitmap
/// ```
///
/// References:
/// <a
/// href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
/// >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a>
///
/// @author Werner Randelshofer
public class TechSmithEncoder extends org.monte.media.av.AbstractCodec {

    private TechSmithCodecCore state;
    private Object previousPixels;
    private int frameCounter;
    private ColorModel previousColorModel;
    private Object newPixels;

    public TechSmithEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                },
                new Format[]{
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

        return encode(in, out);
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
        Integer width = outputFormat.get(WidthKey);
        Integer height = outputFormat.get(HeightKey);
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, width, height);
            scanlineStride = width;
            out.header = null;
        }
        int offset = r.x + r.y * scanlineStride;

        try {
            switch (outputFormat.get(DepthKey)) {
                case 8: {
                    byte[] pixels = new ImageBufferToArray().getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    ColorModel newColorModel = new ImageBufferToArray().getColorModel(in);
                    if (previousColorModel == null || !previousColorModel.equals(newColorModel)) {
                        out.header = newColorModel;
                        previousColorModel = newColorModel;
                    }

                    if (isKeyframe) {
                        state.encodeKey8(tmp, pixels, width, height, offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta8(tmp, pixels, (byte[]) previousPixels, width, height, offset, scanlineStride);
                        } else {
                            state.encodeDelta8(tmp, pixels, (byte[]) previousPixels, width, height, offset, scanlineStride);
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
                    short[] pixels = new ImageBufferToArray().getRGB15(in, width, height); // 16-bit TSCC is actually just 15-bit
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    if (isKeyframe) {
                        state.encodeKey16(tmp, pixels, width, height, offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta16(tmp, pixels, (short[]) previousPixels, width, height, offset, scanlineStride);
                        } else {
                            state.encodeDelta16(tmp, pixels, (short[]) previousPixels, width, height, offset, scanlineStride);
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
                    int[] pixels = new ImageBufferToArray().getRGB24(in, width, height);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_FAILED;
                    }

                    if (isKeyframe) {
                        state.encodeKey24(tmp, pixels, width, height, offset, scanlineStride);
                        out.setFlag(KEYFRAME);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta24(tmp, pixels, (int[]) previousPixels, width, height, offset, scanlineStride);
                        } else {
                            state.encodeDelta24(tmp, pixels, (int[]) previousPixels, width, height, offset, scanlineStride);
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
