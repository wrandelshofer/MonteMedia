/* @(#)ScaleImageCodec.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.converter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import org.monte.media.player.AbstractVideoCodec;
import org.monte.media.player.Buffer;
import org.monte.media.player.Format;
import static org.monte.media.player.VideoFormatKeys.*;
import static org.monte.media.player.BufferFlag.*;

/**
 * Scales a buffered image.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-04-24 Created.
 */
public class ScaleImageCodec extends AbstractVideoCodec {

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
        Graphics2D g = imgOut.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(imgIn, 0, 0, imgOut.getWidth() - 1, imgOut.getHeight() - 1, 0, 0, imgIn.getWidth() - 1, imgIn.getHeight() - 1, null);
        g.dispose();

        out.data = imgOut;

        return CODEC_OK;
    }
}
