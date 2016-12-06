/* @(#)PNGCodec.java
 * Copyright © 2011-2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.png;

import org.monte.media.Format;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;
import org.monte.media.io.ByteArrayImageInputStream;

/**
 * {@code PNGCodec} encodes a BufferedImage as a byte[] array..
 * 
 * Supported input/output formats:
 * 
 * {@code VideoFormat} with {@code BufferedImage.class}, any width, any height,
 * any depth.
 * {@code VideoFormat} with {@code byte[].class}, same width and height as input
 * format, depth=24.
 * 
 *
 * @author Werner Randelshofer
 * @version $Id: PNGCodec.java 364 2016-11-09 19:54:25Z werner $
 */
public class PNGCodec extends AbstractVideoCodec {

    public PNGCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    DepthKey, 24,
                    EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    DepthKey, 24,
                    EncodingKey, ENCODING_AVI_PNG, DataClassKey, byte[].class), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    DepthKey, 24,
                    EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    DepthKey, 24,
                    EncodingKey, ENCODING_AVI_PNG, DataClassKey, byte[].class), //
                });
         name = "PNG Codec";
    }

    @Override
    public Format setOutputFormat(Format f) {
        String mimeType = f.get(MimeTypeKey, MIME_QUICKTIME);
        if (mimeType != null && !mimeType.equals(MIME_AVI)) {
             super.setOutputFormat(
                    f.prepend(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey,
                    byte[].class, DepthKey, 24));
        } else {
             super.setOutputFormat(
                    f.prepend(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_PNG, DataClassKey,
                    byte[].class, DepthKey, 24));
        }

        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey,DepthKey));
            }
        }
        return this.outputFormat;
    }

    @Override
    public int process(Buffer in, Buffer out) {
          if (ENCODING_BUFFERED_IMAGE.equals(outputFormat.get(EncodingKey))) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        BufferedImage image = getBufferedImage(in);
        if (image == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        ByteArrayImageOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new ByteArrayImageOutputStream((byte[]) out.data);
        } else {
            tmp = new ByteArrayImageOutputStream();
        }

        try {
            ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/png").next();
            ImageWriteParam iwParam = iw.getDefaultWriteParam();
            iw.setOutput(tmp);
            IIOImage img = new IIOImage(image, null, null);
            iw.write(null, img, iwParam);
            iw.dispose();

            out.setFlag(KEYFRAME);
            out.header = null;
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        byte[] data = (byte[]) in.data;
        if (data == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        ByteArrayImageInputStream tmp = new ByteArrayImageInputStream(data);
        
        try {
            ImageReader ir = (ImageReader) ImageIO.getImageReadersByMIMEType("image/png").next();
            ir.setInput(tmp);
            out.data = ir.read(0);
            ir.dispose();
            
            out.sampleCount = 1;
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
}
