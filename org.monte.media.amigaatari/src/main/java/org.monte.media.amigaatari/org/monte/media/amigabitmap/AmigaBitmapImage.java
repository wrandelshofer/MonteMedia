/*
 * @(#)AmigaBitmapImage.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.image.ColorModel;

/// A BitmapImage consists of a ColorModel and an accessible byte array of
/// image data.
///
/// The image data is expressed in several layers of rectangular regions
/// called bit-planes. To determine the bits that form a single pixel one
/// must combine all data-bits at the same x,y position in each bit-plane.
/// This is known as a "planar" storage layout as it was used on Commodore
/// Amiga Computers.
///
/// The bit-planes can be stored contiguously or can be interleaved at each
/// scanline of the image.
///
///
/// ```
/// .+++..@...@.+..###...+++.     This sample uses 4 colors:
/// +...+.@@.@@.+.#.....+...+     . = color 0 (all bits clear)
/// +++++:@.@.@.+.#..##.+++++     + = color 1 (bit 0 set, bit 1 clear)
/// +...+.@...@.+.#...#.+...+     @ = color 2 (bit 0 clear, bit 1 set)
/// +...+.@...@.+..####.+...+     # = color 3 (all bits set)
/// ```
/// Fig 1. A sample image
///
///
/// ```
/// 01110000 00001001 11000111 0.......     This is the first bit-plane.
/// 10001000 00001010 00001000 1.......     Each number represents a bit
/// 11111000 00001010 01101111 1.......     in the storage layout. Eight
/// 10001000 00001010 00101000 1.......     bits are grouped into one byte.
/// 10001000 00001001 11101000 1.......     Dots indicate unused bits.
///
/// 00000010 00100001 11000000 0.......     This is the second bit-plane.
/// 00000011 01100010 00000000 0.......
/// 00000010 10100010 01100000 0.......
/// 00000010 00100010 00100000 0.......
/// 00000010 00100001 11100000 0.......
/// ```
/// Fig 2. Contiguous bit-plane storage layout.
///
///
/// ```
/// 01110000 00001001 11000111 0.......     This is the first bit-plane.
/// 00000010 00100001 11000000 0.......     This is the second bit-plane.
///
/// 10001000 00001010 00001000 1.......     The bit-planes are interleaved
/// 00000011 01100010 00000000 0.......     at every scanline of the image.
///
/// 11111000 00001010 01101111 1.......
/// 00000010 10100010 01100000 0.......
///
/// 10001000 00001010 00101000 1.......
/// 00000010 00100010 00100000 0.......
///
/// 10001000 00001001 11101000 1.......
/// 00000010 00100001 11100000 0.......
/// ```
/// Fig 3. Interleaved bit-plane storage layout.
///
/// For more details refer to "Amiga ROM Kernel Reference Manual: Libraries,
/// Addison Wesley"
///
/// **Responsibility**
///
/// Gives clients direct access to the image data of the bitmap.
/// Knows how to convert the bitmap into chunky image data according
/// to the current color model.
/// Supports indexed color model, direct color model, 6 and 8 bit HAM color model.
///
/// @author Werner Randelshofer
public class AmigaBitmapImage
        implements Cloneable {

    /// The bitmap data array.
    private byte[] bitmap;
    /// The width of the image.
    private int width;
    /// The height of the image.
    private int height;
    /// The number of bits that form a single pixel.
    private int depth;
    /// BitmapStride is the number of data array elements
    /// between two bits of the same image pixel.
    ///
    /// This number is always even.
    private int bitplaneStride;
    /// ScanlineStride is the number of data array elements
    /// between a given  pixel and the pixel in the same column of
    /// the next scanline.
    ///
    /// This number is always even.
    private int scanlineStride;
    /// This ColorModel is used for the next conversion from planar
    /// bitmap data into chunky pixel data.
    private ColorModel colorModel;


    /// Construct an interleaved bitmap with the specified size,
    /// depth and color model.
    /// BitplaneStride and ScanlineStride are rounded up to the next
    /// even number of bytes.
    ///
    /// Pre condition:
    /// -
    ///
    /// Post condition:
    /// Interleaved bitmap constructed.
    ///
    /// Obligation:
    /// -
    ///
    /// @param width      Width in pixels.
    /// @param height     Height in pixels.
    /// @param depth      Number of bits per pixel.
    /// @param colorModel Color model to be used for conversions from/to chunky pixels.
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel) {
        this(width, height, depth, colorModel, true);
    }

    /// Construct a bitmap with the specified size, depth and color model
    /// and with optional interleave.
    /// BitplaneStride and ScanlineStride are rounded up to the next
    /// even number of bytes.
    ///
    /// Pre condition:
    /// -
    ///
    /// Post condition:
    /// BitmapImage constructed.
    ///
    /// Obligation:
    /// -
    ///
    /// @param width         Width in pixels.
    /// @param height        Height in pixels.
    /// @param depth         Number of bits per pixel.
    /// @param colorModel    Color model to be used for conversions from/to chunky pixels.
    /// @param isInterleaved Indicator for contiguous or interleaved bit-planes.
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel, boolean isInterleaved) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.colorModel = colorModel;
        if (isInterleaved) {
            bitplaneStride = (width + 15) / 16 * 2;
            scanlineStride = bitplaneStride * depth;
            bitmap = new byte[scanlineStride * height];
        } else {
            scanlineStride = (width + 15) / 16 * 2;
            bitplaneStride = scanlineStride * height;
            bitmap = new byte[bitplaneStride * height];
        }
    }

    /// Construct a bitmap with the specified size, depth, color model and
    /// interleave.
    ///
    /// Pre condition:
    /// ScanlineStride must be a multiple of BitplaneStride or vice versa.
    ///
    /// Post condition:
    /// BitmapImage constructed.
    ///
    /// Obligation:
    /// -
    ///
    /// @param width          Width in pixels.
    /// @param height         Height in pixels.
    /// @param depth          Number of bits per pixel.
    /// @param colorModel     Color model to be used for conversions from/to chunky pixels.
    /// @param bitStride      Number of data array elements between two bits of the same image pixel.
    /// @param scanlineStride Number of data array elements between a given pixel and the pixel in the same column of
    ///                                                                                         the next scanline.
    public AmigaBitmapImage(int width, int height, int depth, ColorModel colorModel, int bitStride, int scanlineStride) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.colorModel = colorModel;
        this.bitplaneStride = bitStride;
        this.scanlineStride = scanlineStride;
        if (bitplaneStride < scanlineStride) {
            bitmap = new byte[scanlineStride * height];
        } else {
            bitmap = new byte[bitplaneStride * height];
        }
    }

    /// Returns the width of the image.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The width in pixels.
    public int getWidth() {
        return width;
    }

    /// Returns the height of the image.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The height in pixels.
    public int getHeight() {
        return height;
    }

    /// Returns the depth of the image.
    ///
    /// The depth indicates how many bits are used to form a single pixel.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The number of bitplanes used to form a single pixel.
    public int getDepth() {
        return depth;
    }

    /// Returns the numer of bytes you must add to a given address
    /// in the bitmap to advance to the next scanline of the image.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The scansize.
    public int getScanlineStride() {
        return scanlineStride;
    }

    /// Returns the number of bytes that you must add to a bitmap address
    /// to advance to the next bit of a scanline.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The interleave of the bitmap.
    public int getBitplaneStride() {
        return bitplaneStride;
    }

    /// Replaces the color model used for conversions from/to chunky pixels.
    ///
    /// Pre condition: The new color model must correspond with the depth of the bitmap.
    ///
    /// Post condition: Color model changed.
    ///
    /// Obligation: -
    ///
    /// @param colorModel The new color model.
    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    /// Returns the current color model of the planar image in this bitmap.
    ///
    /// Pre condition: -
    ///
    /// Post condition: -
    ///
    /// Obligation: -
    ///
    /// @return The color model.
    public ColorModel getColorModel() {
        return colorModel;
    }

    /// Gives you direct access to the bitmap data array.
    ///
    /// Pre condition: -.
    ///
    /// Post condition: -
    ///
    /// Obligation: The bitmap data array remains property
    /// of the AmigaBitmapImageNew and will be used at the next
    /// conversion to chunky. You can access it as you
    /// like (even during conversion) since this class
    /// does never change the contents of the bitmap.
    ///
    /// @return A reference to the bitmap data.
    public byte[] getBitmap() {
        return bitmap;
    }


    /// Creates a deep clone.
    ///
    /// Pre condition: -
    ///
    /// Post condition: Clone created.
    ///
    /// @return A clone.
    @Override
    public AmigaBitmapImage clone() {
        try {
            AmigaBitmapImage theClone = (AmigaBitmapImage) super.clone();
            theClone.bitmap = bitmap.clone();
            return theClone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /// Returns true if the bitmap is interleaved.
    public boolean isInterleaved() {
        return bitplaneStride != scanlineStride;
    }

    /// Returns true if the bitmap is contiguous.
    public boolean isContiguous() {
        return bitplaneStride >= scanlineStride;
    }

    @Override
    public String toString() {
        return "AmigaBitmapImage{" +
                "width=" + width +
                ", height=" + height +
                ", depth=" + depth +
                ", interleaved=" + isInterleaved() +
                ", colorModel=" + colorModel +
                '}';
    }
}
