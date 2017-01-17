/* @(#)ILBMImageReader.java
 * Copyright © 2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.pbm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import ru.sbtqa.monte.media.ilbm.*;
import ru.sbtqa.monte.media.io.ImageInputStreamAdapter;

/**
 * Reads an image in the Amiga IFF Interleaved Bitmap image format (ILBM).
 *
 * @author Werner Randelshofer
 * @version $Id: PBMImageReader.java 364 2016-11-09 19:54:25Z werner $
 */
public class PBMImageReader extends ImageReader {

    private ArrayList<ColorCyclingMemoryImageSource> images = null;

    public PBMImageReader(PBMImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        if (allowSearch && images == null) {
            readImages();
        }
        return images.size();
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readImages();
        return images.get(imageIndex).getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readImages();
        return images.get(imageIndex).getHeight();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readImages();
        ColorCyclingMemoryImageSource iip = images.get(imageIndex);

        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(iip.getColorModel(), //
                iip.getColorModel().createCompatibleSampleModel(iip.getWidth(), iip.getHeight())));
        return l.iterator();
    }

    /**
     * Returns the aspect ratio of the given image (that is, its width
     * divided by its height) as a <code>float</code>.  For images
     * that are inherently resizable, this method provides a way to
     * determine the appropriate width given a deired height, or vice
     * versa.  For non-resizable images, the true width and height
     * are used.
     *
     *  The default implementation simply returns
     * <code>(float)getWidth(imageIndex)/getHeight(imageIndex)</code>.
     *
     * @param imageIndex the index of the image to be queried.
     *
     * @return a <code>float</code> indicating the aspect ratio of the
     * given image.
     *
     * @exception IllegalStateException if the input source has not been set.
     * @exception IndexOutOfBoundsException if the supplied index is
     * out of bounds.
     * @exception IOException if an error occurs during reading.
     */
    @Override
    public float getAspectRatio(int imageIndex) throws IOException {
        readImages();
        ColorCyclingMemoryImageSource mis=images.get(imageIndex);
        float ratio = (float) getWidth(imageIndex) / getHeight(imageIndex);
        if (mis.getProperties().containsKey("aspect")) {
            ratio*=(Double)mis.getProperties().get("aspect");
        }
        return ratio;
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
        readImages();

        return images.get(imageIndex).toBufferedImage();
    }

    private void readImages() throws IOException {
        ImageInputStream in = (ImageInputStream) getInput();
        if (images == null) {
            in.seek(0);
            PBMDecoder d = new PBMDecoder(new ImageInputStreamAdapter(in));
            images = d.produce();
        }
    }
}
