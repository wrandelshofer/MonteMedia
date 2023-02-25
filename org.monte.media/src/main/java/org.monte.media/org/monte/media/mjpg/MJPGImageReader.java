/* @(#)MJPGImageReader.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.mjpg;

import org.monte.media.av.codec.video.AVIBMPDIB;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Reads an image in the Motion JPEG (MJPG) format.
 * <p>
 * . This class can read Motion JPEG files with omitted Huffmann table.
 * <p>
 * For more information see: Microsoft Windows Bitmap Format. Multimedia
 * Technical Note: JPEG DIB Format. (c) 1993 Microsoft Corporation. All rights
 * reserved. <a
 * href="http://www.fileformat.info/format/bmp/spec/b7c72ebab8064da48ae5ed0c053c67a4/BMPDIB.TXT">BMPDIB.txt</a>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MJPGImageReader extends ImageReader {

    private static DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0x0);
    /**
     * When we read the header, we read the whole image.
     */
    private BufferedImage image;
    /**
     * This value is set to true, when we returned the image.
     */
    private boolean didReturnImage;

    public MJPGImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readHeader();
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readHeader();
        return image.getHeight();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readHeader();
        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(RGB, RGB.createCompatibleSampleModel(image.getWidth(), image.getHeight())));
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
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex > 0) {
            throw new IndexOutOfBoundsException();
        }
        readHeader();

        return image;
    }

    /**
     * Reads the image header. Does nothing if the header has already been
     * loaded.
     */
    private void readHeader() throws IOException {
        if (image == null) {

            ImageReader r = getBasicJPEGImageReader();
            Object in = getInput();
            /*if (in instanceof Buffer) {
             Buffer buffer = (Buffer) in;
             in=buffer.getData();
             }*/
            if (in instanceof byte[]) {
                r.setInput(new MemoryCacheImageInputStream(AVIBMPDIB.prependDHTSeg((byte[]) in)));
            } else if (in instanceof ImageInputStream) {
                r.setInput(AVIBMPDIB.prependDHTSeg((ImageInputStream) in));
            } else {
                r.setInput(AVIBMPDIB.prependDHTSeg((InputStream) in));
            }
            didReturnImage = false;
            image = r.read(0);
        }
    }

    /**
     * Gets the JPEG image reader from ImageIO. This method ensures that we do
     * not get a reader from the Monte Media library, because this library does
     * not implement the actual decoding.
     */
    private static ImageReader getBasicJPEGImageReader() {
        for (ImageReader r : (Iterable<ImageReader>) () -> ImageIO.getImageReadersByFormatName("jpeg")) {
            if ("com.sun.imageio.plugins.jpeg.JPEGImageReader".equals(r.getClass().getName())) {
                return r;
            }
        }
        throw new InternalError("could not find native JPEG Reader");
    }

    /**
     * Disposes of resources held internally by the reader.
     */
    @Override
    public void dispose() {
        try {
            if (image != null && !didReturnImage) {
                image.flush();
            }
        } catch (Throwable ex) {
            // we don't care about exceptions here, because we don't need
            // image anymore
        } finally {
            image = null;
        }
    }
}
