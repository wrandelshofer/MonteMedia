/*
 * @(#)ImageBufferToArray.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.video;

import org.monte.media.av.Buffer;

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

import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;

/// Extracts pixel data from a [Buffer] that contains an image.
public class ImageBufferToArray {

    private BufferedImage imgConverter;

    /// Gets a buffered image from a buffer. Returns null if conversion failed.
    public BufferedImage getBufferedImage(Buffer buf) {
        if (buf.data instanceof BufferedImage) {
            return (BufferedImage) buf.data;
        }
        return null;
    }

    /// Gets 8-bit indexed pixels from a buffer. Returns null if conversion failed.
    public byte[] getIndexed8(Buffer buf) {
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

    /// Gets 15-bit RGB pixels from a buffer. Returns null if conversion failed.
    public short[] getRGB15(Buffer buf, int width, int height) {
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
                imgConverter = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
            }
            Graphics2D g = imgConverter.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return ((DataBufferUShort) imgConverter.getRaster().getDataBuffer()).getData();
        }
        return null;
    }

    /// Gets 16-bit RGB-5-6-5 pixels from a buffer. Returns null if conversion failed.
    public short[] getRGB16(Buffer buf, int width, int height) {
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
                imgConverter = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            }
            Graphics2D g = imgConverter.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return ((DataBufferUShort) imgConverter.getRaster().getDataBuffer()).getData();
        }
        return null;
    }


    /// Gets 24-bit RGB pixels from a buffer. Returns null if conversion failed.
    ///
    /// FIXME this does not work with sub-images use [#getPackedRgb24Raster(Buffer,int,int)] instead.
    public int[] getRGB24(Buffer buf, int width, int height) {
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
                    width, height, //
                    null, 0, width);
        }
        return null;
    }

    /// Gets 24-bit RGB pixels from a buffer. Returns null if conversion failed.
    public WritableRaster getPackedRgb24Raster(Buffer buf, int w, int h) {
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
            int[] rgb = image.getRGB(0, 0, //
                    w, h, //
                    null, 0, w);
            return WritableRaster.createPackedRaster(new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0x00ff00, 0x0000ff}, new Point(0, 0));
        }
        return null;
    }

    /// Gets 24-bit ARGB pixels from a buffer. Returns null if conversion failed.
    public WritableRaster getPackedArgb32Raster(Buffer buf, int w, int h) {
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
            int[] rgb = image.getRGB(0, 0, //
                    w, h, //
                    null, 0, w);
            return WritableRaster.createPackedRaster(new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0x00ff00, 0x0000ff, 0xff000000}, new Point(0, 0));
        }
        return null;
    }

    /// Gets 32-bit ARGB pixels from a buffer. Returns null if conversion failed.
    ///
    /// FIXME this does not work with sub-images use [#getPackedArgb32Raster(Buffer,int,int)] instead.
    public int[] getARGB32(Buffer buf, int width, int height) {
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
                    width, height, //
                    null, 0, width);
        }
        return null;
    }

    public ColorModel getColorModel(Buffer buf) {
        if (buf.header instanceof ColorModel) {
            return (ColorModel) buf.header;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            return image.getColorModel();
        }
        return buf.format.get(PaletteKey);
    }
}
