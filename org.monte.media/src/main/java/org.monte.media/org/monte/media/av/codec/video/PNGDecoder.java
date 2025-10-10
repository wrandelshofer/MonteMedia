
/*
 * @(#)PNGCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.io.ByteArrayImageInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.IOException;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.MIME_ZIP;
import static org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.EncodingKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MIME_AVI;
import static org.monte.media.av.codec.video.VideoFormatKeys.MIME_JAVA;
import static org.monte.media.av.codec.video.VideoFormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.codec.video.VideoFormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code PNGCodec} encodes a BufferedImage as a byte[] array.
 * <p>
 * Supported input/output formats:
 * <ul>
 * <li>{@code VideoFormat} with {@code BufferedImage.class}, any width, any height,
 * any depth.</li>
 * <li>{@code VideoFormat} with {@code byte[].class}, same width and height as input
 * format, depth=24.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public class PNGDecoder extends org.monte.media.av.AbstractCodec {

    public PNGDecoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class), //

                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_ZIP,
                                EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class), //
                        //
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                                EncodingKey, ENCODING_AVI_PNG, DataClassKey, byte[].class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                });
        name = "PNG Codec";
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
    public int process(Buffer in, Buffer out) {
        return decode(in, out);
    }


    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        byte[] data = (byte[]) in.data;
        if (data == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        ByteArrayImageInputStream tmp = new ByteArrayImageInputStream(data);

        try {
            ImageReader ir = (ImageReader) ImageIO.getImageReadersByMIMEType("image/png").next();
            ir.setInput(tmp);
            out.data = ir.read(0);
            ir.dispose();

            out.sampleCount = 1;
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
