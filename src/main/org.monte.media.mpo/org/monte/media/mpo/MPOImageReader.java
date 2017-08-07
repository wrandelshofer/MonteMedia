/* @(#)MPOImageReader.java
 * Copyright © 2009-2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.mpo;

import org.monte.media.io.SubImageInputStream;
import org.monte.media.exif.DefaultIIOMetadata;
import org.monte.media.exif.EXIFReader;
import org.monte.media.exif.EXIFTagSet;
import org.monte.media.exif.MPEntryTagSet;
import org.monte.media.exif.MPFTagSet;
import org.monte.media.tiff.TIFFDirectory;
import org.monte.media.tiff.TIFFField;
import org.monte.media.tiff.TIFFNode;
import org.monte.media.tiff.TIFFTag;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.jpeg.CMYKJPEGImageReader;
import org.monte.media.tiff.BaselineTagSet;

/**
 * Reads an image in the MultiPicture Object format (MPO).
 * <p>
 * See: <a href="http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf">MPO Format Specification</a>.
 *
 * @author Werner Randelshofer
 * @version 1.1 2011-02-01 Improves performance of method getImageMetadata.
 * <br>1.0 2009-12-14 Created.
 */
public class MPOImageReader extends ImageReader {

    private static DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0x0);
    /** Number of images. -1 if not known. */
    private int numImages = -1;
    /** Image offsets. null if not known. */
    private long[] imageOffsets;
    /** Image lengths. null if not known. */
    private long[] imageLengths;
    /** Thumbnail offsets. null if not known. */
    private long[] thumbOffsets;
    /** Thumbnail lengths. null if not known. */
    private long[] thumbLengths;
    /** Width of the images. */
    private int[] width = null;
    /** Height of the images. */
    private int[] height = null;
    /** Metadata of all images. */
    private IIOMetadata[] imageMetadata;
    private EXIFReader er;

    public MPOImageReader(MPOImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        if (allowSearch && numImages == -1) {
            readHeader();
        }
        return numImages;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readHeader();
        return width[imageIndex];
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readHeader();
        return height[imageIndex];
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readHeader();
        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(RGB, RGB.createCompatibleSampleModel(width[imageIndex], height[imageIndex])));
        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        readHeader();
        if (imageIndex < 0 || imageIndex >= imageOffsets.length) {
            throw new IndexOutOfBoundsException("illegal imageIndex="+imageIndex);
        }
        return imageMetadata[imageIndex];
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        readHeader();
        if (imageIndex < 0 || imageIndex >= imageOffsets.length) {
            throw new IndexOutOfBoundsException("illegal imageIndex="+imageIndex);
        }
        ImageInputStream in = (ImageInputStream) getInput();
        SubImageInputStream sin = new SubImageInputStream(in, imageOffsets[imageIndex], imageLengths[imageIndex]);
        sin.seek(0);

        ImageReader ir = new CMYKJPEGImageReader(getOriginatingProvider());
        ir.setInput(sin);
        BufferedImage img = ir.read(0);
        ir.dispose();
        return img;
    }

    @Override
    public int getNumThumbnails(int imageIndex) throws IOException {
        readHeader();

        return super.getNumThumbnails(imageIndex);
    }

    /** Reads the header.
     * Does nothing if the header has already been loaded.
     */
    private void readHeader() throws IOException {
        if (numImages == -1) {
            ImageInputStream in = (ImageInputStream) getInput();
            in.seek(0);
            er = new EXIFReader(in);
            er.setFirstImageOnly(false);
            er.read();

            // Get some information that is easy to obtain through a map
            {
                HashMap<TIFFTag, TIFFField> m = er.getMetaDataMap();
                TIFFField mde;
                if ((mde = m.get(MPFTagSet.get(MPFTagSet.TAG_NumberOfImages))) != null) {
                    numImages = ((Number) mde.getData()).intValue();
                } else {
                    numImages = 1;
                }
                width = new int[numImages];
                height = new int[numImages];

                /*
                 if ((mde = m.get(EXIFTagSet.PixelXDimension)) != null) {
                 width = ((Number) mde.getData()).intValue();
                 }
                 if ((mde = m.get(EXIFTagSet.PixelYDimension)) != null) {
                 height = ((Number) mde.getData()).intValue();
                 }*/
                TIFFNode metaDataTree = er.getMetaDataTree();
                for (int i = 0, n = metaDataTree.getChildCount(); i < n; i++) {
                    TIFFNode imageNode = metaDataTree.getChildAt(i);
                    for (Iterator<TIFFNode> e = imageNode.preorderIterator(); e.hasNext();) {
                        TIFFNode node = e.next();
                        if (node instanceof TIFFDirectory) {
                            TIFFDirectory dir = (TIFFDirectory) node;
                            if ((mde = dir.getField(BaselineTagSet.ImageWidth)) != null) {
                                width[i] = ((Number) mde.getData()).intValue();
                            }
                            if ((mde = dir.getField(BaselineTagSet.ImageHeight)) != null) {
                                height[i] = ((Number) mde.getData()).intValue();
                            }
                            if ((mde = dir.getField(EXIFTagSet.PixelXDimension)) != null) {
                                width[i] = ((Number) mde.getData()).intValue();
                            }
                            if ((mde = dir.getField(EXIFTagSet.PixelYDimension)) != null) {
                                height[i] = ((Number) mde.getData()).intValue();
                            }
                        }
                    }
                }
            }

            imageOffsets = new long[numImages];
            imageLengths = new long[numImages];
            if (numImages == 1) {
                imageOffsets[0] = 0;
                imageLengths[0] = in.length();
            }

            // Get now at the tough part
            int index = 0;
            for (Iterator<TIFFNode> e = er.getMetaDataTree().preorderIterator(); e.hasNext();) {
                TIFFNode n = e.next();
                if (n instanceof TIFFDirectory) {
                    TIFFDirectory dir = (TIFFDirectory) n;
                    if (dir.getName() != null && dir.getName().equals("MPEntry")) {
                        long dirOffset = dir.getFileSegments().get(0).getOffset();
                        TIFFField offsetField = dir.getField(MPEntryTagSet.IndividualImageDataOffset);
                        TIFFField lengthField = dir.getField(MPEntryTagSet.IndividualImageSize);
                        if (offsetField != null && lengthField != null) {
                            long dataOffset = (Long) offsetField.getData();
                            imageOffsets[index] = dataOffset == 0 ? 0 : dirOffset + dataOffset;
                            imageLengths[index] = (Long) lengthField.getData();
                            index++;
                        }
                    }
                }
            }

            // Store metadata for later access
            String formatName = "com_sun_media_imageio_plugins_tiff_image_1.0";
            imageMetadata = new IIOMetadata[numImages];
            for (int i = 0; i < numImages; i++) {
                imageMetadata[i] = new DefaultIIOMetadata(formatName, er.getIIOMetadataTree(formatName, i));
            }

            in.seek(0);
        }
    }

    public TIFFNode getExifMetadata() throws IOException {
        readHeader();
        return er.getMetaDataTree();
    }

    public EXIFReader getExifReader() throws IOException {
        readHeader();
        return er;
    }
}
