/*
 * @(#)RunLengthCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.AbstractVideoCodec;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.util.ArrayUtil;

import javax.imageio.stream.ImageOutputStream;
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
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.lang.Math.min;
import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_RLE4;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_RLE8;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code RunLengthCodec} encodes a BufferedImage as a byte[] array.
 * <p>
 * This codec only works with the AVI file format. Other formats, such as
 * QuickTime, use a different encoding for run-length compressed video.
 * <p>
 * This codec currently only supports encoding from a {@code BufferedImage} into
 * the file format. Decoding support may be added in the future.
 * <p>
 * Supported input formats:
 * <ul>
 * <li>{@code Format} with {@code BufferedImage.class}, any width, any height,
 * depth=8.</li>
 * </ul>
 * Supported output formats:
 * <ul>
 * <li>{@code Format} with {@code byte[].class}, same width and height as input
 * format, depth=8.</li>
 * </ul>
 * The codec supports lossless delta- and key-frame encoding of images with 8
 * bits per pixel.
 * <p>
 * The codec does not encode the color palette of an image. This must be done
 * separately.
 * <p>
 * A frame is compressed line by line from bottom to top.
 * <p>
 * Each line of a frame is compressed individually. A line consists of two-byte
 * op-codes optionally followed by data. The end of the line is marked with
 * the EOL op-code.
 * <p>
 * The following op-codes are supported:
 * <ul>
 * <li>{@code 0x00 0x00}
 * <br>Marks the end of a line.</li>
 *
 * <li>{@code  0x00 0x01}
 * <br>Marks the end of the bitmap.</li>
 *
 * <li>{@code 0x00 0x02 x y}
 * <br> Marks a delta (skip). {@code x} and {@code y}
 * indicate the horizontal and vertical offset from the current position.
 * {@code x} and {@code y} are unsigned 8-bit values.</li>
 *
 * <li>{@code 0x00 n data{n} 0x00?}
 * <br> Marks a literal run. {@code n}
 * gives the number of data bytes that follow. {@code n} must be between 3 and
 * 255. If n is odd, a pad byte with the value 0x00 must be added.
 * </li>
 * <li>{@code n data}
 * <br> Marks a repetition. {@code n}
 * gives the number of times the data byte is repeated. {@code n} must be
 * between 1 and 255.
 * </li>
 * </ul>
 * Example:
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
 * References:<br>
 * <a href="http://wiki.multimedia.cx/index.php?title=Microsoft_RLE">http://wiki.multimedia.cx/index.php?title=Microsoft_RLE</a><br>
 *
 * @author Werner Randelshofer
 */
public class RunLengthCodec extends AbstractVideoCodec {

    private byte[] previousPixels;
    private int frameCounter;
    private Object newPixels;

