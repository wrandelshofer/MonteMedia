/*
 * @(#)CDXLImageDecoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.AmigaBitmapImageConverter;
import org.monte.media.amigabitmap.AmigaHAMColorModel;
import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.util.ByteArrays;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BITMAP_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;
import static org.monte.media.cdxl.CDXLMovieReader.BitsPerColor;
import static org.monte.media.cdxl.CDXLMovieReader.HamKey;
import static org.monte.media.cdxl.CDXLMovieReader.InterleavedKey;
import static org.monte.media.cdxl.CDXLMovieReader.MIME_CDXL;

public class CDXLImageDecoder extends AbstractCodec {
    private final AmigaBitmapImageConverter factory = AmigaBitmapImageConverter.newInstance();
    private AmigaBitmapImage bitmap;

    public CDXLImageDecoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_CDXL,
                                EncodingKey, ENCODING_BITMAP_IMAGE, DataClassKey, byte[].class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                });
    }

    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);

        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            //outputFormat = outputFormat.prepend(KeyFrameIntervalKey, max(1, outputFormat.get(FrameRateKey).intValue()));

            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey, DepthKey));
            }
        }
        return this.outputFormat;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(BufferFlag.DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;
        out.length = 1;
        out.offset = 1;

        int depth = inputFormat.get(DepthKey);

        bitmap = reuseBitmap(in, bitmap);

        decodeColorModel(in);
        if (in.data instanceof byte[] bytes) {
            System.arraycopy(bytes, 0, bitmap.getBitmap(), 0, bytes.length);
        }

        out.data = factory.toBufferedImage(bitmap, out.data instanceof BufferedImage b ? b : null);
        out.setFlagsTo(BufferFlag.KEYFRAME);

        return CODEC_OK;
    }

    private void decodeColorModel(Buffer in) {
        if (in.header instanceof byte[] bytes) {
            int[] colors;
            if (in.format.get(BitsPerColor, 12) == 24) {
                colors = decode24BitsPerColor(bytes);
            } else {
                colors = decode12BitsPerColor(bytes);

            }
            ColorModel colorModel;
            if (in.format.get(HamKey, Boolean.FALSE)) {
                colorModel = new AmigaHAMColorModel(in.format.get(DepthKey), colors.length, colors, false);
            } else {
                colorModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
            }
            bitmap.setColorModel(colorModel);
        }
    }

    private static int[] decode12BitsPerColor(byte[] bytes) {
        int[] colors = new int[bytes.length / 2];
        for (int index = 0; index < colors.length; index++) {
            short shortColor = ByteArrays.getShortBE(bytes, index << 1);
            //expand rgb to rrggbb
            int intColor =
                    ((shortColor & 0xf00) << 12) | ((shortColor & 0xf00) << 8)
                            | ((shortColor & 0x0f0) << 8) | ((shortColor & 0x0f0) << 4)
                            | ((shortColor & 0x00f) << 4) | shortColor & 0x00f;

            colors[index] = intColor;
        }
        return colors;
    }

    private static int[] decode24BitsPerColor(byte[] bytes) {
        int[] colors = new int[bytes.length / 3];
        int readIndex = 0;
        for (int index = 0; index < colors.length; index++) {
            int intColor = ((bytes[readIndex] & 0xff) << 16) | (ByteArrays.getShortBE(bytes, readIndex + 1) & 0xffff);
            colors[index] = intColor;
            readIndex += 3;
        }
        return colors;
    }

    private AmigaBitmapImage reuseBitmap(Buffer in, AmigaBitmapImage bitmap) {
        Format format = in.format;
        int depth = format.get(DepthKey);
        int width = format.get(WidthKey);
        int height = format.get(HeightKey);
        if (bitmap != null && bitmap.getWidth() == width
                && bitmap.getHeight() == height
                && bitmap.getDepth() == depth
                && bitmap.getScanlineStride() == bitmap.getBitplaneStride()) {
            return bitmap;
        }
        ColorModel colorModel;
        if (format.get(HamKey, Boolean.FALSE)) {
            colorModel = new AmigaHAMColorModel(depth, 1 << depth, new int[1 << depth], true);
        } else {
            colorModel = new IndexColorModel(8, 1 << depth, new int[1 << depth], 0, false, -1, DataBuffer.TYPE_BYTE);
        }
        return new AmigaBitmapImage(width, height, depth,
                colorModel, format.get(InterleavedKey, false));
    }
}
