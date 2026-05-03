/*
 * @(#)ConvertColorSpaceCodec.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color.codec;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.color.op.ColorSpaceConvertOp;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Converts an image into another color space
///
/// This codec is useful if an image needs to be processed in another color space
///
/// @author Werner Randelshofer
public class ColorSpaceConvertCodec extends org.monte.media.av.AbstractCodec {

    /// The color space of the media.
    public final static FormatKey<ColorSpace> ConvertColorSpaceKey = new FormatKey<>("convertColorSpace", ColorSpace.class);

    public ColorSpaceConvertCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                }//
        );
        name = "Crop Image";
    }

    @Override
    public Format setOutputFormat(Format f) {
        if (!f.containsKey(ConvertColorSpaceKey)) {
            throw new IllegalArgumentException("Output format must specify ColorSpaceKey.");
        }
        return super.setOutputFormat(f);
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;

        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        BufferedImage imgIn = (BufferedImage) in.data;
        if (imgIn == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        ColorSpace cs = outputFormat.get(ConvertColorSpaceKey);
        var imgOut = new ColorSpaceConvertOp(cs).filter(imgIn, out.data instanceof BufferedImage img ? img : null);
        out.data = imgOut;
        return CODEC_OK;
    }


}
