/*
 * @(#)WritableImageCodec.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

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

    private BufferedImage reuseRgbImage;

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);

        out.format = this.outputFormat;

        if (!(in.data instanceof BufferedImage)) {
            out.setFlag(BufferFlag.DISCARD);
            return WritableImageCodec.CODEC_FAILED;
        }
        BufferedImage b = (BufferedImage) in.data;

        // SwingFXUtils is slow if the image has an indexed color model
        /*
        if (b.getColorModel() instanceof IndexColorModel) {
            BufferedImage rgbImage;
            int height = b.getHeight();
            int width = b.getWidth();
            if (reuseRgbImage != null && reuseRgbImage.getWidth() == width && reuseRgbImage.getHeight() == height) {
                rgbImage = reuseRgbImage;
            } else {
                rgbImage = reuseRgbImage=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(b, 0, 0, null);
            g.dispose();
            out.data = (out.data instanceof WritableImage w && w.getWidth() == width && w.getHeight() == height) ? w : new WritableImage(width, height);
            PixelWriter w = ((WritableImage) out.data).getPixelWriter();
            DataBufferInt dataBuffer = (DataBufferInt) rgbImage.getRaster().getDataBuffer();
            w.setPixels(0,0,width,height, PixelFormat.getIntArgbInstance(),
                    dataBuffer.getData(),0,width);
            return WritableImageCodec.CODEC_OK;
        }*/


        out.data = SwingFXUtils.toFXImage(b, (out.data instanceof WritableImage) ? (WritableImage) out.data : null);
        return WritableImageCodec.CODEC_OK;
    }
}
