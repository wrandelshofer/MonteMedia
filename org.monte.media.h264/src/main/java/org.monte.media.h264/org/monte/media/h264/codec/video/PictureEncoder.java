/*
 * @(#)PictureEncoder.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.codec.video;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.h264.impl.jcodec.common.model.ColorSpace;
import org.monte.media.h264.impl.jcodec.common.model.Picture;
import org.monte.media.h264.impl.jcodec.impl.AWTUtil;

import java.awt.image.BufferedImage;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/// Codec for [BufferedImage] to/from [Picture].
public class PictureEncoder extends org.monte.media.av.AbstractCodec {
    public static final String ENCODING_PICTURE = "picture";

    public PictureEncoder() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_BUFFERED_IMAGE,
                                DataClassKey, BufferedImage.class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_PICTURE,
                                DataClassKey, Picture.class), //
                }//
        );
        name = "JCodec Picture Encoder";
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(BufferFlag.DISCARD)) {
            return CODEC_OK;
        }
        if (in.data instanceof BufferedImage) {
            BufferedImage img = (BufferedImage) in.data;
            out.data = AWTUtil.fromBufferedImage(img, ColorSpace.YUV420J);
            return CODEC_OK;
        }
        return CODEC_FAILED;
    }
}
