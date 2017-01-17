/* @(#)ZMBVCodec.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.avi;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import static java.lang.Math.*;
import java.util.Hashtable;
import ru.sbtqa.monte.media.AbstractVideoCodec;
import ru.sbtqa.monte.media.Buffer;
import static ru.sbtqa.monte.media.BufferFlag.*;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.FormatKeys.MediaType;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;

/**
 * Implements the DosBox Capture Codec {@code "ZMBV"}.
 * 
 * This codec currently only supports decoding from the file format into a
 * {@code BufferedImage}. Encoding support may be added in the future.
 * 
 * For details seee {@link ZMBVCodecCore}.
 * 
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
        return decode(in, out);
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

        MyBufferedImage img = null;
        if (out.data instanceof MyBufferedImage) {
            img = (MyBufferedImage) out.data;
        }
        switch (depth) {
            case 8: {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    int[] cmap = new int[256];
                    IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
                    img = new MyBufferedImage(width, height, imgType, icm);
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
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
                    img = new MyBufferedImage(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false);
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 16: {
                int imgType = BufferedImage.TYPE_USHORT_565_RGB; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(15, 0x1f << 11, 0x3f << 5, 0x1f << 0);
                    img = new MyBufferedImage(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false);
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 32:
            default:
                throw new UnsupportedOperationException("Unsupported depth:" + depth);
        }

        Object swap = oldPixels;
        oldPixels = newPixels;
        newPixels = swap;
        out.setFlag(KEYFRAME, isKeyframe);

        out.data = img;
        return CODEC_OK;
    }

    private static class MyBufferedImage extends BufferedImage {

        private ColorModel colorModel;

        public MyBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
            super(cm, raster, isRasterPremultiplied, new Hashtable<Object, Object>());
            colorModel = cm;
        }

        public MyBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
            super(cm, raster, isRasterPremultiplied, properties);
            colorModel = cm;
        }

        public MyBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
            super(width, height, imageType, cm);
            colorModel = cm;
        }

        public MyBufferedImage(int width, int height, int imageType) {
            super(width, height, imageType);
        }

        @Override
        public ColorModel getColorModel() {
            return colorModel;
        }

        public void setColorModel(ColorModel newValue) {
            this.colorModel = newValue;
        }
    }
}
