/*
 * @(#)RawDecoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.util.ByteArrays;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/// `RawCodec` encodes a BufferedImage as a byte[] array.
///
/// This codec does not encode the color palette of an image. This must be done
/// separately.
///
/// The pixels of a frame are written row by row from top to bottom and from
/// the left to the right.
///
/// Supported input formats:
///
///   - `VideoFormat` onlyWith `BufferedImage.class`, any width, any height,
///     depth=4.
///
/// Supported output formats:
///
///   - `VideoFormat` onlyWith `byte[].class`, same width and height as input
///     format, depth=4.
///
/// @author Werner Randelshofer
public class RawDecoder extends org.monte.media.av.AbstractCodec {

    public RawDecoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 32), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                });
    }


    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);

        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey, DepthKey));
            }
        }
        return this.outputFormat;
    }


    public void readKey8(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferByte buf = (DataBufferByte) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();

        int h = img.getHeight();
        int w = img.getWidth();
        int i = offset;
        int xy = 0;
        byte[] out = buf.getData();
        for (int y = 0; y < h; y++) {
            System.arraycopy(in, i, out, xy, w);
            i += w;
            xy += scanlineStride;
        }
    }

    public void readKey16(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferUShort buf = (DataBufferUShort) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();

        int h = img.getHeight();
        int w = img.getWidth();
        int i = offset;
        int xy = 0;
        short[] out = buf.getData();
        for (int y = 0; y < h; y++) {
            for (int k = 0, k2 = 0; k < w; k++, k2 += 2) {
                out[xy + k] = ByteArrays.getShortBE(in, i + k2);
            }
            i += w * 2;
            xy += scanlineStride;
        }
    }

    public void readKey24(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferInt buf = (DataBufferInt) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();

        int h = img.getHeight();
        int w = img.getWidth();
        int i = offset;
        int xy = 0;
        int[] out = buf.getData();
        for (int y = 0; y < h; y++) {
            for (int k = 0, k3 = 0; k < w; k++, k3 += 3) {
                out[xy + k] = 0xff000000//Alpha
                        | ((in[i + k3] & 0xff) << 16)//Red
                        | ((in[i + k3 + 1] & 0xff) << 8)//Green
                        | ((in[i + k3 + 2] & 0xff));//Blue
            }
            i += w * 3;
            xy += scanlineStride;
        }
    }

    public void readKey32(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferInt buf = (DataBufferInt) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();

        int h = img.getHeight();
        int w = img.getWidth();
        int i = offset;
        int xy = 0;
        int[] out = buf.getData();
        for (int y = 0; y < h; y++) {
            for (int k = 0, k2 = 0; k < w; k++, k2 += 4) {
                out[xy + k] = ByteArrays.getIntBE(in, i + k2);
            }
            i += w * 4;
            xy += scanlineStride;
        }
    }


    @Override
    public int process(Buffer in, Buffer out) {
        return decode(in, out);
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        out.sampleCount = 1;
        BufferedImage img = null;

        int imgType;
        ColorModel cm;
        switch (inputFormat.get(DepthKey)) {
            case 8 -> {
                cm = inputFormat.get(PaletteKey);
                imgType = BufferedImage.TYPE_BYTE_INDEXED;
            }
            case 16 -> {
                cm = inputFormat.get(PaletteKey);
                imgType = BufferedImage.TYPE_USHORT_555_RGB;
            }
            case 32 -> {
                cm = inputFormat.get(PaletteKey);
                imgType = BufferedImage.TYPE_INT_ARGB;
            }
            default -> {
                cm = inputFormat.get(PaletteKey);
                imgType = BufferedImage.TYPE_INT_RGB;
            }
        }
        ;

        int width = inputFormat.get(WidthKey);
        int height = inputFormat.get(HeightKey);
        if (out.data instanceof BufferedImage) {
            img = (BufferedImage) out.data;
            if (img != null && img.getWidth() != width
                    || img.getHeight() != height
                    || img.getType() != imgType) {
                img = null;
            }
        }
        if (img == null) {
            if (cm != null) {
                img = new BufferedImage(
                        cm,
                        cm.createCompatibleWritableRaster(width, height), false, null);
            } else {
                img = new BufferedImage(width, height, imgType);
            }
        }
        out.data = img;

        switch (inputFormat.get(DepthKey)) {
            case 8:
                readKey8((byte[]) in.data, in.offset, in.length, img);
                break;
            case 16:
                readKey16((byte[]) in.data, in.offset, in.length, img);
                break;
            case 24:
            default:
                readKey24((byte[]) in.data, in.offset, in.length, img);
                break;
            case 32:
                readKey32((byte[]) in.data, in.offset, in.length, img);
                break;
        }
        return CODEC_OK;
    }
}
