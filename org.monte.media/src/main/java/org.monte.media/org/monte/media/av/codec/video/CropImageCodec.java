/*
 * @(#)CropImageCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys.MediaType;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Crops a buffered image.
///
/// Usage:
/// <pre>
///     var codec=new CropImageCodec();
///     codec.setOutputFormat(new Format(CropImageCodec.CropImageKey, new Rectangle(10,10,620,460)));
///     var in=new Buffer();
///     var out=new Buffer();
///     in.data=ew BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
///     var result=codec.process(in,out);
///     if (result != Codec.CODEC_OK) throw new RuntimeException("cropping failed",out.exception);
///     return (BufferedImage) out.data;
/// </pre>
///
/// @author Werner Randelshofer
public class CropImageCodec extends org.monte.media.av.AbstractCodec {

    /// The cropping of the media.
    public final static FormatKey<Rectangle> CropImageKey = new FormatKey<>("cropImage", Rectangle.class);

    public CropImageCodec() {
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
        if (!f.containsKey(CropImageKey)) {
            throw new IllegalArgumentException("Output format must specify CropImageKey.");
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
        Rectangle cropRect = outputFormat.get(CropImageKey);
        if (cropRect == null) {
            out.exception = new IllegalArgumentException("the output format must contain a " + CropImageKey);
            return CODEC_FAILED;
        }

        BufferedImage imgOut = reuseOutputImage(imgIn, out.data instanceof BufferedImage b ? b : null, cropRect);
        out.data = imgOut;
        var subImage = imgIn.getSubimage(cropRect.x, cropRect.y, cropRect.width, cropRect.height);
        imgOut.setData(subImage.copyData(imgOut.getRaster()));

        return CODEC_OK;
    }

    private BufferedImage reuseOutputImage(BufferedImage input, BufferedImage output, Rectangle cropRect) {
        ColorModel inputColorModel = input.getColorModel();
        int cropRectWidth = (int) cropRect.getWidth();
        int cropRectHeight = (int) cropRect.getHeight();
        if (output != null
                && output.getWidth() == cropRectWidth
                && output.getHeight() == cropRectHeight
                && output.getType() == input.getType()) {
            ColorModel outputColorModel = output.getColorModel();
            if (inputColorModel == outputColorModel) {
                return output;
            }
            if (inputColorModel instanceof IndexColorModel && output.getType() == input.getType()) {
                return new BufferedImage(inputColorModel, output.getRaster(), false, null);
            }
            return new BufferedImage(inputColorModel, output.getRaster(), input.isAlphaPremultiplied(), null);
        }
        if (inputColorModel instanceof IndexColorModel icm) {
            return new BufferedImage(cropRectWidth, cropRectHeight, input.getType(), icm);
        }
        return new BufferedImage(cropRectWidth, cropRectHeight, input.getType());
    }
}
