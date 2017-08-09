/* @(#)AmigaBitmapImage.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;

/**
 * A BitmapImage is comprised of a ColorModel and an accessible byte array of
 * image data.
 * <p>
 * The image data is expressed in several layers of rectangular regions
 * called bit-planes. To determine the bits that form a single pixel one
 * must combine all data-bits at the same x,y position in each bit-plane.
 * This is known as a "planar" storage layout as it was used on Commodore
 * Amiga Computers.
 * <p>
 * The bit-planes can be stored contiguously or can be interleaved at each
 * scanline of the image.
 * <p>
 * Fig 1. A sample image:
 * <pre>
 * .+++..@...@.+..###...+++.     This sample uses 4 colors:
 * +...+.@@.@@.+.#.....+...+     . = color 0 (all bits clear)
 * +++++:@.@.@.+.#..##.+++++     + = color 1 (bit 0 set, bit 1 clear)
 * +...+.@...@.+.#...#.+...+     @ = color 2 (bit 0 clear, bit 1 set)
 * +...+.@...@.+..####.+...+     # = color 3 (all bits set)
 * </pre>
 * <p>
 * Fig 2. Contiguous bit-plane storage layout.
 * <pre>
 * 01110000 00001001 11000111 0.......     This is the first bit-plane.
 * 10001000 00001010 00001000 1.......     Each number represents a bit
 * 11111000 00001010 01101111 1.......     in the storage layout. Eight
 * 10001000 00001010 00101000 1.......     bits are grouped into one byte.
 * 10001000 00001001 11101000 1.......     Dots indicate unused bits.
 *
 * 00000010 00100001 11000000 0.......     This is the second bit-plane.
 * 00000011 01100010 00000000 0.......
 * 00000010 10100010 01100000 0.......
 * 00000010 00100010 00100000 0.......
 * 00000010 00100001 11100000 0.......
 * </pre>
 * <p>
 * Fig 3. Interleaved bit-plane storage layout.
 * <pre>
 * 01110000 00001001 11000111 0.......     This is the first bit-plane.
 * 00000010 00100001 11000000 0.......     This is the second bit-plane.
 * 
 * 10001000 00001010 00001000 1.......     The bit-planes are interleaved
 * 00000011 01100010 00000000 0.......     at every scanline of the image.
 * 
 * 11111000 00001010 01101111 1.......
 * 00000010 10100010 01100000 0.......
 * 
 * 10001000 00001010 00101000 1.......
 * 00000010 00100010 00100000 0.......
 * 
 * 10001000 00001001 11101000 1.......
 * 00000010 00100001 11100000 0.......
 * </pre>
 * For more details refer to "Amiga ROM Kernel Reference Manual: Libraries,
 * Addison Wesley"
 * <p>
 * <b>Responsibility</b>
 * <p>
 * Gives clients direct access to the image data of the bitmap.
 * Knows how to convert the bitmap into chunky image data according
 * to the current color model.
 * Supports indexed color model, direct color model, 6 and 8 bit HAM color model.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public class AmigaBitmapImage
        implements Cloneable {

    /**  The bitmap data array. */
    private byte[] bitmap;
    /** The width of the image. */
    private int width;
    /** The height of the image. */
    private int height;
    /** The number of bits that form a single pixel. */
    private int depth;
    /** BitmapStride is the number of data array elements
     * between two bits of the same image pixel. */
    private int bitplaneStride;
    /** ScanlineStride is the number of data array elements
     * between a given  pixel and the pixel in the same column of
     * the next scanline. */
    private int scanlineStride;
    /** This ColorModel is used for the next conversion from planar
     * bitmap data into chunky pixel data.
     */
    private ColorModel planarColorModel;
    /** This ColorModel represents the preferred color model for chunky pixel.
     * If this value is null, then convertToChunky uses the planarColorModel_.
     */
    private ColorModel preferredChunkyColorModel_;
    /** This ColorModel represents the current color model for chunky pixel.
     */
    private ColorModel currentChunkyColorModel_;
    /** This ColorModel was used at the previous conversion from
     * planar bitmap into chunky pixel data.
     */
    private ColorModel lastPixelColorModel_;
    /** Indicates availability of chunky pixel data. */
    private int pixelType;
    /** Tag for byte pixel data. */
    public final static int BYTE_PIXEL = 1;
    /** Tag for integer pixel data. */
    public final static int INT_PIXEL = 2;
    /** Tag for short pixel data. */
    public final static int SHORT_PIXEL = 2;
    /** Tag indicating that no pixel data is available. */
    public final static int NO_PIXEL = 0;
    /** Output array for byte pixel data. */
    private byte[] bytePixels;
    /** Output array for integer pixel data. */
    private int[] intPixels;
    /** Output array for short pixel data. */
    private short[] shortPixels;
    /**
     * If this boolean is set to true, then convertToChunky always generates
     * chunky pixels using a DirectColorModel.
     */
    private boolean enforceDirectColors_ = false;

    /**
     * If you set this to true, then convertToChunky always generates
     * chunky pixels using a DirectColorModel.
     */
    public void setEnforceDirectColors(boolean b) {
        enforceDirectColors_ = b;
    }

    /**
     * If this returns true, then convertToChunky always generates
     * chunky pixels using a DirectColorModel.
     */
    public boolean isEnforceDirectColors() {
        return enforceDirectColors_;
    }

    /**
     * Construct an interleaved bitmap with the specified size,
     * depth and color model.
     * BitplaneStride and ScanlineStride are rounded up to the next
     * even number of bytes.
     * <p>
     * Pre condition:
     *   -
     * <p>
     * Post condition:
     *   Interleaved bitmap constructed.
     * <p>
     * Obligation:
     *   -
     *
     * @param  width  Width in pixels.
     * @param  height  Height in pixels.
     * @param  depth  Number of bits per pixel.
     * @param  colorModel  Color model to be used for conversions from/to chunky pixels.
     */
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel) {
        this(width, height, depth, colorModel, true);
    }

    /**
     * Construct a bitmap with the specified size, depth and color model
     * and with optional interleave.
     * BitplaneStride and ScanlineStride are rounded up to the next
     * even number of bytes.
     * <p>
     * Pre condition:
     *   -
     * <p>
     * Post condition:
     *   BitmapImage constructed.
     * <p>
     * Obligation:
     *   -
     *
     * @param  width  Width in pixels.
     * @param  height  Height in pixels.
     * @param  depth  Number of bits per pixel.
     * @param  colorModel  Color model to be used for conversions from/to chunky pixels.
     * @param  isInterleaved  Indicator for contiguous or interleaved bit-planes.
     */
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel, boolean isInterleaved) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.planarColorModel = colorModel;
        if (isInterleaved) {
            bitplaneStride = (width + 15) / 16 * 2;
            scanlineStride = bitplaneStride * depth;
            bitmap = new byte[scanlineStride * height];
        } else {
            scanlineStride = (width + 15) / 16 * 2;
            bitplaneStride = scanlineStride * depth;
            bitmap = new byte[bitplaneStride * height];
        }
        pixelType = NO_PIXEL;
    }

    /**
     * Construct a bitmap with the specified size, depth, color model and
     * interleave.
     * <p>
     * Pre condition:
     * ScanlineStride must be a multiple of BitplaneStride or vice versa.
     * <p>
     * Post condition:
     * BitmapImage constructed.
     * <p>
     * Obligation:
     *   -
     *
     * @param  width  Width in pixels.
     * @param  height  Height in pixels.
     * @param  depth  Number of bits per pixel.
     * @param  colorModel  Color model to be used for conversions from/to chunky pixels.
     * @param  bitStride  Number of data array elements between two bits of the same image pixel.
     * @param  scanlineStride  Number of data array elements between a given pixel and the pixel in the same column of
     * the next scanline.
     */
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel, int bitStride, int scanlineStride) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.planarColorModel = colorModel;
        this.bitplaneStride = bitStride;
        this.scanlineStride = scanlineStride;
        if (bitplaneStride < scanlineStride) {
            bitmap = new byte[scanlineStride * height];
        } else {
            bitmap = new byte[bitplaneStride * height];
        }
        pixelType = NO_PIXEL;
    }

    /**
     * Returns the width of the image.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the image.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The height in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the depth of the image.
     * <p>
     * The depth indicates how many bits are used to form a single pixel.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The number of bitplanes used to form a single pixel.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the numer of bytes you must add to a given address
     * in the bitmap to advance to the next scanline of the image.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The scansize.
     */
    public int getScanlineStride() {
        return scanlineStride;
    }

    /**
     * Returns the number of bytes that you must add to a bitmap address
     * to advance to the next bit of a scanline.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The interleave of the bitmap.
     */
    public int getBitplaneStride() {
        return bitplaneStride;
    }

    /**
     * Replaces the color model used for conversions from/to chunky pixels.
     * <p>
     * Pre condition: The new color model must correspond with the depth of the bitmap.
     * <p>
     * Post condition: Color model changed.
     * <p>
     * Obligation: -
     *
     * @param colorModel The new color model.
     */
    public void setPlanarColorModel(ColorModel colorModel) {
        planarColorModel = colorModel;
    }

    /**
     * Returns the current color model of the planar image in this bitmap.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The color model.
     */
    public ColorModel getPlanarColorModel() {
        return planarColorModel;
    }

    /**
     * Sets the preferred color model used for to chunky pixels.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: Color model changed.
     * <p>
     * Obligation: -
     *
     * @param  colorModel The new color model.
     */
    public void setPreferredChunkyColorModel(ColorModel colorModel) {
        preferredChunkyColorModel_ = colorModel;
    }

    /**
     * Returns the current color model of the chunky image in this bitmap.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  The color model.
     */
    public ColorModel getChunkyColorModel() {
        if (currentChunkyColorModel_ == null) {
            convertToChunky(0, 0, 0, 0);
        }
        return currentChunkyColorModel_;
    }

    /**
     * Gives you direct access to the bitmap data array.
     * <p>
     * Pre condition: -.
     * <p>
     * Post condition: -
     * <p>
 Obligation: The bitmap data array remains property
 of the AmigaBitmapImage and will be used at the next
 conversion to chunky. You can access it as you
 like (even during conversion) since this class
 does never change the contents of the bitmap.
     *
     * @return  A reference to the bitmap data.
     */
    public byte[] getBitmap() {
        return bitmap;
    }

    /**
     * Returns a reference to the byte pixel data that has been
     * generated by a previous call to #converToChunky.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: You may modify the contents of the array
     * as you like to get some nice effects for the
     * next call to #convertToChunky. Note whovewer that
     * #convertToChunky will not reuse this array when
     * the colorModel has been changed to a color format
     * that requires pixels in integer format.
     *
     * @return  byte array or NULL when no byte pixels have been
     * generated by #convertToChunky.
     */
    public byte[] getBytePixels() {
        if (pixelType == BYTE_PIXEL) {
            return bytePixels;
        } else {
            return null;
        }
    }

    /**
     * Returns a reference to the byte pixel data that has been
     * generated by a previous call to #converToChunky.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: You may modify the contents of the array
     * as you like to get some nice effects for the
     * next call to #convertToChunky. Note whovewer that
     * #convertToChunky will not reuse this array when
     * the colorModel has been changed to a color format
     * that requires pixels in integer format.
     *
     * @return  byte array or NULL when no byte pixels have been
     * generated by #convertToChunky.
     */
    public short[] getShortPixels() {
        if (pixelType == BYTE_PIXEL) {
            return shortPixels;
        } else {
            return null;
        }
    }

    /**
     * Returns a reference to the integer pixel data that has been
     * generated by a previous call to #converToChunky.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: You may modify the contents of the array
     * as you like to get some nice effects for the
     * next call to #convertToChunky. Note however that
     * #convertToChunky will not reuse this array when
     * the colorModel has been changed to a color format
     * that requires pixels in byte format.
     *
     * @return  byte array or NULL when no int pixels have been
     * generated by #convertToChunky.
     */
    public int[] getIntPixels() {
        if (pixelType == INT_PIXEL) {
            return intPixels;
        } else {
            return null;
        }
    }

    /**
     * Returns the available type of pixel data.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: -
     * <p>
     * Obligation: -
     *
     * @return  A constant that specifies the current type of pixel data.
     */
    public int getPixelType() {
        return pixelType;
    }

    /**
     * Creates a clone.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: Clone created.
     *
     * @return  A clone.
     */
    @Override
    public AmigaBitmapImage clone() {
        try {
            AmigaBitmapImage theClone = (AmigaBitmapImage) super.clone();
            theClone.bitmap = bitmap.clone();
            if (getPixelType() == BYTE_PIXEL) {
                theClone.bytePixels = bytePixels.clone();
            }
            if (getPixelType() == INT_PIXEL) {
                theClone.intPixels = intPixels.clone();
            }
            return theClone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Converts the planar image data into chunky pixel data.
     * <p>
     * This method will either generate byte pixel data or integer
     * pixel data (depending on the color model).
     * <p>
     * The pixel array that resulted to a prior call to this
     * method will be reused when the image dimension and the color
     * model allows for it.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: Chunky pixels generated.
     * <p>
     * Obligation: -
     *
     * @return The type of generated pixel data.
     */
    public int convertToChunky() {
        return convertToChunky(0, 0, getHeight() - 1, getWidth() - 1);
    }

    /**
     * Converts the indicated area of the bitmap data into  pixel data.
     * <p>
     * This method will either generate byte pixel data or integer
     * pixel data (depending on the color model).
     * <p>
     * Note that the size of the generated pixel data always corresponds
     * to the size of the complete image. You do only specify a subset
     * of the image to be <i>converted</i> not a subset to be extracted.
     * Note also that the pixel data that resulted from prior calls to
     * this method will be reused when the generated pixel array was
     * of the same size and type.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: The indicated part of the bitmap has been
     *   converted into chunky pixels.
     * <p>
     * Obligation: -
     *
     * @return The type of generated pixel data.
     */
    public int convertToChunky(int top, int left, int bottom, int right) {
        pixelType = NO_PIXEL;

        /* Ensure pre conditions are met. */
        if (top < 0) {
            top = 0;
        }
        if (left < 0) {
            left = 0;
        }
        if (bottom > getHeight() - 1) {
            bottom = getHeight() - 1;
        }
        if (right > getWidth() - 1) {
            right = getWidth() - 1;
        }

        /* */
        if (planarColorModel instanceof AmigaHAMColorModel) {
            if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                bytePixels = null;
                shortPixels = null;
                intPixels = new int[getWidth() * getHeight()];
            }
            currentChunkyColorModel_ = planarColorModel;
            if (((AmigaHAMColorModel) planarColorModel).getHAMType() == AmigaHAMColorModel.HAM6) {
                ham6PlanesToDirectPixels(top, left, bottom, right);
            } else if (((AmigaHAMColorModel) planarColorModel).getHAMType() == AmigaHAMColorModel.HAM8) {
                ham8PlanesToDirectPixels(top, left, bottom, right);
            } else {
                throw new InternalError("unsupported ham model:" + planarColorModel);
            }
            pixelType = INT_PIXEL;

        } else {
            if (planarColorModel instanceof IndexColorModel) {
                if (enforceDirectColors_ || preferredChunkyColorModel_ instanceof DirectColorModel) {
                    if (preferredChunkyColorModel_ != null && ((DirectColorModel) preferredChunkyColorModel_).getPixelSize() == 16) {
                        if (shortPixels == null || shortPixels.length != getWidth() * getHeight()) {
                            bytePixels = null;
                            intPixels = null;
                            shortPixels = null;
                            shortPixels = new short[getWidth() * getHeight()];
                        }
                        currentChunkyColorModel_ =
                                (preferredChunkyColorModel_ != null && (preferredChunkyColorModel_ instanceof DirectColorModel))
                                ? preferredChunkyColorModel_
                                : new DirectColorModel(16, 0x7c00, 0x3e0, 0x1f);

                        indexPlanesTo555(top, left, bottom, right);
                        pixelType = SHORT_PIXEL;
                    } else {
                        if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                            bytePixels = null;
                            shortPixels = null;
                            intPixels = new int[getWidth() * getHeight()];
                        }

                        currentChunkyColorModel_ =
                                (preferredChunkyColorModel_ != null && (preferredChunkyColorModel_ instanceof DirectColorModel))
                                ? preferredChunkyColorModel_
                                : ColorModel.getRGBdefault();

                        currentChunkyColorModel_ = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
                        indexPlanesToDirectPixels(top, left, bottom, right);
                        pixelType = INT_PIXEL;
                    }
                } else {
                    if (bytePixels == null || bytePixels.length != getWidth() * getHeight()) {
                        intPixels = null;
                        shortPixels = null;
                        bytePixels = new byte[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    indexPlanesToIndexPixels(top, left, bottom, right);
                    pixelType = BYTE_PIXEL;
                }
            } else if (planarColorModel instanceof DirectColorModel) {
                if (((DirectColorModel) planarColorModel).getPixelSize() == 16) {
                    if (shortPixels == null || shortPixels.length != getWidth() * getHeight()) {
                        bytePixels = null;
                        intPixels = null;
                        shortPixels = null;
                        shortPixels = new short[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    directPlanesTo555(top, left, bottom, right);
                    pixelType = SHORT_PIXEL;
                } else {
                    if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                        bytePixels = null;
                        shortPixels = null;
                        shortPixels = null;
                        intPixels = new int[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    directPlanesToDirectPixels(top, left, bottom, right);
                    pixelType = INT_PIXEL;
                }
            } else {
                throw new InternalError("unsupported color model:" + planarColorModel);
            }
        }
        return pixelType;
    }

    /**
     * Converts the indicated area of the bitmap data into  pixel data.
     * <p>
     * This method will either generate byte pixel data or integer
     * pixel data (depending on the color model).
     * <p>
     * Note that the size of the generated pixel data always corresponds
     * to the size of the complete image. You do only specify a subset
     * of the image to be <i>converted</i> not a subset to be extracted.
     * Note also that the pixel data that resulted from prior calls to
     * this method will be reused when the generated pixel array was
     * of the same size and type.
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: The indicated part of the bitmap has been
     *   converted into chunky pixels.
     * <p>
     * Obligation: -
     */
    public void convertFromChunky(BufferedImage image) {
        /* */
        if (planarColorModel instanceof AmigaHAMColorModel) {

            throw new UnsupportedOperationException("HAM mode not implemented:"+ planarColorModel);


        } else {
            if (planarColorModel instanceof IndexColorModel) {
                if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                    planarColorModel=image.getColorModel();
                    Raster raster=image.getRaster();
                    int dx=0,dy=0;
                    while (raster.getParent()!=null) {
                        dx+=raster.getMinX();
                        dy+=raster.getMinY();
                        raster=raster.getParent();
                    }
                   DataBufferByte dbuf= ((DataBufferByte)image.getRaster().getDataBuffer());
                  int inScanlineStride=raster.getWidth();
                    byte[] inb=dbuf.getData();
                    
                    if (bytePixels==null||bytePixels.length!=width*height) {
                        bytePixels=new byte[width*height];
                    }
                    
                    for (int y=0;y<height;y++) {
                        System.arraycopy(inb,dx+(y+dy)*inScanlineStride,bytePixels,y*width,width);
                    }
                    indexPixelsToIndexPlanes(0, 0, getHeight() - 1, getWidth() - 1);
                } else {
                
                throw new UnsupportedOperationException("index color model not implemented:" + planarColorModel);
                }
            } else if (planarColorModel instanceof DirectColorModel) {
                throw new UnsupportedOperationException("index color model not implemented:" + planarColorModel);
            } else {
                throw new UnsupportedOperationException("unsupported color model:" + planarColorModel);
            }
        }
    }

    /**
     * Frees the memory allocated for the pixel data.
     *
     * <p>
     * Pre condition: -
     * <p>
     * Post condition: The bitmap has given up all its
     * references to the pixel data.
     * <p>
     * Obligation: The pixel data will not be reused at the
     * next call to #convertToChunky.
     */
    public void flushPixels() {
        pixelType = NO_PIXEL;
        intPixels = null;
        shortPixels = null;
        bytePixels = null;
    }

    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the IndexColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of java.awt.IndexColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     *
     * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
     * @version  1997-10-16  Created.
     */
    private void indexPlanesToIndexPixels(int top, int left, int bottom, int right) {

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = depth - 8;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        int iBitmap;
        int iScanline;
        int iDepth;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (((bitmap_[iBitmap] << bitShift) & 128) >>> 7);
                }
                iPixel += pixelLineStride;
                }
                 */
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        bytePixels[iPixel++] = (byte) (((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7);
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x & 7;
                iBitmap = iScanline + x >>> 3;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                );
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x & 7;
                iBitmap = iScanline + x >>> 3;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                );
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                int bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                );
                }
                iPixel += pixelLineStride;
                }*/

                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }
                iPixel=0;
                 */
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                | (bitmap_[iBitmap+bitplaneStride6] & bitMask) << 6
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                | (bitmap_[iBitmap+bitplaneStride6] & bitMask) << 6
                | (bitmap_[iBitmap+bitplaneStride7] & bitMask) << 7
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                for (iDepth = depth; iDepth > 0; iDepth--) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                //bytePixels_[iPixel++] = (byte)(pixel >>> bitCorrection);
                bytePixels_[iPixel++] = (byte)(pixel);
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        bytePixels[iPixel++] = (byte) (pixel >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
        }
    }
    private void indexPixelsToIndexPlanes(int top, int left, int bottom, int right) {

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = depth - 8;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        int iBitmap;
        int iScanline;
        int iDepth;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        switch (depth) {
            case 1:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        bytePixels[iPixel++] = (byte) (((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7);
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 6:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        int px=bytePixels[iPixel++];
                        b7=(b7<<1)|((px>>>7)&1);
                        b6=(b6<<1)|((px>>>6)&1);
                        b5=(b5<<1)|((px>>>5)&1);
                        b4=(b4<<1)|((px>>>4)&1);
                        b3=(b3<<1)|((px>>>3)&1);
                        b2=(b2<<1)|((px>>>2)&1);
                        b1=(b1<<1)|((px>>>1)&1);
                        b0=(b0<<1)|((px>>>0)&1);
                        
                        if (iBit == 7) {
                             bitmap[iBitmap]=(byte)b0;
                             bitmap[iBitmap + bitplaneStride]=(byte)b1;
                             bitmap[iBitmap + bitplaneStride2]=(byte)b2;
                             bitmap[iBitmap + bitplaneStride3]=(byte)b3;
                             bitmap[iBitmap + bitplaneStride4]=(byte)b4;
                             bitmap[iBitmap + bitplaneStride5]=(byte)b5;
                             bitmap[iBitmap + bitplaneStride6]=(byte)b6;
                             bitmap[iBitmap + bitplaneStride7]=(byte)b7;
                        }
                    }
                    // FIXME - Add special treatment here when width is not a multiple of 8
                    
                    iPixel += pixelLineStride; 
                }
                break;

            default:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        bytePixels[iPixel++] = (byte) (pixel >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
        }
    }
    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the DirectColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of java.awt.IndexColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     */
    private void indexPlanesToDirectPixels(int top, int left, int bottom, int right) {
        IndexColorModel colorModel = (IndexColorModel) planarColorModel;
        final int[] clut = new int[colorModel.getMapSize()];
        //colorModel.getRGBs(clut);
        IndexColorModel icm = (IndexColorModel) planarColorModel;
        byte[] reds = new byte[clut.length];
        byte[] greens = new byte[clut.length];
        byte[] blues = new byte[clut.length];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < clut.length; i++) {
            clut[i] = 0xff000000 | (reds[i] & 0xff) << 16 | (greens[i] & 0xff) << 8 | (blues[i] & 0xff);
        }
        if (clut.length < (1 << getDepth())) {
            throw new IndexOutOfBoundsException("Clut must not be smaller than depth");
        }


        /*
        int transparentPixel = colorModel.getTransparentPixel();
        if (transparentPixel != -1) {
        clut[transparentPixel] &= 0x00ffffff;
        }
        }*/

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;


        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(((bitmap_[iBitmap] << bitShift) & 128) >>> 7)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        intPixels[iPixel++] = clut[(((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                )];
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                intPixels_[iPixel++] = clut[(
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }

                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                
                for (iDepth = 0; iDepth < depth; iDepth++) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                intPixels_[iPixel++] = clut[(pixel >>> bitCorrection)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        intPixels[iPixel++] =
                                clut[pixel >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
        }
    }

    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the DirectColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of java.awt.IndexColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     */
    private void indexPlanesTo555(int top, int left, int bottom, int right) {
        IndexColorModel colorModel = (IndexColorModel) planarColorModel;
        final short[] clut = new short[colorModel.getMapSize()];
        //colorModel.getRGBs(clut);
        IndexColorModel icm = (IndexColorModel) planarColorModel;
        byte[] reds = new byte[clut.length];
        byte[] greens = new byte[clut.length];
        byte[] blues = new byte[clut.length];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < clut.length; i++) {
            clut[i] = (short) ((reds[i] & 0xf8) << 7 | (greens[i] & 0xf8) << 2 | (blues[i] & 0xf8) >> 3);
        }
        if (clut.length < (1 << getDepth())) {
            throw new IndexOutOfBoundsException("Clut must not be smaller than depth");
        }


        /*
        int transparentPixel = colorModel.getTransparentPixel();
        if (transparentPixel != -1) {
        clut[transparentPixel] &= 0x00ffffff;
        }
        }*/

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;


        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(((bitmap_[iBitmap] << bitShift) & 128) >>> 7)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        shortPixels[iPixel++] = clut[(((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                )];
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                
                intPixels_[iPixel++] = clut[(
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }

                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                
                for (iDepth = 0; iDepth < depth; iDepth++) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                intPixels_[iPixel++] = clut[(pixel >>> bitCorrection)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        shortPixels[iPixel++] =
                                clut[pixel >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
        }
    }

    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the DirectColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of java.awt.DirectColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     */
    private void directPlanesToDirectPixels(int top, int left, int bottom, int right) {
        /*
        // This section shows the original algorithm.
        
        final int depth = getDepth();
        final int width = getWidth();
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int iScanline, x, iBitmap, iDepth, bitShift;
        int pixel = 0;
        int iPixel = top * width + left;
        
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride)
        {
        for (x = left; x < right; x++)
        {
        bitShift = x % 8 + 16;
        iBitmap = iScanline + x / 8;
        for (iDepth = depth; iDepth > 0; iDepth--)
        {
        pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 0x800000);
        iBitmap += bitplaneStride;
        }
        intPixels_[iPixel++] = 0xff000000 | ((pixel >>> 16) & 0xff) + (pixel & 0xff00) + ((pixel << 16) & 0xff0000);
        }
        iPixel += pixelLineStride;
        }
         */
        /*
        // Eliminating the innermost loop increases the performance
        // by 37 percent.
        
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;
        final int bitplaneStride8 = bitplaneStride * 8;
        final int bitplaneStride9 = bitplaneStride * 9;
        final int bitplaneStride10 = bitplaneStride * 10;
        final int bitplaneStride11 = bitplaneStride * 11;
        final int bitplaneStride12 = bitplaneStride * 12;
        final int bitplaneStride13 = bitplaneStride * 13;
        final int bitplaneStride14 = bitplaneStride * 14;
        final int bitplaneStride15 = bitplaneStride * 15;
        final int bitplaneStride16 = bitplaneStride * 16;
        final int bitplaneStride17 = bitplaneStride * 17;
        final int bitplaneStride18 = bitplaneStride * 18;
        final int bitplaneStride19 = bitplaneStride * 19;
        final int bitplaneStride20 = bitplaneStride * 20;
        final int bitplaneStride21 = bitplaneStride * 21;
        final int bitplaneStride22 = bitplaneStride * 22;
        final int bitplaneStride23 = bitplaneStride * 23;
        
        int iBitmap = top * scanlineStride + left / 8;
        
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride)
        {
        for (x = left; x < right; x++)
        {
        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        intPixels_[iPixel++] = 0xff000000 |
        ((bitmap_[iBitmap] << bitShift) & 128) << 9 |
        ((bitmap_[iBitmap+bitplaneStride] << bitShift) & 128) << 10 |
        ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) << 11 |
        ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) << 12 |
        ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) << 13 |
        ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) << 14 |
        ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) << 15 |
        ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128) << 16 |
        ((bitmap_[iBitmap+bitplaneStride8] << bitShift) & 128) << 1 |
        ((bitmap_[iBitmap+bitplaneStride9] << bitShift) & 128) << 2 |
        ((bitmap_[iBitmap+bitplaneStride10] << bitShift) & 128) << 3 |
        ((bitmap_[iBitmap+bitplaneStride11] << bitShift) & 128) << 4 |
        ((bitmap_[iBitmap+bitplaneStride12] << bitShift) & 128) << 5 |
        ((bitmap_[iBitmap+bitplaneStride13] << bitShift) & 128) << 6 |
        ((bitmap_[iBitmap+bitplaneStride14] << bitShift) & 128) << 7 |
        ((bitmap_[iBitmap+bitplaneStride15] << bitShift) & 128) << 8 |
        ((bitmap_[iBitmap+bitplaneStride16] << bitShift) & 128) >>> 7 |
        ((bitmap_[iBitmap+bitplaneStride17] << bitShift) & 128) >>> 6 |
        ((bitmap_[iBitmap+bitplaneStride18] << bitShift) & 128) >>> 5 |
        ((bitmap_[iBitmap+bitplaneStride19] << bitShift) & 128) >>> 4 |
        ((bitmap_[iBitmap+bitplaneStride20] << bitShift) & 128) >>> 3 |
        ((bitmap_[iBitmap+bitplaneStride21] << bitShift) & 128) >>> 2 |
        ((bitmap_[iBitmap+bitplaneStride22] << bitShift) & 128) >>> 1 |
        ((bitmap_[iBitmap+bitplaneStride23] << bitShift) & 128)
        ;
        }
        iPixel += pixelLineStride;
        }
         */

        // Eliminating the innermost loop and avoiding unnecessary
        // array accesses improves performance by 56 percent
        // regarding to the original algorithm.

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;
        final int bitplaneStride8 = bitplaneStride * 8;
        final int bitplaneStride9 = bitplaneStride * 9;
        final int bitplaneStride10 = bitplaneStride * 10;
        final int bitplaneStride11 = bitplaneStride * 11;
        final int bitplaneStride12 = bitplaneStride * 12;
        final int bitplaneStride13 = bitplaneStride * 13;
        final int bitplaneStride14 = bitplaneStride * 14;
        final int bitplaneStride15 = bitplaneStride * 15;
        final int bitplaneStride16 = bitplaneStride * 16;
        final int bitplaneStride17 = bitplaneStride * 17;
        final int bitplaneStride18 = bitplaneStride * 18;
        final int bitplaneStride19 = bitplaneStride * 19;
        final int bitplaneStride20 = bitplaneStride * 20;
        final int bitplaneStride21 = bitplaneStride * 21;
        final int bitplaneStride22 = bitplaneStride * 22;
        final int bitplaneStride23 = bitplaneStride * 23;

        int iBitmap = top * scanlineStride + left / 8;
        int b0 = bitmap[iBitmap];
        int b1 = bitmap[iBitmap + bitplaneStride];
        int b2 = bitmap[iBitmap + bitplaneStride2];
        int b3 = bitmap[iBitmap + bitplaneStride4];
        int b4 = bitmap[iBitmap + bitplaneStride4];
        int b5 = bitmap[iBitmap + bitplaneStride5];
        int b6 = bitmap[iBitmap + bitplaneStride6];
        int b7 = bitmap[iBitmap + bitplaneStride7];
        int b8 = bitmap[iBitmap + bitplaneStride8];
        int b9 = bitmap[iBitmap + bitplaneStride9];
        int b10 = bitmap[iBitmap + bitplaneStride10];
        int b11 = bitmap[iBitmap + bitplaneStride11];
        int b12 = bitmap[iBitmap + bitplaneStride12];
        int b13 = bitmap[iBitmap + bitplaneStride13];
        int b14 = bitmap[iBitmap + bitplaneStride14];
        int b15 = bitmap[iBitmap + bitplaneStride15];
        int b16 = bitmap[iBitmap + bitplaneStride16];
        int b17 = bitmap[iBitmap + bitplaneStride17];
        int b18 = bitmap[iBitmap + bitplaneStride18];
        int b19 = bitmap[iBitmap + bitplaneStride19];
        int b20 = bitmap[iBitmap + bitplaneStride20];
        int b21 = bitmap[iBitmap + bitplaneStride21];
        int b22 = bitmap[iBitmap + bitplaneStride22];
        int b23 = bitmap[iBitmap + bitplaneStride23];
        /*
        
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        for (x = left; x < right; x++) {
        iBitmap = iScanline + x / 8;
        bitShift = x % 8;
        if (bitShift == 0) {
        b0 = bitmap_[iBitmap];
        b1 = bitmap_[iBitmap+bitplaneStride];
        b2 = bitmap_[iBitmap+bitplaneStride2];
        b3 = bitmap_[iBitmap+bitplaneStride3];
        b4 = bitmap_[iBitmap+bitplaneStride4];
        b5 = bitmap_[iBitmap+bitplaneStride5];
        b6 = bitmap_[iBitmap+bitplaneStride6];
        b7 = bitmap_[iBitmap+bitplaneStride7];
        b8 = bitmap_[iBitmap+bitplaneStride8];
        b9 = bitmap_[iBitmap+bitplaneStride9];
        b10 = bitmap_[iBitmap+bitplaneStride10];
        b11 = bitmap_[iBitmap+bitplaneStride11];
        b12 = bitmap_[iBitmap+bitplaneStride12];
        b13 = bitmap_[iBitmap+bitplaneStride13];
        b14 = bitmap_[iBitmap+bitplaneStride14];
        b15 = bitmap_[iBitmap+bitplaneStride15];
        b16 = bitmap_[iBitmap+bitplaneStride16];
        b17 = bitmap_[iBitmap+bitplaneStride17];
        b18 = bitmap_[iBitmap+bitplaneStride18];
        b19 = bitmap_[iBitmap+bitplaneStride19];
        b20 = bitmap_[iBitmap+bitplaneStride20];
        b21 = bitmap_[iBitmap+bitplaneStride21];
        b22 = bitmap_[iBitmap+bitplaneStride22];
        b23 = bitmap_[iBitmap+bitplaneStride23];
        }
        intPixels_[iPixel++] =
        0xff000000
        | ((b0 << bitShift) & 128) << 9
        | ((b1 << bitShift) & 128) << 10
        | ((b2 << bitShift) & 128) << 11
        | ((b3 << bitShift) & 128) << 12
        | ((b4 << bitShift) & 128) << 13
        | ((b5 << bitShift) & 128) << 14
        | ((b6 << bitShift) & 128) << 15
        | ((b7 << bitShift) & 128) << 16
        | ((b8 << bitShift) & 128) << 1
        | ((b9 << bitShift) & 128) << 2
        | ((b10 << bitShift) & 128) << 3
        | ((b11 << bitShift) & 128) << 4
        | ((b12 << bitShift) & 128) << 5
        | ((b13 << bitShift) & 128) << 6
        | ((b14 << bitShift) & 128) << 7
        | ((b15 << bitShift) & 128) << 8
        | ((b16 << bitShift) & 128) >>> 7
        | ((b17 << bitShift) & 128) >>> 6
        | ((b18 << bitShift) & 128) >>> 5
        | ((b19 << bitShift) & 128) >>> 4
        | ((b20 << bitShift) & 128) >>> 3
        | ((b21 << bitShift) & 128) >>> 2
        | ((b22 << bitShift) & 128) >>> 1
        | ((b23 << bitShift) & 128)
        ;
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;*/
        int iBit, bitMask;
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                    b6 = bitmap[iBitmap + bitplaneStride6];
                    b7 = bitmap[iBitmap + bitplaneStride7];
                    b8 = bitmap[iBitmap + bitplaneStride8];
                    b9 = bitmap[iBitmap + bitplaneStride9];
                    b10 = bitmap[iBitmap + bitplaneStride10];
                    b11 = bitmap[iBitmap + bitplaneStride11];
                    b12 = bitmap[iBitmap + bitplaneStride12];
                    b13 = bitmap[iBitmap + bitplaneStride13];
                    b14 = bitmap[iBitmap + bitplaneStride14];
                    b15 = bitmap[iBitmap + bitplaneStride15];
                    b16 = bitmap[iBitmap + bitplaneStride16];
                    b17 = bitmap[iBitmap + bitplaneStride17];
                    b18 = bitmap[iBitmap + bitplaneStride18];
                    b19 = bitmap[iBitmap + bitplaneStride19];
                    b20 = bitmap[iBitmap + bitplaneStride20];
                    b21 = bitmap[iBitmap + bitplaneStride21];
                    b22 = bitmap[iBitmap + bitplaneStride22];
                    b23 = bitmap[iBitmap + bitplaneStride23];
                }

                intPixels[iPixel++] = ((b0 & bitMask) << 16
                        | (b1 & bitMask) << 17
                        | (b2 & bitMask) << 18
                        | (b3 & bitMask) << 19
                        | (b4 & bitMask) << 20
                        | (b5 & bitMask) << 21
                        | (b6 & bitMask) << 22
                        | (b7 & bitMask) << 23
                        | (b8 & bitMask) << 8
                        | (b9 & bitMask) << 9
                        | (b10 & bitMask) << 10
                        | (b11 & bitMask) << 11
                        | (b12 & bitMask) << 12
                        | (b13 & bitMask) << 13
                        | (b14 & bitMask) << 14
                        | (b15 & bitMask) << 15
                        | (b16 & bitMask)
                        | (b17 & bitMask) << 1
                        | (b18 & bitMask) << 2
                        | (b19 & bitMask) << 3
                        | (b20 & bitMask) << 4
                        | (b21 & bitMask) << 5
                        | (b22 & bitMask) << 6
                        | (b23 & bitMask) << 7) >>> (7 - iBit);

            }
            iPixel += pixelLineStride;
        }
    }

    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the HAMColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of HAMColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     *
     * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
     * @version  1997-10-16  Created.
     */
    private void ham6PlanesToDirectPixels(int top, int left, int bottom, int right) {
        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int[] HAMColors = new int[((AmigaHAMColorModel) planarColorModel).getMapSize()];
        ((AmigaHAMColorModel) planarColorModel).getRGBs(HAMColors);
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int lastPixel, iLastPixel = top * width + left - 1;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        int iBitmap;
        int b0, b1, b2, b3, b4, b5;
        b0 = b1 = b2 = b3 = b4 = b5 = 0;
        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position
        /*
        
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        if (left == 0) {
        lastPixel = 0xff000000;
        } else {
        lastPixel = intPixels_[iLastPixel];
        iLastPixel += width;
        }
        for (x = left; x < right; x++) {
        
        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        pixel =
        ((bitmap_[iBitmap] << bitShift) & 128) >>> 3
        | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 2
        | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 1
        | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128)
        ;
        
        switch (((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 7
        | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 6) {
        
        case 0: // use indexed color
        intPixels_[iPixel++] = lastPixel = HAMColors[pixel >>> 4];
        break;
        
        case 1: // modifie blue
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel >>> 4;
        break;
        
        case 2:  // modify red
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 12 & 0x000f0000;
        break;
        
        default: // modify green
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 4 & 0x0f00;
        break;
        }
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;
        iLastPixel = top*width + left - 1;
         */
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            if (left == 0) {
                lastPixel = 0xff000000;
            } else {
                lastPixel = intPixels[iLastPixel];
                iLastPixel += width;
            }
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                }
                pixel = ((b0 & bitMask)
                        | (b1 & bitMask) << 1
                        | (b2 & bitMask) << 2
                        | (b3 & bitMask) << 3) >>> (7 - iBit);

                switch (((b4 & bitMask)
                        | (b5 & bitMask) << 1) >>> (7 - iBit)) {

                    case 0: // use indexed color
                        intPixels[iPixel++] = lastPixel = HAMColors[pixel];
                        break;

                    case 1: // modifie blue
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel << 4;
                        break;

                    case 2:  // modify red
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 20;
                        break;

                    default: // modify green
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 12;
                        break;
                }
            }
            iPixel += pixelLineStride;
        }
    }

    /**
     * Converts the planar image data into chunky pixels.
     *
     * After successful completion the chunky pixels can by used
     * in conjunction with the HAMColorModel associated to
     * this instance.
     *
     * Pre condition
     *   The color model must be an instance of HAMColorModel.
     *   0 <= topBound <= bottomBound <= height.
     *   0 <= leftBound <= rightBound <= width.
     * Post condition
     *   -
     * Obligation
     *   -
     */
    private void ham8PlanesToDirectPixels(int top, int left, int bottom, int right) {
        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int[] HAMColors = new int[((AmigaHAMColorModel) planarColorModel).getMapSize()];
        ((AmigaHAMColorModel) planarColorModel).getRGBs(HAMColors);
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int lastPixel, iLastPixel = top * width + left - 1;
        int pixel = 0;
        int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap = top * scanlineStride + left / 8;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position
        /*
        
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        if (left == 0) {
        lastPixel = 0xff000000;
        } else {
        lastPixel = intPixels_[iLastPixel];
        iLastPixel += width;
        }
        for (x = left; x < right; x++) {
        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        pixel =
        ((bitmap_[iBitmap] << bitShift) & 128) >>> 5
        | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 4
        | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 3
        | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 2
        | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 1
        | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128)
        ;
        
        switch (((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 7
        | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128) >>> 6) {
        
        case 0: // use indexed color
        intPixels_[iPixel++] = lastPixel = HAMColors[pixel >>> 2];
        break;
        
        case 1: // modifie blue
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel >>> 6;
        break;
        
        case 2: // modify red
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 10 & 0x030000;
        break;
        
        default: // modify green
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 2 & 0x0300;
        break;
        
        }
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;
        iLastPixel = top*width + left - 1;
         */
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            if (left == 0) {
                lastPixel = 0xff000000;
            } else {
                lastPixel = intPixels[iLastPixel];
                iLastPixel += width;
            }
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                    b6 = bitmap[iBitmap + bitplaneStride6];
                    b7 = bitmap[iBitmap + bitplaneStride7];
                }
                pixel = ((b0 & bitMask)
                        | (b1 & bitMask) << 1
                        | (b2 & bitMask) << 2
                        | (b3 & bitMask) << 3
                        | (b4 & bitMask) << 4
                        | (b5 & bitMask) << 5) >>> (7 - iBit);

                switch (((b6 & bitMask)
                        | (b7 & bitMask) << 1) >>> (7 - iBit)) {

                    case 0: // use indexed color
                        intPixels[iPixel++] = lastPixel = HAMColors[pixel];
                        break;

                    case 1: // modifie blue
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel << 2 | pixel >>> 4;
                        break;

                    case 2:  // modify red
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 18 | (pixel & 0x03) << 16;
                        break;

                    default: // modify green
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 10 | (pixel & 0x03) << 8;
                        break;
                }
            }
            iPixel += pixelLineStride;
        }
    }

    public void setIntPixels(int[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        intPixels = buf;
    }

    public void setBytePixels(byte[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        bytePixels = buf;
    }

    public void setShortPixels(short[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        shortPixels = buf;
    }

    private void directPlanesTo555(int top, int left, int bottom, int right) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
