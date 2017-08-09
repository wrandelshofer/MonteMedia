/* @(#)ZMBVCodec.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.avi.codec.video;

import java.awt.image.DataBufferUShort;
import java.awt.Point;
import java.awt.image.DirectColorModel;
import org.monte.media.av.codec.video.AbstractVideoCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import static java.lang.Math.*;
import static org.monte.media.av.codec.video.VideoFormatKeys.*;
import static org.monte.media.av.BufferFlag.*;
import org.monte.media.image.BufferedImageWithColorModel;


/**
 * Implements the DosBox Capture Codec {@code "ZMBV"}.
 * <p>
 * This codec currently only supports decoding from the file format into
 * a {@code BufferedImage}. Encoding support may be added in the future.
 * <p>
 * For details seee {@link ZMBVCodecCore}.
 * </p>
 *
 * @author Werner Randelshofer
 * @version $Id: ZMBVCodec.java 364 2016-11-09 19:54:25Z werner $
 */
public class ZMBVCodec extends AbstractVideoCodec {

    private ZMBVCodecCore state;
    private Object oldPixels;
    private Object newPixels;

    public ZMBVCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, 
                    EncodingKey, ENCODING_AVI_DOSBOX_SCREEN_CAPTURE, DataClassKey, byte[].class, FixedFrameRateKey, true), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA, 
                            EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                });
        name = "ZMBV Codec";
    }
    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);

        // This codec can not scale an image nor change pixel depth.
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
        return decode(in,out);
    }
    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        out.format = outputFormat;
        out.length = 1;
        out.offset = 0;

        int width = outputFormat.get(WidthKey);
        int height = outputFormat.get(HeightKey);

        if (state == null) {
            state = new ZMBVCodecCore();
        }

        Object[] newPixelHolder = new Object[]{newPixels};
        Object[] oldPixelHolder = new Object[]{oldPixels};
        int result = state.decode((byte[]) in.data, in.offset, in.length, newPixelHolder, oldPixelHolder, width, height, false);
        boolean isKeyframe = result < 0;
        int depth = abs(result);
        newPixels = newPixelHolder[0];
        oldPixels = oldPixelHolder[0];

        BufferedImageWithColorModel img = null;
        if (out.data instanceof BufferedImageWithColorModel) {
            img = (BufferedImageWithColorModel) out.data;
        }
        switch (depth) {
            case 8: {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    int[] cmap = new int[256];
                    IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
                    img = new BufferedImageWithColorModel(width, height, imgType, icm);
                } else {
                    BufferedImageWithColorModel oldImg = img;
                    img = new BufferedImageWithColorModel(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                int[] cmap = state.getPalette();
                IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
                img.setColorModel(icm);
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((byte[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 15: {
                int imgType = BufferedImage.TYPE_USHORT_555_RGB; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(15, 0x1f << 10, 0x1f << 5, 0x1f << 0);
                    img = new BufferedImageWithColorModel(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false);
                } else {
                    BufferedImageWithColorModel oldImg = img;
                    img = new BufferedImageWithColorModel(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 16: {
                int imgType = BufferedImage.TYPE_USHORT_565_RGB; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(15, 0x1f << 11, 0x3f << 5, 0x1f << 0);
                    img = new BufferedImageWithColorModel(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false);
                } else {
                    BufferedImageWithColorModel oldImg = img;
                    img = new BufferedImageWithColorModel(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 32:
            default:
                throw new UnsupportedOperationException("Unsupported depth:" + depth);
        }

        Object swap=oldPixels;
        oldPixels=newPixels; newPixels=swap;
        out.setFlag(KEYFRAME, isKeyframe);

        out.data = img;
        return CODEC_OK;
    }

    }
