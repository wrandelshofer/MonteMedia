/*
 * @(#)RawEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.ImageBufferToArray;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.ByteArrays;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

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
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
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
public class RawEncoder extends org.monte.media.av.AbstractCodec {

    public RawEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
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
    private void writeKey8(ByteArrayImageOutputStream out, byte[] data, int width, int height, int offset,
                           int scanlineStride)
            throws IOException {

        // Write the samples
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            out.write(data, xy, width);
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
    private void writeKey16(ByteArrayImageOutputStream out, short[] data, int width, int height, int offset,
                            int scanlineStride)
            throws IOException {
        // Write the samples
        int i = (int) out.getStreamPosition();
        out.growSizeBy(height * width * 2);
        byte[] bytes = out.getBuffer();
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0; x < width; x++, i += 2) {
                short pixel = data[xy + x];
                ByteArrays.setShortBE(bytes, i, pixel);
            }
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
    private void writeKey24(ByteArrayImageOutputStream out, int[] data, int width, int height, int offset,
                            int scanlineStride)
            throws IOException {

        // Write the samples
        int i = (int) out.getStreamPosition();
        out.growSizeBy(height * width * 3);
        byte[] bytes = out.getBuffer();
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0; x < width; x++, i += 3) {
                int pixel = data[xy + x];
                bytes[i] = (byte) (pixel >> 16);
                bytes[i + 1] = (byte) (pixel >> 8);
                bytes[i + 2] = (byte) (pixel);
            }
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
    private void writeKey32(ByteArrayImageOutputStream out, int[] data, int width, int height, int offset,
                            int scanlineStride)
            throws IOException {

        // Write the samples
        for (int xy = offset, ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
            for (int x = 0, i = 0; x < width; x++, i += 4) {
                int pixel = data[xy + x];
                out.writeInt(pixel);
            }
        }
    }


    @Override
    public int process(Buffer in, Buffer out) {
        return encode(in, out);
    }


    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;
        ImageBufferToArray ibta = new ImageBufferToArray();

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
                    writeKey8(tmp, ibta.getIndexed8(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 16: {
                    writeKey16(tmp, ibta.getRGB15(in, r.width, r.height), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 24: {
                    writeKey24(tmp, ibta.getRGB24(in, r.width, r.height), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 32: {
                    writeKey32(tmp, ibta.getARGB32(in, r.width, r.height), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
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
