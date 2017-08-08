/* @(#)BitmapCodecOLD.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.bitmap.codec.video;

import org.monte.media.av.codec.video.AbstractVideoCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.bitmap.BitmapImage;
import java.awt.image.BufferedImage;
import static org.monte.media.anim.AmigaVideoFormatKeys.*;
import static org.monte.media.av.BufferFlag.*;

/**
 * Converts BufferedImage to BitmapImage. 
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-09-04 Created.
 */
public class BitmapCodecOLD extends AbstractVideoCodec {

    public BitmapCodecOLD() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_ANIM,
                    EncodingKey, ENCODING_ANIM_OP5, DataClassKey, byte[].class, FixedFrameRateKey, false), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA, 
                            EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, false), //
                });
        name="ILBM Codec";
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
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format=outputFormat;

        BufferedImage pixmap = (BufferedImage) in.data;
        Format vf = outputFormat;
        BitmapImage bitmap = out.data instanceof BitmapImage ? (BitmapImage) out.data : null;
        if (bitmap == null || bitmap.getWidth() != vf.get(WidthKey)
                || bitmap.getHeight() != vf.get(HeightKey) || bitmap.getDepth() != vf.get(DepthKey)) {
            bitmap = new BitmapImage(vf.get(WidthKey), vf.get(HeightKey), vf.get(DepthKey), pixmap.getColorModel());
            out.data = bitmap;
        }
        bitmap.setPlanarColorModel(pixmap.getColorModel());
        bitmap.convertFromChunky(pixmap);


        return CODEC_OK;
    }
}
