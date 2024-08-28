/*
 * @(#)PictureCodec.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.codec;

import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.jcodec.impl.AWTUtil;

import java.awt.image.BufferedImage;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

public class PictureCodec extends AbstractCodec {
    public static final String ENCODING_PICTURE = "picture";

    public PictureCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE,
                                DataClassKey, BufferedImage.class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_PICTURE,
                                DataClassKey, Picture.class), //
                }//
        );
        name = "Picture Codec";
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(BufferFlag.DISCARD)) {
            return CODEC_OK;
        }
        if (in.data instanceof BufferedImage img) {
            out.data = AWTUtil.fromBufferedImage(img, ColorSpace.YUV420J);
            return CODEC_OK;
        }
        return CODEC_FAILED;
    }
}