    public RunLengthCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_RLE8, DataClassKey, byte[].class,
                                DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_RLE4, DataClassKey, byte[].class,
                                DepthKey, 4), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_RLE8, DataClassKey, byte[].class,
                                DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_RLE4, DataClassKey, byte[].class,
                                DepthKey, 4), //
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

    @Override
    public void reset() {
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat == null) return CODEC_FAILED;
        if (outputFormat.get(EncodingKey).equals(ENCODING_AVI_RLE8)
                || outputFormat.get(EncodingKey).equals(ENCODING_AVI_RLE4)) {
            return encode(in, out);
        } else {
            return decode(in, out);
        }
    }

    private int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        ByteArrayImageOutputStream tmp = new ByteArrayImageOutputStream(ArrayUtil.reuseByteArray(out.data, 32));
        tmp.clear();

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

        boolean isKeyframe = frameCounter++ % outputFormat.get(KeyFrameIntervalKey, 60) == 0;

        try {
            byte[] pixels = getIndexed8(in);
            if (pixels == null) {
                return CODEC_FAILED;
            }
            if (isKeyframe) {
                encodeKey8(tmp, pixels, r.width, r.height, offset, scanlineStride);
                out.setFlag(KEYFRAME);
            } else {
                encodeDelta8(tmp, pixels, previousPixels, r.width, r.height, offset, scanlineStride);
                out.clearFlag(KEYFRAME);
            }
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            //
            if (previousPixels == null) {
                previousPixels = pixels.clone();
            } else {
                System.arraycopy(pixels, 0, previousPixels, 0, pixels.length);
            }
            return CODEC_OK;
        } catch (IOException ex) {
            out.exception = ex;
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        out.length = 1;
        out.offset = 0;


        int width = outputFormat.get(WidthKey);
        int height = outputFormat.get(HeightKey);
        int inputDepth = inputFormat.get(DepthKey);
        int outputDepth = outputFormat.get(DepthKey, inputDepth);

        boolean isKeyFrame;
        try {
            newPixels = ArrayUtil.reuseByteArray(newPixels, width * height);
            isKeyFrame = decode8((byte[]) in.data, in.offset, in.length, (byte[]) newPixels, (byte[]) newPixels, width, height, false);
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


    /**
     * Encodes an 8-bit key frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param offset         The offset to the first pixel in the data array.
     * @param width          The width of the image in data elements.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void encodeKey8(ImageOutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        out.write(0);
                        out.write(literalCount); // Literal OP-code
                        out.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                out.write(1); // Repeat OP-code
                                out.write(data[xy - literalCount]);
                            }
                        } else {
                            out.write(0);
                            out.write(literalCount); // Literal OP-code
                            out.write(data, xy - literalCount, literalCount);
                            if (literalCount % 2 == 1) {
                                out.write(0); // pad byte
                            }
                            literalCount = 0;
                        }
                    }
                    out.write(repeatCount); // Repeat OP-code
                    out.write(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        out.write(1); // Repeat OP-code
                        out.write(data[xy - literalCount]);
                    }
                } else {
                    out.write(0);
                    out.write(literalCount);
                    out.write(data, xy - literalCount, literalCount);
                    if (literalCount % 2 == 1) {
                        out.write(0); // pad byte
                    }
                }
                literalCount = 0;
            }

            out.write(0);
            out.write(0x0000);// End of line
        }
        out.write(0);
        out.write(0x0001);// End of bitmap
    }


    /**
     * Encodes an 8-bit delta frame.
     *
     * @param out            The output stream.
     * @param data           The image data.
     * @param prev           The image data of the previous frame.
     * @param offset         The offset to the first pixel in the data array.
     * @param width          The width of the image in data elements.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void encodeDelta8(ImageOutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int mismatch = Arrays.mismatch(data, xy, xymax, prev, xy, xymax);
            int skipCount = mismatch < 0 ? xymax - xy : mismatch;
            xy += skipCount;

            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                if (verticalOffset == 1 && skipCount == 0) {
                    out.write(0x00);
                    out.write(0x00); // End of line OP-code
                    verticalOffset = 0;
                } else {
                    out.write(0x00);
                    out.write(0x02); // Skip OP-code
                    out.write(min(255, skipCount)); // horizontal offset
                    out.write(min(255, verticalOffset)); // vertical offset
                    skipCount -= min(255, skipCount);
                    verticalOffset -= min(255, verticalOffset);
                }
            }


            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            out.write(1); // Repeat OP-code
                            out.write(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            out.write(0);
                            out.write(literalRun); // Literal OP-code
                            out.write(data, xy - literalCount, literalRun);
                            if (literalRun % 2 == 1) {
                                out.write(0); // pad byte
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            out.write(0);
                            out.write(0x0002); // Skip OP-code
                            out.write(min(255, skipCount));
                            out.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        out.write(repeatCount); // Repeat OP-code
                        out.write(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    out.write(1); // Repeat OP-code
                    out.write(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    out.write(0);
                    out.write(literalRun); // Literal OP-code
                    out.write(data, xy - literalCount, literalRun);
                    if (literalRun % 2 == 1) {
                        out.write(0); // pad byte
                    }
                    literalCount -= literalRun;
                }
            }

            out.write(0);
            out.write(0x0000); // End of line OP-code
        }

        out.write(0);
        out.write(0x0001);// End of bitmap
    }

    public boolean decode8(byte[] in, int off, int length, byte[] out, byte[] prev, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {

        if (prev != out) {
            System.arraycopy(prev, 0, out, 0, width * height);
        }

        int limit = off + length;
        int offset = 0;
        int scanlineStride = width;
        boolean isKeyFrame = true;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline separately
        try {
            int inIndex = off;
            int x = 0;
            int xy = upsideDown;
            loop:
            while (inIndex < limit) {
                int opcode = in[inIndex++];
                switch (opcode) {
                    case 0:// escape
                        int opcode2 = in[inIndex++];
                        switch (opcode2) {
                            case 0: // end of line
                                isKeyFrame &= x == width;
                                xy -= scanlineStride;
                                x = 0;
                                break;
                            case 1: // end of bitmap
                                isKeyFrame &= x == 0 && xy == -width;
                                break loop;
                            case 2: // skip
                                int horizontalOffset = in[inIndex++] & 0xff;
                                int verticalOffset = in[inIndex++] & 0xff;
                                xy -= verticalOffset * scanlineStride;
                                x += horizontalOffset;
                                isKeyFrame = false;
                                break;
                            default:
                                int literalCount = opcode2 & 0xff;
                                System.arraycopy(in, inIndex, out, xy + x, literalCount);
                                x += literalCount;
                                inIndex += literalCount + (literalCount & 1);// skip pad byte
                                break;
                        }
                        break;
                    default://repeat
                        int repeatCount = opcode & 0xff;
                        byte value = in[inIndex++];
                        Arrays.fill(out, xy + x, xy + x + repeatCount, value);
                        x += repeatCount;
                        break;
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            throw new IOException(t);
        }
        return isKeyFrame;
    }


}
