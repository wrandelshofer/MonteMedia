/*
 * @(#)ScaleImageCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.image.algo.NearestNeighbourResampleAlgoFloat;
import org.monte.media.image.op.GaussianKernelFactory;
import org.monte.media.image.op.ScaleOp;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Scales a buffered image.
 * <p>
 * Usage:
 * <pre>
 *     var codec=new ScaleImageCodec();
 *     codec.setOutputFormat(new Format(
 *          VideoFormatKeys.WidthKey, 320,
 *          VideoFormatKeys.HeightKey, 240
 *     ));
 *     var in=new Buffer();
 *     var out=new Buffer();
 *     in.data=ew BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
 *     var result=codec.process(in,out);
 *     if (result != Codec.CODEC_OK) throw new RuntimeException("cropping failed",out.exception);
 *     return (BufferedImage) out.data;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class ScaleImageCodec extends org.monte.media.av.AbstractCodec {

    private Object interpolationRenderingHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

    public ScaleImageCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                        new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE), //
                }//
        );
        name = "Scale Image";
    }

    @Override
    public Format setOutputFormat(Format f) {
        if (!f.containsKey(WidthKey) || !f.containsKey(HeightKey)) {
            throw new IllegalArgumentException("Output format must specify width and height.");
        }
        Format fNew = super.setOutputFormat(f.prepend(DepthKey, 24));
        return fNew;
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

        BufferedImage imgOut = null;
        if (out.data instanceof BufferedImage) {
            imgOut = (BufferedImage) out.data;
            if (imgOut.getWidth() != outputFormat.get(WidthKey)
                    || imgOut.getHeight() != outputFormat.get(HeightKey)//
                    || imgOut.getType() != imgIn.getType()) {
                imgOut = null;
            }
        }
        if (imgOut == null) {
            if (imgIn.getColorModel() instanceof IndexColorModel) {
                imgOut = new BufferedImage(outputFormat.get(WidthKey), outputFormat.get(HeightKey), imgIn.getType(), (IndexColorModel) imgIn.getColorModel());
            } else {
                imgOut = new BufferedImage(outputFormat.get(WidthKey), outputFormat.get(HeightKey), imgIn.getType());
            }

        }
        if (imgOut.getWidth() < imgIn.getWidth() && imgOut.getHeight() < imgIn.getHeight()) {
            downscaleImage(imgIn, imgOut);
        } else {
            upscaleImage(imgIn, imgOut);
        }
        out.data = imgOut;

        return CODEC_OK;
    }

    private void upscaleImage(BufferedImage src, BufferedImage dst) {
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationRenderingHint);
        g.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), 0, 0, src.getWidth(), src.getHeight(), null);
        g.dispose();
    }

    private void downscaleImage(BufferedImage src, BufferedImage dst) {
        var scaleOp = new ScaleOp(src.getWidth(), src.getHeight(), dst.getWidth(), dst.getHeight(),
                0.5f, new GaussianKernelFactory(), new NearestNeighbourResampleAlgoFloat());
        BufferedImage tmp = scaleOp.filter(src, null);

        var g = dst.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
    }
}
