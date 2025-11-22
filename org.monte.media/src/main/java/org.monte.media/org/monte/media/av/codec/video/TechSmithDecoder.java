/*
 * @(#)TechSmithDecoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.avi.codec.video.RunLengthEncoder;
import org.monte.media.util.ArrayUtil;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
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
/// <pre>
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
/// </pre>
///
/// References:
/// <a
/// href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
/// >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a>
///
/// @author Werner Randelshofer
public class TechSmithDecoder extends org.monte.media.av.AbstractCodec {

    private TechSmithCodecCore state;
    private Object previousPixels;
    private int frameCounter;
    private ColorModel previousColorModel;
    private Object newPixels;

    public TechSmithDecoder() {
        super(new Format[]{
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

        return decode(in, out);
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
                    ColorModel cm = new ArrayBufferToImage().getColorModel(in);
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

}
