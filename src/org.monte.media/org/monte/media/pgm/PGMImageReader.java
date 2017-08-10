/* @(#)PGMImageReader.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.pgm;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

/**
 * Reads an image in the Netpbm grayscale image format (PGM).
 * <p>
 * See: <a href="http://netpbm.sourceforge.net/doc/pgm.html">PGM Format Specification</a>.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class PGMImageReader extends ImageReader {

    /** All images have the same width.*/
    private int width = -1;
    /** All images have the same height.*/
    private int height = -1;
    /** All images have the same depth. Must be in the range [1,65535].*/
    private int maxGray = -1;
    /** Number of images. */
    private int numImages = -1;
    /** Start of image data. */
    private long dataOffset = -1;

    public PGMImageReader(PGMImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        if (allowSearch && numImages == -1) {
            readHeader();

            ImageInputStream in = (ImageInputStream) getInput();
            in.seek(dataOffset);
            int dataSize = width * height * (maxGray > 255 ? 2 : 1);
            numImages = 0;
            while (in.skipBytes(dataSize) == dataSize) {
                numImages++;
            }
        }
        return numImages;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readHeader();
        return width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readHeader();
        return height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readHeader();
        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        ComponentColorModel ccm = new ComponentColorModel(//
                new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY)),
                new int[]{maxGray > 255 ? 16 : 8},//
                false, false, Transparency.OPAQUE,//
                (maxGray > 255) ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_BYTE);
        l.add(new ImageTypeSpecifier(ccm, ccm.createCompatibleSampleModel(width, height)));
        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        readHeader();

        if (imageIndex > 0) {
            throw new ArrayIndexOutOfBoundsException("imageIndex is " + imageIndex + " must be 0.");
        }

        ImageInputStream in = (ImageInputStream) getInput();
        in.seek(dataOffset + imageIndex * width * height * (maxGray > 255 ? 2 : 1));

        ComponentColorModel ccm = new ComponentColorModel(//
                new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY)),
                new int[]{maxGray > 255 ? 16 : 8},//
                false, false, Transparency.OPAQUE,//
                (maxGray > 255) ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_BYTE);
        SampleModel sm = ccm.createCompatibleSampleModel(width, height);

        BufferedImage img;
        if (maxGray > 255) {
            DataBufferShort db = new DataBufferShort(width * height);
            in.readFully(db.getData(), 0, width * height);
            img = new BufferedImage(ccm, Raster.createWritableRaster(sm, db, new Point(0, 0)), false, new Hashtable<Object,Object>());
        } else {
            DataBufferByte db = new DataBufferByte(width * height);
            in.readFully(db.getData(), 0, width * height);
            img = new BufferedImage(ccm, Raster.createWritableRaster(sm, db, new Point(0, 0)), false, new Hashtable<Object,Object>());
        }

        return img;
    }

    /** Reads the PGM header.
     * Does nothing if the header has already been loaded.
     */
    private void readHeader() throws IOException {
        if (dataOffset == -1) {

            ImageInputStream in = (ImageInputStream) getInput();
            in.seek(0);

            // Check if file starts with "P5"
            if (in.readShort() != 0x5035) {
                in.reset();
                throw new IOException("Illegal magic number");
            }
            // Skip whitespace (blank, TAB, CR or LF)
            int b = in.readUnsignedByte();
            if (b != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
                throw new IOException("Whitespace missing after magic number");
            }
            // Read width
            width = readHeaderValue(in, "image width");
            if (width < 1) {
                throw new IOException("Illegal image width " + width);
            }
            height = readHeaderValue(in, "image height");
            if (height < 1) {
                throw new IOException("Illegal image width " + height);
            }
            maxGray = readHeaderValue(in, "maximum gray value");
            if (maxGray < 2 || maxGray > 65536) {
                throw new IOException("Illegal maximum gray value " + maxGray);
            }
            dataOffset = in.getStreamPosition();
        }
    }

    private int readHeaderValue(ImageInputStream in, String name) throws IOException {
        // Skip whitespace (blank, TAB, CR or LF) and comments
        int b;
        do {
            b = in.readUnsignedByte();

            if (b == '#') { // comments
                do {
                    b = in.readUnsignedByte();
                } while (b != 0x0d && b != 0x0a);
            }
        } while (b == 0x20 || b == 0x09 || b == 0x0d || b == 0x0a);

        // read value
        if (b < 0x30 || b > 0x39) {
            throw new IOException(name + " missing");
        }
        int value = 0;
        do {
            if (value >= 100000) {
                throw new IOException(name + " is too large");
            }
            value = value * 10 + b - 0x30;
        } while ((b = in.readUnsignedByte()) >= 0x30 && b <= 0x39);
        if (b != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
            throw new IOException("Whitespace after " + name + " missing");
        }
        return value;
    }
}
