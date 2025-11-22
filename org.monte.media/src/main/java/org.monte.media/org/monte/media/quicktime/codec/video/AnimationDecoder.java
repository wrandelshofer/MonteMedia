/*
 * @(#)AnimationDecoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.video.ArrayBufferToImage;
import org.monte.media.av.codec.video.VideoDecoderCore;
import org.monte.media.color.Colors;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.ByteArrays;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/// Implements the Apple Animation codec.
///
/// Supports lossless delta- and key-frame encoding of images onlyWith 8, 16 or
/// 24 bits per pixel.
///
/// The QuickTime player requires that a keyframe is written once per second.
/// This codec enforces this.
///
/// An encoded frame has the following format:
/// <pre>
/// Header:
/// uint32 chunkSize
///
/// uint16 header 0x0000 =&amp; decode entire image
///               0x0008 =&amp; starting line and number of lines follows
/// if header==0x0008 {
///   uint16 startingLine at which to begin updating frame
///   uint16 reserved 0x0000
///   uint16 numberOfLines to update
///   uint16 reserved 0x0000
/// }
/// n-bytes compressed lines
/// </pre>
///
/// The first 4 bytes defines the chunk length. This field also carries some
/// other unknown flags, since at least one of the high bits is sometimes
/// set.
///
/// If the overall length of the chunk is less than 8, treat the frame as a NOP,
/// which means that the frame is the same as the one before it.
///
/// Next, there is a header of either 0x0000 or 0x0008. A header value onlyWith
/// bit 3 set (header &amp; 0x0008) indicates that information follows revealing
/// at which line the decode process is to begin:
/// <pre>
/// 2 bytes    starting line at which to begin updating frame
/// 2 bytes    unknown
/// 2 bytes    the number of lines to update
/// 2 bytes    unknown
/// </pre>
///
/// If the header is 0x0000, then the decode begins from the first line and
/// continues through the entire height of the image.
///
/// After the header comes the individual RLE-compressed lines. An individual
/// compressed line is comprised of a skip code, followed by a series of RLE
/// codes and pixel data:
/// <pre>
///  1 byte     skip code
///  1 byte     RLE code
///  n bytes    pixel data
///  1 byte     RLE code
///  n bytes    pixel data
/// </pre> Each line begins onlyWith a byte that defines the number of pixels to
/// skip in a particular line in the output line before outputting new pixel
/// data. Actually, the skip count is set to one more than the number of pixels
/// to skip. For example, a skip byte of 15 means "skip 14 pixels", while a skip
/// byte of 1 means "don't skip any pixels". If the skip byte is 0, then the
/// frame decode is finished. Therefore, the maximum skip byte value of 255
/// allows for a maximum of 254 pixels to be skipped.
///
/// After the skip byte is the first RLE code, which is a single signed byte. The
/// RLE code can have the following meanings:
///    - equal to 0: There is
///     another single-byte skip code in the stream. Again, the actual number of
///     pixels to skip is 1 less than the skip code. Therefore, the maximum skip byte
///     value of 255 allows for a maximum of 254 pixels to be skipped.
///   - equal to -1: End of the RLE-compressed line
///   - greater than 0: Run of pixel data is copied directly from the encoded
///     stream to the output frame.
///   - less than -1: Repeat pixel data -(RLE code) times.
///
/// The pixel data has the following format:    - 8-bit data: Pixels are
///     handled in groups of four. Each pixel is a palette index (the palette is
///     determined by the Quicktime file transporting the data).
/// If (code &gt;
///     0), copy (4 * code) pixels from the encoded stream to the output.
/// If
///     (code &lt; -1), extract the next 4 pixels from the encoded stream and render
///     the entire group -(code) times to the output frame.
///   - 16-bit data: Each pixel is represented by a 16-bit RGB value onlyWith 5
///     bits used for each of the red, green, and blue color components and 1 unused
///     bit to round the value tmp to 16 bits: `xrrrrrgg gggbbbbb`. Pixel data
///     is rendered to the output frame one pixel at a time.
/// If (code &gt; 0),
///     copy the run of (code) pixels from the encoded stream to the output.
/// If
///     (code &lt; -1), unpack the next 16-bit RGB value from the encoded stream and
///     render it to the output frame -(code) times.
///   - 24-bit data: Each pixel is represented by a 24-bit RGB value onlyWith 8
///     bits (1 byte) used for each of the red, green, and blue color components:
///     `rrrrrrrr gggggggg bbbbbbbb`. Pixel data is rendered to the output
///     frame one pixel at a time.
/// If (code &gt; 0), copy the run of (code)
///     pixels from the encoded stream to the output.
/// If (code &lt; -1), unpack
///     the next 24-bit RGB value from the encoded stream and render it to the output
///     frame -(code) times.
///   - 32-bit data: Each pixel is represented by a 32-bit ARGB value onlyWith 8
///     bits (1 byte) used for each of the alpha, red, green, and blue color
///     components: `aaaaaaaa rrrrrrrr gggggggg bbbbbbbb`. Pixel data is
///     rendered to the output frame one pixel at a time.
/// If (code &gt; 0), copy
///     the run of (code) pixels from the encoded stream to the output.
/// If (code
///     &lt; -1), unpack the next 32-bit ARGB value from the encoded stream and
///     render it to the output frame -(code) times.
///
/// References:
/// <a
/// href="http://multimedia.cx/qtrle.txt">http://multimedia.cx/qtrle.txt</a>
///
/// @author Werner Randelshofer
public class AnimationDecoder extends org.monte.media.av.AbstractCodec {

    private Object previousPixels;
    private int frameCounter;
    private Object newPixels;
    protected byte[] byteBuf = new byte[4];

    private final static int SKIP_CODE = 0;
    private final static int EOL_CODE = -1;

    public AnimationDecoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 2), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 4), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 8), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 16), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 24), //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_ANIMATION, DataClassKey, byte[].class, DepthKey, 32), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
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
    public void reset() {
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat == null) return CODEC_FAILED;
        return decode(in, out);
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

        try {

            switch (inputDepth) {
                case 2, 4, 8 -> {
                    newPixels = ArrayUtil.reuseByteArray(newPixels, width * height);
                    decode8((byte[]) in.data, in.offset, in.length, (byte[]) newPixels, (byte[]) newPixels, width, height);
                }
                case 16 -> {
                    newPixels = ArrayUtil.reuseShortArray(newPixels, width * height);
                    decode16((byte[]) in.data, in.offset, in.length, (short[]) newPixels, (short[]) newPixels, width, height, false);
                }
                case 24 -> {
                    newPixels = ArrayUtil.reuseIntArray(newPixels, width * height);
                    decode24((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                }
                case 32 -> {
                    newPixels = ArrayUtil.reuseIntArray(newPixels, width * height);
                    decode32((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                }
                default -> {
                    out.setFlag(DISCARD);
                    return CODEC_FAILED;
                }
            }
        } catch (IOException e) {
            out.exception = e;
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        BufferedImage img = (out.data instanceof BufferedImage) ? (BufferedImage) out.data : null;
        ArrayBufferToImage abti = new ArrayBufferToImage();
        switch (outputDepth) {
            case 2 -> {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    ColorModel cm = abti.getColorModel(in);
                    if (cm == null) {
                        cm = Colors.createMacColors();
                    }
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                for (int y = 0; y < height; y++) {
                    System.arraycopy((byte[]) newPixels, y * width, pixels, y * width / 4, width / 4);
                }
            }
            case 4 -> {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    ColorModel cm = abti.getColorModel(in);
                    if (cm == null) {
                        cm = Colors.createMacColors();
                    }
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                for (int y = 0; y < height; y++) {
                    System.arraycopy((byte[]) newPixels, y * width, pixels, y * width / 2, width / 2);
                }
            }
            case 8 -> {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    ColorModel cm = abti.getColorModel(in);
                    if (cm == null) {
                        cm = Colors.createMacColors();
                    }
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((byte[]) newPixels, 0, pixels, 0, width * height);
            }
            case 16, 15 -> {
                int imgType = BufferedImage.TYPE_USHORT_555_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    ColorModel palette = outputFormat.get(PaletteKey);
                    DirectColorModel cm = palette instanceof DirectColorModel dp && (dp.getPixelSize() == 15 || dp.getPixelSize() == 16) ? dp : new DirectColorModel(15, 0x1f << 10, 0x1f << 5, 0x1f);
                    img = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
                } else {
                    BufferedImage oldImg = img;
                    img = new BufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            case 24 -> {
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
            default -> throw new UnsupportedOperationException("Unsupported depth:" + outputDepth);
        }


        out.data = img;
        return CODEC_OK;
    }


    public void decode8(byte[] inArray, int off, int length, byte[] out, byte[] prev, int width, int height) throws
            IOException {
        if (prev != out) {
            System.arraycopy(prev, 0, out, 0, width * height);
        }
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inArray, off, length);
        // Decode chunk size
        // -----------------
        long chunkSize = in.readUnsignedInt();
        if (chunkSize <= 8) {
            return;
        }

        // Decode header
        // -----------------
        int header = in.readUnsignedShort();
        int startingLine;
        int numberOfLines;
        if (header == 0) {
            // decode entire image
            startingLine = 0;
            numberOfLines = height;
        } else if (header == 8) {
            // starting line and number of lines follows
            startingLine = in.readUnsignedShort();
            int reserved1 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved1 != 0) {
                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));
            }
            numberOfLines = in.readUnsignedShort();
            int reserved2 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved2 != 0) {
                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));
            }
        } else {
            throw new IOException("Unknown header 0x" + Integer.toHexString(header));
        }
        if (startingLine > height || numberOfLines == 0) {
            return;
        }
        if (startingLine + numberOfLines - 1 > height) {
            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);
        }

        // Decode scanlines
        // -----------------
        int offset = 0;
        int scanlineStride = width;
        int multiplier = 4;
        for (int l = 0; l < numberOfLines; l++) {
            int i = offset + (startingLine + l) * scanlineStride;
            int skipCode = in.readUnsignedByte() - 1;
            if (skipCode == EOL_CODE) {
                break; // end of image code
            } else if (skipCode > 0) {
                i += skipCode * multiplier;
            }
            int x = 0;
            while (true) {
                int opCode = in.readByte();
                if (opCode == SKIP_CODE) {// skip op
                    skipCode = in.readUnsignedByte() - 1;
                    if (skipCode > 0) {
                        i += skipCode * multiplier;
                        x += skipCode * multiplier;
                    }
                } else if (opCode > SKIP_CODE) { // run of data op
                    in.readFully(out, i, opCode * multiplier);
                    i += opCode * multiplier;
                    x += opCode * multiplier;
                } else if (opCode == EOL_CODE) { // end of line op
                    break;
                } else { // repeat op
                    int d = in.readInt();
                    int end = i - opCode * multiplier;
                    while (i < end) {
                        ByteArrays.setIntBE(out, i, d);
                        i += multiplier;
                    }
                    x += opCode * multiplier;
                }
            }
            assert i <= offset + (startingLine + l + 1) * scanlineStride;
        }
        assert in.getStreamPosition() == in.length();
    }

    public void decode16(byte[] inArray, int off, int length, short[] out, short[] prev, int width, int height,
                         boolean onlyDecodeIfKeyframe) throws IOException {
        if (prev != out) {
            System.arraycopy(prev, 0, out, 0, width * height);
        }
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inArray, off, length);
        // Decode chunk size
        // -----------------
        long chunkSize = in.readUnsignedInt();
        if (chunkSize <= 8) {
            return;
        }
        if (in.length() != chunkSize) {
            // sometimes the chunk size is wrong, but we can still decode the input
            // throw new IOException("Illegal chunk size:" + chunkSize + " expected:" + in.length());
        }
        // Decode header
        // -----------------
        int header = in.readUnsignedShort();
        int startingLine;
        int numberOfLines;
        if (header == 0) {
            // decode entire image
            startingLine = 0;
            numberOfLines = height;
        } else if (header == 8) {
            // starting line and number of lines follows
            startingLine = in.readUnsignedShort();
            int reserved1 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved1 != 0) {
                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));
            }
            numberOfLines = in.readUnsignedShort();
            int reserved2 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved2 != 0) {
                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));
            }
        } else {
            throw new IOException("Unknown header 0x" + Integer.toHexString(header));
        }
        if (startingLine > height || numberOfLines == 0) {
            return;
        }
        if (startingLine + numberOfLines - 1 > height) {
            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);
        }

        // Decode scanlines
        // -----------------
        int offset = 0;
        int scanlineStride = width;
        for (int l = 0; l < numberOfLines; l++) {
            int i = offset + (startingLine + l) * scanlineStride;
            int skipCode = in.readUnsignedByte() - 1;
            if (skipCode == EOL_CODE) {
                break; // end of image code
            } else if (skipCode > 0) {
                i += skipCode;
            }

            while (true) {
                int opCode = in.readByte();
                if (opCode == SKIP_CODE) {// skip op
                    skipCode = in.readUnsignedByte() - 1;
                    if (skipCode > 0) {
                        i += skipCode;
                    }
                } else if (opCode > SKIP_CODE) { // run of data op
                    in.readFully(out, i, opCode);
                    i += opCode;
                } else if (opCode == EOL_CODE) { // end of line op
                    break;
                } else { // repeat op
                    short d = in.readShort();
                    int end = i - opCode;
                    Arrays.fill(out, i, end, d);
                    i = end;
                }
            }
            assert i <= offset + (startingLine + l + 1) * scanlineStride;
        }
        if (in.getStreamPosition() == in.length()) {
            throw new IOException("did not consume all bytes of stream. consumed=" + in.getStreamPosition() + " stream length: " + in.length());
        }
    }

    public void decode24(byte[] inArray, int off, int length, int[] out, int[] prev, int width, int height,
                         boolean onlyDecodeIfKeyframe) throws IOException {
        if (prev != out) {
            System.arraycopy(prev, 0, out, 0, width * height);
        }
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inArray, off, length);
        // Decode chunk size
        // -----------------
        long chunkSize = in.readUnsignedInt();
        if (chunkSize <= 8) {
            return;
        }
        if (in.length() != chunkSize) {
            throw new IOException("Illegal chunk size:" + chunkSize + " expected:" + in.length());
        }
        // Decode header
        // -----------------
        int header = in.readUnsignedShort();
        int startingLine;
        int numberOfLines;
        if (header == 0) {
            // decode entire image
            startingLine = 0;
            numberOfLines = height;
        } else if (header == 8) {
            // starting line and number of lines follows
            startingLine = in.readUnsignedShort();
            int reserved1 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved1 != 0) {
                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));
            }
            numberOfLines = in.readUnsignedShort();
            int reserved2 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved2 != 0) {
                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));
            }
        } else {
            throw new IOException("Unknown header 0x" + Integer.toHexString(header));
        }
        if (startingLine > height || numberOfLines == 0) {
            return;
        }
        if (startingLine + numberOfLines - 1 > height) {
            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);
        }

        // Decode scanlines
        // -----------------
        int offset = 0;
        int scanlineStride = width;
        for (int l = 0; l < numberOfLines; l++) {
            int i = offset + (startingLine + l) * scanlineStride;
            int skipCode = in.readUnsignedByte() - 1;
            if (skipCode == EOL_CODE) {
                break; // end of image code
            } else if (skipCode > 0) {
                i += skipCode;
            }
            while (true) {
                int opCode = in.readByte();
                if (opCode == SKIP_CODE) {// skip op
                    skipCode = in.readUnsignedByte() - 1;
                    if (skipCode > 0) {
                        i += skipCode;
                    }
                } else if (opCode > SKIP_CODE) { // run of data op
                    VideoDecoderCore.readInts24BE(in, out, i, opCode, byteBuf);
                    i += opCode;
                } else if (opCode == EOL_CODE) { // end of line op
                    break;
                } else { // repeat op
                    int d = VideoDecoderCore.readInt24BE(in, byteBuf);
                    int end = i - opCode;
                    Arrays.fill(out, i, end, d);
                    i = end;
                }
            }
            assert i <= offset + (startingLine + l + 1) * scanlineStride;
        }
        assert in.getStreamPosition() == in.length();
    }


    public void decode32(byte[] inArray, int off, int length, int[] out, int[] prev, int width, int height,
                         boolean onlyDecodeIfKeyframe) throws IOException {
        if (prev != out) {
            System.arraycopy(prev, 0, out, 0, width * height);
        }
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inArray, off, length);
        // Decode chunk size
        // -----------------
        long chunkSize = in.readUnsignedInt();
        if (chunkSize <= 8) {
            return;
        }
        if (in.length() != chunkSize) {
            throw new IOException("Illegal chunk size:" + chunkSize + " expected:" + in.length());
        }
        // Decode header
        // -----------------
        int header = in.readUnsignedShort();
        int startingLine;
        int numberOfLines;
        if (header == 0) {
            // decode entire image
            startingLine = 0;
            numberOfLines = height;
        } else if (header == 8) {
            // starting line and number of lines follows
            startingLine = in.readUnsignedShort();
            int reserved1 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved1 != 0) {
                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));
            }
            numberOfLines = in.readUnsignedShort();
            int reserved2 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved2 != 0) {
                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));
            }
        } else {
            throw new IOException("Unknown header 0x" + Integer.toHexString(header));
        }
        if (startingLine > height || numberOfLines == 0) {
            return;
        }
        if (startingLine + numberOfLines - 1 > height) {
            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);
        }

        // Decode scanlines
        // -----------------
        int offset = 0;
        int scanlineStride = width;
        for (int l = 0; l < numberOfLines; l++) {
            int i = offset + (startingLine + l) * scanlineStride;
            int skipCode = in.readUnsignedByte() - 1;
            if (skipCode == EOL_CODE) {
                break; // end of image code
            } else if (skipCode > 0) {
                i += skipCode;
            }
            while (true) {
                int opCode = in.readByte();
                if (opCode == SKIP_CODE) {// skip op
                    skipCode = in.readUnsignedByte() - 1;
                    if (skipCode > 0) {
                        i += skipCode;
                    }
                } else if (opCode > SKIP_CODE) { // run of data op
                    in.readFully(out, i, opCode);
                    i += opCode;
                } else if (opCode == EOL_CODE) { // end of line op
                    break;
                } else { // repeat op
                    int d = in.readInt();
                    int end = i - opCode;
                    Arrays.fill(out, i, end, d);
                    i = end;
                }
            }
            assert i <= offset + (startingLine + l + 1) * scanlineStride;
        }
        assert in.getStreamPosition() == in.length();
    }

    /// Decodes a 16-bit delta frame.
    ///
    /// @param in             The input stream.
    /// @param data           The image data.
    /// @param prev           The image data of the previous frame. This may be the same
    ///                                                                   object as data.
    /// @param width          The width of the image in data elements.
    /// @param height         The height of the image in data elements.
    /// @param offset         The offset to the first pixel in the data array.
    /// @param scanlineStride The number to append to offset to get to the next
    ///                                                                   scanline.
    public void decodeDelta16(ImageInputStream in, short[] data, short[] prev, int width, int height, int offset,
                              int scanlineStride)
            throws IOException {
        in.setByteOrder(ByteOrder.BIG_ENDIAN);

        // Decode chunk size
        // -----------------
        long chunkSize = in.readUnsignedInt();
        if (chunkSize <= 8) {
            return;
        }
        if (in.length() != chunkSize) {
            throw new IOException("Illegal chunk size:" + chunkSize + " expected:" + in.length());
        }
        //System.out.println("chunkSize:" + chunkSize);
        // Decode header
        // -----------------
        int header = in.readUnsignedShort();
        int startingLine;
        int numberOfLines;
        if (header == 0) {
            // decode entire image
            startingLine = 0;
            numberOfLines = height;
        } else if (header == 8) {
            // starting line and number of lines follows
            startingLine = in.readUnsignedShort();
            int reserved1 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved1 != 0) {
                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));
            }
            numberOfLines = in.readUnsignedShort();
            int reserved2 = in.readUnsignedShort(); // reserved, must be 0x0000
            if (reserved2 != 0) {
                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));
            }
        } else {
            throw new IOException("Unknown header 0x" + Integer.toHexString(header));
        }
        //System.out.println("startingLine " + startingLine + ", nbLines " + numberOfLines);

        if (startingLine > height || numberOfLines == 0) {
            return;
        }
        if (startingLine + numberOfLines - 1 > height) {
            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);
        }

        // Decode scanlines
        // -----------------
        for (int l = 0; l < numberOfLines; l++) {
            //System.out.println("  l:" + l);
            int i = offset + (startingLine + l) * scanlineStride;

            {
                int skipCode = in.readUnsignedByte() - 1;
                if (skipCode == EOL_CODE) {
                    //System.out.println("end of image");
                    break; // end of image code
                } else if (skipCode > SKIP_CODE) {
                    //System.out.println("skip " + skipCode);
                    if (data == prev) {
                        i += skipCode;
                    } else {
                        for (int j = 0; j < skipCode; j++) {
                            data[i] = prev[i];
                            i++;
                        }
                    }
                }
            }

            while (true) {
                int opCode = in.readByte();
                if (opCode == SKIP_CODE) {// skip op
                    int skipCode = in.readUnsignedByte() - 1;
                    if (skipCode > 0) {
                        //System.out.println("skip " + skipCode);
                        if (prev != data) {
                            System.arraycopy(prev, i, data, i, skipCode);
                        }
                        i += skipCode;
                    }
                } else if (opCode > SKIP_CODE) { // run of data op
                    //System.out.println("data " + opCode);
                    try {
                        in.readFully(data, i, opCode);
                    } catch (EOFException e) {
                        //System.out.println("EOFException");
                        //System.out.flush();
                        System.exit(5);
                        return;
                    }
                    i += opCode;
                } else if (opCode == EOL_CODE) { // end of line op
                    //System.out.println("EOL");
                    break;
                } else { // repeat op
                    //System.out.println("repeat "+opCode);
                    short d = in.readShort();
                    int end = i - opCode;
                    while (i < end) {
                        data[i++] = d;
                    }
                }
            }
            assert i <= offset + (startingLine + l + 1) * scanlineStride;
        }
        assert in.getStreamPosition() == in.length();
    }
}
