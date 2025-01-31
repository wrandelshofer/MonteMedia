/*
 * @(#)RawCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.AbstractVideoCodec;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.ByteArrays;

import javax.imageio.stream.ImageOutputStream;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
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

/**
 * {@code RawCodec} encodes a BufferedImage as a byte[] array.
 * <p>
 * This codec does not encode the color palette of an image. This must be done
 * separately.
 * <p>
 * The pixels of a frame are written row by row from top to bottom and from
 * the left to the right.
 * <p>
 * Supported input formats:
 * <ul>
 * <li>{@code VideoFormat} onlyWith {@code BufferedImage.class}, any width, any height,
 * depth=4.</li>
 * </ul>
 * Supported output formats:
 * <ul>
 * <li>{@code VideoFormat} onlyWith {@code byte[].class}, same width and height as input
 * format, depth=4.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public class RawCodec extends AbstractVideoCodec {

    public RawCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class,
                                FixedFrameRateKey, true, DepthKey, 24), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class, DepthKey, 24), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_RAW, DataClassKey, byte[].class, DepthKey, 32), //
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


    /**
     * Encodes an 8-bit key frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param width          The width of the image in data elements.
     * @param height         The height of the image in data elements.
     * @param offset         The offset to the first pixel in the data array.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey8(ImageOutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {

        // Write the samples
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            out.write(data, xy, width);
        }
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

    /**
     * Encodes a 16-bit key frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param width          The width of the image in data elements.
     * @param height         The height of the image in data elements.
     * @param offset         The offset to the first pixel in the data array.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey16(ImageOutputStream out, short[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {

        // Write the samples
        byte[] bytes = new byte[width * 2]; // holds a scanline of raw image data onlyWith 3 channels of 16 bit data
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0, i = 0; x < width; x++, i += 2) {
                short pixel = data[xy + x];
                ByteArrays.setShortBE(bytes, i, pixel);
            }
            out.write(bytes, 0, bytes.length);
        }
    }

    /**
     * Encodes a 24-bit key frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param width          The width of the image in data elements.
     * @param height         The height of the image in data elements.
     * @param offset         The offset to the first pixel in the data array.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey24(ImageOutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {

        // Write the samples
        byte[] bytes = new byte[width * 3]; // holds a scanline of raw image data onlyWith 3 channels of 32 bit data
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0, i = 0; x < width; x++, i += 3) {
                int pixel = data[xy + x];
                bytes[i] = (byte) (pixel >> 16);
                bytes[i + 1] = (byte) (pixel >> 8);
                bytes[i + 2] = (byte) (pixel);
            }
            out.write(bytes, 0, bytes.length);
        }
    }

    /**
     * Encodes a 32-bit key frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param width          The width of the image in data elements.
     * @param height         The height of the image in data elements.
     * @param offset         The offset to the first pixel in the data array.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey32(OutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {

        // Write the samples
        byte[] bytes = new byte[width * 4]; // holds a scanline of raw image data onlyWith 3 channels of 32 bit data
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0, i = 0; x < width; x++, i += 4) {
                int pixel = data[xy + x];
                ByteArrays.setIntBE(bytes, i, pixel);
            }
            out.write(bytes, 0, bytes.length);
        }
    }

    /**
     * Encodes a 24-bit key frame.
     *
     * @param out   The output stream.
     * @param image The image.
     */
    public void writeKey24(OutputStream out, BufferedImage image)
            throws IOException {

        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = image.getRaster();
        int[] rgb = new int[width * 3]; // holds a scanline of raw image data onlyWith 3 channels of 32 bit data
        byte[] bytes = new byte[width * 3]; // holds a scanline of raw image data onlyWith 3 channels of 8 bit data
        for (int y = 0; y < height; y++) {
            // Note: Method getPixels is very slow as it does sample conversions for us
            rgb = raster.getPixels(0, y, width, 1, rgb);
            for (int k = 0, n = width * 3; k < n; k++) {
                bytes[k] = (byte) rgb[k];
            }
            out.write(bytes);
        }
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat.get(EncodingKey) == ENCODING_BUFFERED_IMAGE) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
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
            default -> {
                cm = null;
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
        }
        return CODEC_OK;
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;

        ByteArrayImageOutputStream tmp = new ByteArrayImageOutputStream(ArrayUtil.reuseByteArray(out.data, 32));
        tmp.clear();

        Format vf = outputFormat;

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
        } else {
            r = new Rectangle(0, 0, vf.get(WidthKey), vf.get(HeightKey));
            scanlineStride = vf.get(WidthKey);
        }

        try {
            switch (vf.get(DepthKey)) {
                case 8: {
                    writeKey8(tmp, getIndexed8(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 16: {
                    writeKey16(tmp, getRGB15(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 24: {
                    writeKey24(tmp, getRGB24(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 32: {
                    writeKey24(tmp, getARGB32(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                default: {
                    out.setFlag(DISCARD);
                    return CODEC_FAILED;
                }
            }

            out.format = outputFormat;
            out.sampleCount = 1;
            out.setFlag(KEYFRAME);
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            out.exception = ex;
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
}
