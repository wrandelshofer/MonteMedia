/*
 * @(#)CropImageCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys.MediaType;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Replaces the color space of an image without converting sample values.
///
/// This codec is useful if an image was loaded in the wrong color space.
///
/// @author Werner Randelshofer
public class ReplaceColorSpaceCodec extends org.monte.media.av.AbstractCodec {

    /// The color space of the media.
    public final static FormatKey<ColorSpace> ColorSpaceKey = new FormatKey<>("colorSpace", ColorSpace.class);

    public ReplaceColorSpaceCodec() {
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
        if (!f.containsKey(ColorSpaceKey)) {
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
        ColorSpace cs = outputFormat.get(ColorSpaceKey);
        if (cs == null) {
            out.exception = new IllegalArgumentException("the output format must contain a " + ColorSpaceKey);
            return CODEC_FAILED;
        }
        BufferedImage imgOut;
        if (imgIn.getColorModel() instanceof DirectColorModel cm) {
            var newCm = new DirectColorModel(cs, cm.getPixelSize(), cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask(), cm.getAlphaMask(), cm.isAlphaPremultiplied(), cm.getTransferType());
            imgOut = new BufferedImage(newCm, imgIn.getRaster(), imgIn.isAlphaPremultiplied(), null);
        } else if (imgIn.getColorModel() instanceof ComponentColorModel cm) {
            var newCm = new ComponentColorModel(cs, cm.hasAlpha(), cm.isAlphaPremultiplied(), cm.getTransparency(), cm.getTransferType());
            imgOut = new BufferedImage(newCm, imgIn.getRaster(), imgIn.isAlphaPremultiplied(), null);
        } else {
            out.exception = new RuntimeException("don't know hot to replace color model " + imgIn.getColorModel());
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        out.data = imgOut;

        return CODEC_OK;
    }
}
