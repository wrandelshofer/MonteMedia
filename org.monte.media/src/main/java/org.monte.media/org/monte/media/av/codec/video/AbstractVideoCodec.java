/*
 * @(#)AbstractVideoCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;

import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code AbstractVideoCodec}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractVideoCodec extends AbstractCodec {

    private BufferedImage imgConverter;

    public AbstractVideoCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        super(supportedInputFormats, supportedOutputFormats);
    }

    /**
     * Gets 8-bit indexed pixels from a buffer. Returns null if conversion failed.
     */
    protected byte[] getIndexed8(Buffer buf) {
        if (buf.data instanceof byte[]) {
            return (byte[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getRaster().getDataBuffer() instanceof DataBufferByte) {
                return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            }
        }
        return null;
    }

    protected ColorModel getColorModel(Buffer buf) {
        if (buf.header instanceof ColorModel) {
            return (ColorModel) buf.header;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            return image.getColorModel();
        }
        return buf.format.get(PaletteKey);
    }

    /**
     * Gets 15-bit RGB pixels from a buffer. Returns null if conversion failed.
     */
    protected short[] getRGB15(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (short[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                if (image.getRaster().getDataBuffer() instanceof DataBufferShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferShort) image.getRaster().getDataBuffer()).getData();
                } else if (image.getRaster().getDataBuffer() instanceof DataBufferUShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
                }
            }
            if (imgConverter == null) {
                int width = outputFormat.get(WidthKey);
                int height = outputFormat.get(HeightKey);
                imgConverter = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
            }
            Graphics2D g = imgConverter.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return ((DataBufferUShort) imgConverter.getRaster().getDataBuffer()).getData();
        }
        return null;
    }

    /**
     * Gets 16-bit RGB-5-6-5 pixels from a buffer. Returns null if conversion failed.
     */
    protected short[] getRGB16(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (short[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (image.getRaster().getDataBuffer() instanceof DataBufferShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferShort) image.getRaster().getDataBuffer()).getData();
                } else if (image.getRaster().getDataBuffer() instanceof DataBufferUShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
                }
            }
            if (imgConverter == null) {
                int width = outputFormat.get(WidthKey);
                int height = outputFormat.get(HeightKey);
                imgConverter = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            }
            Graphics2D g = imgConverter.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return ((DataBufferUShort) imgConverter.getRaster().getDataBuffer()).getData();
        }
        return null;
    }


    /**
     * Gets 24-bit RGB pixels from a buffer. Returns null if conversion failed.
     * <p>
     * FIXME this does not work with sub-images use {@link #getPackedRgb24Raster(Buffer)} instead.
     */
    protected int[] getRGB24(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (int[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getRedMask() == 0xff0000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    }
                }
            }
            return image.getRGB(0, 0, //
                    outputFormat.get(WidthKey), outputFormat.get(HeightKey), //
                    null, 0, outputFormat.get(WidthKey));
        }
        return null;
    }

    /**
     * Gets 24-bit RGB pixels from a buffer. Returns null if conversion failed.
     */
    protected WritableRaster getPackedRgb24Raster(Buffer buf) {
        if (buf.data instanceof int[]) {
            return null;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getRedMask() == 0xff0000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return image.getRaster();
                    }
                }
            }
            Integer w = outputFormat.get(WidthKey);
            Integer h = outputFormat.get(HeightKey);
            int[] rgb = image.getRGB(0, 0, //
                    w, h, //
                    null, 0, w);
            return WritableRaster.createPackedRaster(new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0x00ff00, 0x0000ff}, new Point(0, 0));
        }
        return null;
    }

    /**
     * Gets 24-bit ARGB pixels from a buffer. Returns null if conversion failed.
     */
    protected WritableRaster getPackedArgb32Raster(Buffer buf) {
        if (buf.data instanceof int[]) {
            return null;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00
                        && dcm.getRedMask() == 0xff0000 && dcm.getAlphaMask() == 0xff000000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return image.getRaster();
                    }
                }
            }
            Integer w = outputFormat.get(WidthKey);
            Integer h = outputFormat.get(HeightKey);
            int[] rgb = image.getRGB(0, 0, //
                    w, h, //
                    null, 0, w);
            return WritableRaster.createPackedRaster(new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0x00ff00, 0x0000ff, 0xff000000}, new Point(0, 0));
        }
        return null;
    }

    /**
     * Gets 32-bit ARGB pixels from a buffer. Returns null if conversion failed.
     * <p>
     * FIXME this does not work with sub-images use {@link #getPackedArgb32Raster(Buffer)} instead.
     */
    protected int[] getARGB32(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (int[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getRedMask() == 0xff0000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    }
                }
            }
            return image.getRGB(0, 0, //
                    outputFormat.get(WidthKey), outputFormat.get(HeightKey), //
                    null, 0, outputFormat.get(WidthKey));
        }
        return null;
    }

    /**
     * Gets a buffered image from a buffer. Returns null if conversion failed.
     */
    protected BufferedImage getBufferedImage(Buffer buf) {
        if (buf.data instanceof BufferedImage) {
            return (BufferedImage) buf.data;
        }
        return null;
    }

    private byte[] byteBuf = new byte[4];

    protected void writeInt24(ImageOutputStream out, int v) throws IOException {
        byteBuf[0] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[2] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    protected void writeInt24LE(ImageOutputStream out, int v) throws IOException {
        byteBuf[2] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[0] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    protected void writeInts24(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            b[boff++] = (byte) (v >>> 16);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v);
        }

        out.write(b, 0, len * 3);
    }

    protected void writeInts24LE(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            b[boff++] = (byte) (v >>> 0);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v >>> 16);
            //b[boff++] = (byte)(v >>> 24);
        }

        out.write(b, 0, len * 3);
    }

    /**
     * Copies a buffered image.
     */
    protected static BufferedImage copyImage(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
