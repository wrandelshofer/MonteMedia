/* @(#)JPGCodec.java
 * Copyright © 2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.jpeg;

import org.monte.media.io.ByteArrayImageInputStream;
import javax.imageio.ImageReader;
import org.monte.media.Format;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

/**
 * {@code JPEGCodec} encodes a BufferedImage as a byte[] array. 
 * <p>
 * Supported input/output formats: 
 * <ul> 
 * <li>@code VideoFormat} with {@code BufferedImage.class}, any
 * width, any height, any depth.</li>
 * <li>{@code VideoFormat} with {@code byte[].class}, same width and height as input
 * format, depth=24.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id: JPEGCodec.java 364 2016-11-09 19:54:25Z werner $
 */
public class JPEGCodec extends AbstractVideoCodec {
    
    public JPEGCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_JPEG,//
                    CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG, //
                    DataClassKey, byte[].class, DepthKey, 24), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_MJPG, DataClassKey, byte[].class, DepthKey, 24), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_JPEG, DataClassKey, byte[].class, DepthKey, 24), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,//
                    EncodingKey, ENCODING_QUICKTIME_JPEG,//
                    CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG, //
                    DataClassKey, byte[].class, DepthKey, 24), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_MJPG, DataClassKey, byte[].class, DepthKey, 24), //
                }//
                );
        name = "JPEG Codec";
    }
    
    @Override
    public int process(Buffer in, Buffer out) {
        if (ENCODING_BUFFERED_IMAGE.equals(outputFormat.get(EncodingKey))) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);

        // This codec can not scale an image nor producing anything else
        // than 24-bit images. 
        // Enforce these properties
        if (outputFormat != null) {
            outputFormat = outputFormat.prepend(DepthKey, 24);
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey));
            }
        }
        return this.outputFormat;
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
            ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
            ImageWriteParam iwParam = iw.getDefaultWriteParam();
            iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = outputFormat.get(QualityKey, 1f);
            iwParam.setCompressionQuality(quality);
            iw.setOutput(tmp);
            IIOImage img = new IIOImage(image, null, null);
            iw.write(null, img, iwParam);
            iw.dispose();
            
            out.sampleCount = 1;
            out.setFlag(KEYFRAME);
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
            // ImageReader ir = (ImageReader) ImageIO.getImageReadersByMIMEType("image/jpeg").next();
            ImageReader ir = new MJPGImageReader(new MJPGImageReaderSpi());
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
