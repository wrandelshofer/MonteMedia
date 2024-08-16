/*
 * @(#)WritableImageCodec.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;

import java.awt.image.BufferedImage;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_WRITABLE_IMAGE;

public class WritableImageCodec extends AbstractCodec {
    public WritableImageCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class), //
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_WRITABLE_IMAGE, DataClassKey, WritableImage.class), //
                }
        );
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);

        out.format = this.outputFormat;

        if (!(in.data instanceof BufferedImage b)) {
            out.setFlag(BufferFlag.DISCARD);
            return WritableImageCodec.CODEC_FAILED;
        }

        out.data = SwingFXUtils.toFXImage(b, (out.data instanceof WritableImage w) ? w : null);
        return WritableImageCodec.CODEC_OK;
    }
}
