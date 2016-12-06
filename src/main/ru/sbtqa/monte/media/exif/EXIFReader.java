/* @(#)EXIFReader.java
 * Copyright © 2009-2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.exif;

import org.monte.media.io.ImageInputStreamAdapter;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.jpeg.JFIFInputStream;
import org.monte.media.jpeg.JFIFInputStream.Segment;
import org.monte.media.riff.RIFFChunk;
import org.monte.media.riff.RIFFParser;
import org.monte.media.riff.RIFFVisitor;
import org.monte.media.tiff.FileSegment;
import org.monte.media.tiff.BaselineTagSet;
import org.monte.media.tiff.IFDDataType;
import org.monte.media.tiff.IFD;
import org.monte.media.tiff.IFDEntry;
import org.monte.media.math.Rational;
import org.monte.media.tiff.TIFFDirectory;
import org.monte.media.tiff.TIFFField;
import org.monte.media.tiff.TIFFInputStream;
import org.monte.media.tiff.TIFFNode;
import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;
import org.monte.media.AbortException;
import org.monte.media.ParseException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * Reads EXIF and MP meta data from a JPEG, MPO or AVI file.  Creates a tree
 * structure of {@code DefaultMutableTreeNode}s. Nodes with a String user object
 * describe the hierarchy of the meta data. Nodes with an MetaDataEntry as user
 * object hold the actual meta data.  Sources:  Exchangeable image file
 * format for digital still cameras: EXIF Version 2.2. (April, 2002). Standard
 * of Japan Electronics and Information Technology Industries Association. JEITA
 * CP-3451. <a
 * href="http://www.exif.org/Exif2-2.PDF">http://www.exif.org/Exif2-2.PDF</a>
 *  Multi-Picture Format (February 4, 2009). Standard of the Camera & Imaging
 * Products Association. CIPA DC-007-Translation-2009. <a
 * href="http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf">
 * http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf</a>
 *
 * @author Werner Randelshofer
 * @version $Id: EXIFReader.java 364 2016-11-09 19:54:25Z werner $
 */
public class EXIFReader {

    private File file;
    private ImageInputStream iin;
    /**
     * When this is set to true, the reader stops after heaving read the
     * metadata of the first image.
     */
    private boolean firstImageOnly;
    /**
     * Whether data from the file container shall be added to the Exif data. For
     * most file types, this adds the width and height of the image to the Exif.
     */
    private boolean includeContainerMetadata = true;
    /**
     * Meta data tree.
     */
    private TIFFNode root;
    /**
     * Contains offsets to additional images.
     */
    private TreeSet<Long> imageOffsets = new TreeSet<Long>();

    public EXIFReader(File f) {
        this.file = f;
    }

    public EXIFReader(ImageInputStream iin) {
        this.iin = iin;
    }

    public void setFirstImageOnly(boolean b) {
        firstImageOnly = b;
    }

    public boolean isFirstImageOnly() {
        return firstImageOnly;
    }

    public void setIncludeContainerMetadata(boolean b) {
        includeContainerMetadata = b;
    }

    public boolean isIncludeContainerMetadata() {
        return includeContainerMetadata;
    }

    /**
     * Reads the meta data from the file or input stream that has been set on
     * the constructor.
     */
    public void read() throws IOException {
        if (file != null) {
            iin = new FileImageInputStream(file);
        }
        try {
            iin.seek(0);
            // Determine file type
            int magic = iin.readInt();
            iin.seek(0);
            if (magic == 0x49492a00) {
                // Little-Endian TIFF File
                // XXX - Implement Little-Endian TIFF File support
            } else if (magic == 0x4d4d002a) {
                // Big-Endian TIFF File
                // XXX - Implement Big-Endian TIFF File support
            } else if (magic == 0x52494646) {
                // Little-Endian RIFF File
                readRIFF(iin);
            } else {
                // JFIF File
                readJFIF(iin);
            }
        } finally {
            if (file != null) {
                iin.close();
            }
        }
    }

    /**
     * Reads the metadata from a JFIF file.
     */
    private void readJFIF(ImageInputStream iin) throws IOException {
        root = new TIFFDirectory(null, null, -1);

        ByteArrayOutputStream exifStream = null;
        ArrayList<FileSegment> exifSeg = null;

        ByteArrayOutputStream mpStream = null;
        ArrayList<FileSegment> mpSeg = null;

        byte[] buf = new byte[512];
        JFIFInputStream in = new JFIFInputStream(new BufferedInputStream(new ImageInputStreamAdapter(iin)));

        int imageCount = 0;
        TIFFDirectory imageNode = null;


        // Collect APP2_MARKER data segments with Exif content
        Extraction:
        for (Segment seg = in.getNextSegment(); seg != null; seg = in.getNextSegment()) {
            switch (seg.marker) {
                case JFIFInputStream.SOF0_MARKER:
                case JFIFInputStream.SOF1_MARKER:
                case JFIFInputStream.SOF2_MARKER:
                case JFIFInputStream.SOF3_MARKER:
                //case JFIFInputStream.SOF4_MARKER:
                case JFIFInputStream.SOF5_MARKER:
                case JFIFInputStream.SOF6_MARKER:
                case JFIFInputStream.SOF7_MARKER:
                //case JFIFInputStream.SOF8_MARKER:
                case JFIFInputStream.SOF9_MARKER:
                case JFIFInputStream.SOFA_MARKER:
                case JFIFInputStream.SOFB_MARKER:
                //case JFIFInputStream.SOFC_MARKER:
                case JFIFInputStream.SOFD_MARKER:
                case JFIFInputStream.SOFE_MARKER:
                case JFIFInputStream.SOFF_MARKER:
                    /*
                     * typedef struct {
                     * ubyte   samplePrecision;
                     * ushort  numberOfLines;
                     * ushort  numberOfSamplesPerLine;
                     * ubyte   numberOfComponentsInFrame;
                     * SOFFrameComponent[numberOfComponentsInFrame] frameComponent;
                     * } SOF0;
                     */
                    if (includeContainerMetadata && imageNode != null) {
                        short samplePrecision = (short)(in.read() & 0xff);
                        int numberOfLines = ((in.read() & 0xff) << 8) | (in.read());
                        int samplesPerLine = ((in.read() & 0xff) << 8) | (in.read());
                        int numberOfComponents = in.read() & 0xff;
                        TIFFDirectory dir = new TIFFDirectory(BaselineTagSet.getInstance(), null, 0);
                        imageNode.add(dir);
                        dir.add(new TIFFField(BaselineTagSet.BitsPerSample, samplePrecision, IFDDataType.BYTE));
                        dir.add(new TIFFField(BaselineTagSet.ImageWidth, samplesPerLine, IFDDataType.SHORT));
                        dir.add(new TIFFField(BaselineTagSet.ImageHeight, numberOfLines, IFDDataType.SHORT));
                    }
                    break;
                case JFIFInputStream.SOI_MARKER:
                    imageNode = new TIFFDirectory(ImageTagSet.getInstance(), null, imageCount++, 0, in.getStreamPosition(), new FileSegment(seg.offset, seg.length));
                    root.add(imageNode);
                    exifStream = new ByteArrayOutputStream();
                    exifSeg = new ArrayList<FileSegment>();

                    mpStream = new ByteArrayOutputStream();
                    mpSeg = new ArrayList<FileSegment>();

                    break;
                case JFIFInputStream.APP1_MARKER:
                    // Test whether segment starts with Exif identifier.
                    try {
                        in.read(buf, 0, 6);
                        if (!new String(buf, 0, 6, "ASCII").equals("Exif\u0000\u0000")) {
                            // the segment does not start with the double 
                            // zero-terminated string Exif. skip it.
                            continue;
                        }
                    } catch (IOException e) {
                        // the segment does not start with a zero-terminated string.
                        // skip it.
                        continue;
                    }
                    exifSeg.add(new FileSegment(seg.offset + 6, seg.length - 6));
                    for (int count = in.read(buf); count != -1; count = in.read(buf)) {
                        exifStream.write(buf, 0, count);
                    }
                    break;
                case JFIFInputStream.APP2_MARKER:
                    // Test whether segment starts with MPF identifier.
                    try {
                        in.read(buf, 0, 4);
                        if (!new String(buf, 0, 4, "ASCII").equals("MPF\u0000")) {
                            // the segment does not start with the
                            // zero-terminated string MPF. skip it
                            continue;
                        }
                    } catch (IOException e) {
                        // the segment does not start with a zero-terminated string.
                        // skip it.
                        continue;
                    }
                    mpSeg.add(new FileSegment(seg.offset + 4, seg.length - 4));
                    for (int count = in.read(buf); count != -1; count = in.read(buf)) {
                        mpStream.write(buf, 0, count);
                    }
                    break;
                case JFIFInputStream.EOI_MARKER:
                    break;
                case JFIFInputStream.SOS_MARKER:
                    // Extract the Exif data
                    if (exifStream.size() > 0) {
                        TIFFInputStream tin = new TIFFInputStream(new ByteArrayImageInputStream(exifStream.toByteArray()));
                        readTIFFIFD(tin, imageNode, exifSeg);
                        exifStream.reset();
                    }
                    // Extract the MP data
                    if (mpStream.size() > 0) {
                        TIFFInputStream tin = new TIFFInputStream(new ByteArrayImageInputStream(mpStream.toByteArray()));
                        readMPFIFD(tin, imageNode, null, mpSeg);
                        mpStream.reset();
                    }
                    if (firstImageOnly) {
                        break Extraction;
                    } else {
                        long streamPosition = in.getStreamPosition();
                        Long nextImage = imageOffsets.ceiling(streamPosition);
                        if (nextImage == null) {
                            break Extraction;
                        } else {
                            in.skipFully(nextImage - streamPosition);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Reads the Exif metadata from an AVI RIFF file.
     */
    private void readRIFF(ImageInputStream iin) throws IOException {
        root = new TIFFDirectory(null, null, -1);

        RIFFParser parser = new RIFFParser();
        final int hdrl_ID = RIFFParser.stringToID("hdrl");
        final int strl_ID = RIFFParser.stringToID("strl");
        final int strh_ID = RIFFParser.stringToID("strh");
        final int strd_ID = RIFFParser.stringToID("strd");
        final int AVI_ID = RIFFParser.stringToID("AVI ");
        final int AVIF_ID = RIFFParser.stringToID("AVIF");
        final int RIFF_ID = RIFFParser.stringToID("RIFF");
        final int LIST_ID = RIFFParser.stringToID("LIST");
        parser.declareDataChunk(strl_ID, strh_ID);
        parser.declareDataChunk(strl_ID, strd_ID);
        parser.declareGroupChunk(AVI_ID, RIFF_ID);
        parser.declareGroupChunk(hdrl_ID, LIST_ID);
        parser.declareGroupChunk(strl_ID, LIST_ID);
        try {
            parser.parse(new ImageInputStreamAdapter(iin), new RIFFVisitor() {
                private boolean isAVI;
                private int trackCount = 0;
                private TIFFDirectory trackNode;

                @Override
                public void enterGroup(RIFFChunk group) throws ParseException, AbortException {
                    if (group.getType() == AVI_ID) {
                        isAVI = true;
                    }
                }

                @Override
                public void leaveGroup(RIFFChunk group) throws ParseException, AbortException {
                    if (group.getType() == AVI_ID) {
                        isAVI = false;
                    }
                    if (isAVI && group.getType() == hdrl_ID) {
                        throw new AbortException();
                    }
                }

                @Override
                public void visitChunk(RIFFChunk group, RIFFChunk chunk) throws ParseException, AbortException {
                    if (chunk.getID() == strh_ID) {
                        trackCount++;
                    } else if (chunk.getID() == strd_ID) {
                        trackNode = new TIFFDirectory(TrackTagSet.getInstance(), null, trackCount - 1, null, null, new FileSegment(chunk.getScan(), chunk.getSize()));
                        root.add(trackNode);
                        ByteArrayImageInputStream in = new ByteArrayImageInputStream(chunk.getData(), 8, (int) chunk.getSize() - 8, ByteOrder.LITTLE_ENDIAN);
                        try {
                            TIFFInputStream tin = new TIFFInputStream(in, ByteOrder.LITTLE_ENDIAN, 0);
                            ArrayList<FileSegment> tiffSeg = new ArrayList<FileSegment>();
                            tiffSeg.add(new FileSegment(chunk.getScan() + 8, chunk.getSize() - 8));
                            readTIFFIFD(tin, trackNode, tiffSeg);

                            //}
                            //System.out.println("EXIFReader.readRIFF magic:" + RIFFParser.idToString(magic));
                        } catch (IOException ex) {
                            ParseException e = new ParseException("Error parsing AVI strd chunk.");
                            e.initCause(ex);
                            throw e;
                        } finally {
                            in.close();
                        }
                        if (isFirstImageOnly()) {
                            throw new AbortException();
                        }
                    }
                }

                @Override
                public boolean enteringGroup(RIFFChunk group) {
                    return true;
                }
            });
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (AbortException ex) {
            // aborts are explicitly done by the visitor
        }
    }

    /**
     * Reads the Exif metadata from an AVI RIFF file.
     */
    public void readAVIstrdChunk(byte[] data) throws IOException {
        int track = 0; // track number
        int scan = 0;

        root = new TIFFDirectory(null, null, -1);

        TIFFDirectory trackNode = new TIFFDirectory(TrackTagSet.getInstance(), null, track, null, null, new FileSegment(0, data.length));
        root.add(trackNode);
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, 8, data.length - 8, ByteOrder.LITTLE_ENDIAN);
        TIFFInputStream tin = new TIFFInputStream(in, ByteOrder.LITTLE_ENDIAN, 0);
        ArrayList<FileSegment> tiffSeg = new ArrayList<FileSegment>();
        tiffSeg.add(new FileSegment(scan + 8, data.length - 8));
        readTIFFIFD(tin, trackNode, tiffSeg);
    }

    private void readTIFFIFD(TIFFInputStream tin, TIFFDirectory parent, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = BaselineTagSet.getInstance();
        for (IFD ifd = tin.readIFD(tin.getFirstIFDOffset(), true, true); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
            TIFFDirectory ifdNode = new TIFFDirectory(tagSet, null, count++, ifd, null, tiffSeg);
            parent.add(ifdNode);
            long thumbnailOffset = 0;
            long thumbnailLength = 0;
            int entryCount = 0;
            for (IFDEntry entry : ifd.getEntries()) {
                switch (entry.getTagNumber()) {
                    case BaselineTagSet.TAG_EXIF:
                        readExifIFD(tin, entry.getValueOffset(), ifdNode, entry, tiffSeg);
                        break;
                    case BaselineTagSet.TAG_GPS:
                        readGPSIFD(tin, entry.getValueOffset(), ifdNode, entry, tiffSeg);
                        break;
                    case BaselineTagSet.TAG_Interoperability:
                        readInteropIFD(tin, entry.getValueOffset(), ifdNode, entry, tiffSeg);
                        break;
                    case BaselineTagSet.TAG_JPEGInterchangeFormat:
                        thumbnailOffset = entry.getValueOffset();
                        ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                        break;
                    case BaselineTagSet.TAG_JPEGInterchangeFormatLength:
                        thumbnailLength = entry.getValueOffset();
                        ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                        break;
                    default:
                        ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                        break;
                }
                entryCount++;
            }

            // Hack the thumbnail image in, if one is present
            if (thumbnailOffset > 0 && thumbnailLength > 0) {
                byte[] buf = new byte[(int) thumbnailLength];
                tin.read(thumbnailOffset, buf, 0, (int) thumbnailLength);
                IFDEntry entry = new IFDEntry(BaselineTagSet.TAG_JPEGThumbnailImage, IFDDataType.UNDEFINED.getTypeNumber(), thumbnailLength, thumbnailOffset, -1);
                ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
            }
        }
    }

    private void readExifIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = EXIFTagSet.getInstance();
        for (IFD ifd = tin.readIFD(offset); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
            TIFFDirectory ifdNode = new TIFFDirectory(tagSet, BaselineTagSet.getInstance().getTag(BaselineTagSet.TAG_EXIF), count++, ifd, parentEntry, tiffSeg);
            parent.add(ifdNode);
            int entryCount = 0;
            for (IFDEntry entry : ifd.getEntries()) {
                if (entry.getTagNumber() == EXIFTagSet.Interoperability.getNumber()) {
                    readInteropIFD(tin, entry.getValueOffset(), ifdNode, entry, tiffSeg);
                } else if (entry.getTagNumber() == EXIFTagSet.MakerNote.getNumber()) {
                    if (readMakerNoteIFD(tin, entry.getValueOffset(), ifdNode, entry, tiffSeg)) {
                        break;
                    } else {
                        // fall through
                    }
                } else {
                    ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                }
                entryCount++;
            }
        }
    }

    private void readGPSIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = GPSTagSet.getInstance();
        for (IFD ifd = tin.readIFD(offset); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
            TIFFDirectory ifdNode = new TIFFDirectory(tagSet, BaselineTagSet.getInstance().getTag(BaselineTagSet.TAG_GPS), count++, ifd, parentEntry, tiffSeg);
            parent.add(ifdNode);
            int entryCount = 0;
            for (IFDEntry entry : ifd.getEntries()) {
                ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                entryCount++;
            }
        }
    }

    private void readInteropIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = InteroperabilityTagSet.getInstance();
        for (IFD ifd = tin.readIFD(offset); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
            TIFFDirectory ifdNode = new TIFFDirectory(tagSet, BaselineTagSet.getInstance().getTag(BaselineTagSet.TAG_Interoperability), count++, ifd, parentEntry, tiffSeg);
            parent.add(ifdNode);
            int entryCount = 0;
            for (IFDEntry entry : ifd.getEntries()) {
                ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                entryCount++;
            }
        }
    }

    private void readMPFIFD(TIFFInputStream tin, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = MPFTagSet.getInstance();
        for (IFD ifd = tin.readIFD(tin.getFirstIFDOffset()); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
            TIFFDirectory ifdNode = new TIFFDirectory(tagSet, null, count++, ifd, parentEntry, tiffSeg);
            parent.add(ifdNode);
            int entryCount = 0;
            for (IFDEntry entry : ifd.getEntries()) {
                switch (entry.getTagNumber()) {
                    case MPFTagSet.TAG_MPEntryInformation:
                        readMPEntries(tin, entry, ifdNode, tiffSeg);
                        break;
                    default:
                        ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                        entryCount++;
                        break;
                }
            }
        }
    }

    /**
     * imageCount*16 byte MP Entry Information.
     */
    private void readMPEntries(TIFFInputStream tin, IFDEntry mpEntryInformation, TIFFDirectory parent, ArrayList<FileSegment> tiffSeg) throws IOException {
        byte[] buf = (byte[]) mpEntryInformation.readData(tin);
        TagSet tagSet = MPEntryTagSet.getInstance();
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(buf);
        ByteOrder bo = tin.getByteOrder();
        in.setByteOrder(bo);
        int numImages = (int) mpEntryInformation.getLength() / 16;
        try {
            for (int imageCount = 0; imageCount < numImages; imageCount++) {
                TIFFDirectory ifdNode = new TIFFDirectory(tagSet, tagSet.getTag(MPFTagSet.TAG_MPEntryInformation), imageCount, mpEntryInformation.getValueOffset(), 16 * imageCount, tiffSeg);

                parent.add(ifdNode);

                int imageAttr = in.readInt();
                short dpif = (short) (imageAttr >>> 31);
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_DependentParentImageFlag), dpif, IFDDataType.BYTE));

                short dcif = (short) ((imageAttr >>> 30) & 1);
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_DependentChildImageFlag), dcif, IFDDataType.BYTE));

                short rif = (short) ((imageAttr >>> 29) & 1);
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_RepresentativeImageFlag), rif, IFDDataType.BYTE));

                short idf = (short) ((imageAttr >>> 24) & 7);
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_ImageDataFormat), idf, IFDDataType.BYTE));

                long mptc = (imageAttr & 0xffffffL);
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_MPTypeCode), mptc, IFDDataType.LONG));

                // Read the individual image size
                long imageSize = in.readInt() & 0xffffffffL;
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_IndividualImageSize), imageSize, IFDDataType.LONG));

                // Read the individual data offset
                long imageOffset = in.readInt() & 0xffffffffL;
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_IndividualImageDataOffset), imageOffset, IFDDataType.LONG));
                imageOffsets.add(imageOffset);
                // Read the dependent image 1 entry number
                int dependentImageEntryNumber = in.readUnsignedShort();
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_DependentImage1EntryNumber), dependentImageEntryNumber, IFDDataType.SHORT));

                // Read the dependent image 2 entry number
                dependentImageEntryNumber = in.readUnsignedShort();
                ifdNode.add(new TIFFField(tagSet.getTag(MPEntryTagSet.TAG_DependentImage2EntryNumber), dependentImageEntryNumber, IFDDataType.SHORT));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
    }

    private boolean readMakerNoteIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        // Test whether segment starts with FUJIFILM magic.
        try {
            String magic = tin.readASCII(offset, 10);
            if (magic.equals("FUJIFILM\u000c")) {
                return readFujifilmMakerNoteIFD(tin, offset, parent, parentEntry, tiffSeg);
            } else if (magic.equals("SONY DSC ")) {
                return readSonyMakerNoteIFD(tin, offset, parent, parentEntry, tiffSeg);
            }
        } catch (IOException e) {
            // the segment does not start with a magic. Return false.
            return false;
        }
        return false;
    }

    private boolean readFujifilmMakerNoteIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = FujifilmMakerNoteTagSet.getInstance();
        try {
            for (IFD ifd = tin.readIFD(offset + 12); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
                TIFFDirectory ifdNode = new TIFFDirectory(tagSet, EXIFTagSet.MakerNote, count++, ifd, parentEntry, tiffSeg);
                parent.add(ifdNode);
                int entryCount = 0;
                for (IFDEntry entry : ifd.getEntries()) {
                    // Note: FujifilmMakerNode Data pointers are offset by IFD offset
                    entry.setIFDOffset(offset);

                    ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                    entryCount++;
                }
            }
        } catch (IOException e) {
            // the IFD is incomplete or otherwise damaged
            return false;
        }
        return true;
    }

    private boolean readSonyMakerNoteIFD(TIFFInputStream tin, long offset, TIFFDirectory parent, IFDEntry parentEntry, ArrayList<FileSegment> tiffSeg) throws IOException {
        int count = 0;
        TagSet tagSet = SonyMakerNoteTagSet.getInstance();
        try {
            for (IFD ifd = tin.readIFD(offset + 12); ifd != null; ifd = tin.readIFD(ifd.getNextOffset())) {
                TIFFDirectory ifdNode = new TIFFDirectory(tagSet, EXIFTagSet.MakerNote, count++, ifd, parentEntry, tiffSeg);
                parent.add(ifdNode);
                int entryCount = 0;
                for (IFDEntry entry : ifd.getEntries()) {
                    ifdNode.add(new TIFFField(tagSet.getTag(entry.getTagNumber()), entry.readData(tin), entry));
                    entryCount++;
                }
            }
        } catch (IOException e) {
            // the IFD is incomplete or otherwise damaged
            return false;
        }
        return true;
    }

    /**
     * Gets the meta data as a Swing TreeNode structure.
     */
    public TIFFNode getMetaDataTree() {
        return root;
    }

    /**
     * Returns the number of images that are described with EXIF. Returns -1 if
     * not known.
     */
    public int getImageCount() {
        return root == null ? -1 : root.getChildCount();
    }

    /**
     * Returns all IFDDirectories of the specified tag set for the given image.
     */
    public ArrayList<TIFFDirectory> getDirectories(int image, TagSet tagSet) {
        ArrayList<TIFFDirectory> dirs = new ArrayList<TIFFDirectory>();
        Stack<TIFFDirectory> stack = new Stack<TIFFDirectory>();
        stack.push((TIFFDirectory) getMetaDataTree().getChildAt(image));
        while (!stack.isEmpty()) {
            TIFFDirectory dir = stack.pop();
            for (TIFFNode node : dir.getChildren()) {
                if (node instanceof TIFFDirectory) {
                    TIFFDirectory dirNode = (TIFFDirectory) node;
                    if (dirNode.getTagSet() == tagSet) {
                        dirs.add(0, dirNode); // must insert first because we traverse in post-order
                    } else {
                        stack.push(dirNode);
                    }

                }
            }
        }
        return dirs;
    }

    /**
     * Returns all thumbnails.
     */
    public ArrayList<BufferedImage> getThumbnails(boolean suppressException) throws IOException {
        ArrayList<BufferedImage> thumbnails = new ArrayList<BufferedImage>();
        Stack<TIFFDirectory> stack = new Stack<TIFFDirectory>();
        stack.push((TIFFDirectory) getMetaDataTree());
        if (stack.peek() == null) {
            return thumbnails;
        }
        while (!stack.isEmpty()) {
            TIFFDirectory dir = stack.pop();
            for (TIFFNode node : dir.getChildren()) {
                if (node instanceof TIFFDirectory) {

                    stack.push((TIFFDirectory) node);
                } else if (node instanceof TIFFField) {
                    TIFFField field = (TIFFField) node;
                    if (field.getTag() == BaselineTagSet.JPEGThumbnailImage) {
                        try {
                            thumbnails.add(0, ImageIO.read(new ByteArrayImageInputStream((byte[]) field.getData())));
                            // must insert first because we traverse in post-order
                        } catch (IOException e) {
                            if (!suppressException) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
        return thumbnails;
    }

    /**
     * Returns a flat hash map of the metadata.
     */
    public HashMap<TIFFTag, TIFFField> getMetaDataMap() {
        HashMap<TIFFTag, TIFFField> m = new HashMap<TIFFTag, TIFFField>();

        for (Iterator<TIFFNode> i = root.preorderIterator(); i.hasNext();) {
            TIFFNode node = i.next();

            if (node instanceof TIFFField) {
                m.put(node.getTag(), (TIFFField) node);
            }
        }

        return m;
    }

    /**
     * Gets the metadata as an ImageIO structure.  Format description
     * replicated from <a
     * href="http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/package-summary.html"
     * >http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/package-summary.html</a>:
     *  The DTD for the native image metadata format is as follows:
     * 
     * The DTD for the native image metadata format is as follows:
     * &lt;!DOCTYPE "com_sun_media_imageio_plugins_tiff_image_1.0" [
     *
     *  &lt;!ELEMENT "com_sun_media_imageio_plugins_tiff_image_1.0" (TIFFIFD)*&gt;
     *
     *    &lt;!ELEMENT "TIFFIFD" (TIFFField | TIFFIFD)*&gt;
     *      &lt;!-- An IFD (directory) containing fields --&gt;
     *      &lt;!ATTLIST "TIFFIFD" "tagSets" #CDATA #REQUIRED&gt;
     *        &lt;!-- Data type: String --&gt;
     *      &lt;!ATTLIST "TIFFIFD" "parentTagNumber" #CDATA #IMPLIED&gt;
     *        &lt;!-- The tag number of the field pointing to this IFD --&gt;
     *        &lt;!-- Data type: Integer --&gt;
     *      &lt;!ATTLIST "TIFFIFD" "parentTagName" #CDATA #IMPLIED&gt;
     *        &lt;!-- A mnemonic name for the field pointing to this IFD, if known
     *             --&gt;
     *        &lt;!-- Data type: String --&gt;
     *
     *      &lt;!ELEMENT "TIFFField" (TIFFBytes | TIFFAsciis |
     *        TIFFShorts | TIFFSShorts | TIFFLongs | TIFFSLongs |
     *        TIFFRationals | TIFFSRationals |
     *        TIFFFloats | TIFFDoubles | TIFFUndefined)&gt;
     *        &lt;!-- A field containing data --&gt;
     *        &lt;!ATTLIST "TIFFField" "number" #CDATA #REQUIRED&gt;
     *          &lt;!-- The tag number asociated with the field --&gt;
     *          &lt;!-- Data type: String --&gt;
     *        &lt;!ATTLIST "TIFFField" "name" #CDATA #IMPLIED&gt;
     *          &lt;!-- A mnemonic name associated with the field, if known --&gt;
     *          &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFBytes" (TIFFByte)*&gt;
     *          &lt;!-- A sequence of TIFFByte nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFByte" EMPTY&gt;
     *            &lt;!-- An integral value between 0 and 255 --&gt;
     *            &lt;!ATTLIST "TIFFByte" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *            &lt;!ATTLIST "TIFFByte" "description" #CDATA #IMPLIED&gt;
     *              &lt;!-- A description, if available --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFAsciis" (TIFFAscii)*&gt;
     *          &lt;!-- A sequence of TIFFAscii nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFAscii" EMPTY&gt;
     *            &lt;!-- A String value --&gt;
     *            &lt;!ATTLIST "TIFFAscii" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFShorts" (TIFFShort)*&gt;
     *          &lt;!-- A sequence of TIFFShort nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFShort" EMPTY&gt;
     *            &lt;!-- An integral value between 0 and 65535 --&gt;
     *            &lt;!ATTLIST "TIFFShort" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *            &lt;!ATTLIST "TIFFShort" "description" #CDATA #IMPLIED&gt;
     *              &lt;!-- A description, if available --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFSShorts" (TIFFSShort)*&gt;
     *          &lt;!-- A sequence of TIFFSShort nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFSShort" EMPTY&gt;
     *            &lt;!-- An integral value between -32768 and 32767 --&gt;
     *            &lt;!ATTLIST "TIFFSShort" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *            &lt;!ATTLIST "TIFFSShort" "description" #CDATA #IMPLIED&gt;
     *              &lt;!-- A description, if available --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFLongs" (TIFFLong)*&gt;
     *          &lt;!-- A sequence of TIFFLong nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFLong" EMPTY&gt;
     *            &lt;!-- An integral value between 0 and 4294967295 --&gt;
     *            &lt;!ATTLIST "TIFFLong" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *            &lt;!ATTLIST "TIFFLong" "description" #CDATA #IMPLIED&gt;
     *              &lt;!-- A description, if available --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFSLongs" (TIFFSLong)*&gt;
     *          &lt;!-- A sequence of TIFFSLong nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFSLong" EMPTY&gt;
     *            &lt;!-- An integral value between -2147483648 and 2147482647 --&gt;
     *            &lt;!ATTLIST "TIFFSLong" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *            &lt;!ATTLIST "TIFFSLong" "description" #CDATA #IMPLIED&gt;
     *              &lt;!-- A description, if available --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFRationals" (TIFFRational)*&gt;
     *          &lt;!-- A sequence of TIFFRational nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFRational" EMPTY&gt;
     *            &lt;!-- A rational value consisting of an unsigned numerator and
     *                 denominator --&gt;
     *            &lt;!ATTLIST "TIFFRational" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The numerator and denominator, separated by a slash --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFSRationals" (TIFFSRational)*&gt;
     *          &lt;!-- A sequence of TIFFSRational nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFSRational" EMPTY&gt;
     *            &lt;!-- A rational value consisting of a signed numerator and
     *                 denominator --&gt;
     *            &lt;!ATTLIST "TIFFSRational" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The numerator and denominator, separated by a slash --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFFloats" (TIFFFloat)*&gt;
     *          &lt;!-- A sequence of TIFFFloat nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFFloat" EMPTY&gt;
     *            &lt;!-- A single-precision floating-point value --&gt;
     *            &lt;!ATTLIST "TIFFFloat" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFDoubles" (TIFFDouble)*&gt;
     *          &lt;!-- A sequence of TIFFDouble nodes --&gt;
     *
     *          &lt;!ELEMENT "TIFFDouble" EMPTY&gt;
     *            &lt;!-- A double-precision floating-point value --&gt;
     *            &lt;!ATTLIST "TIFFDouble" "value" #CDATA #IMPLIED&gt;
     *              &lt;!-- The value --&gt;
     *              &lt;!-- Data type: String --&gt;
     *
     *        &lt;!ELEMENT "TIFFUndefined" EMPTY&gt;
     *          &lt;!-- Uninterpreted byte data --&gt;
     *          &lt;!ATTLIST "TIFFUndefined" "value" #CDATA #IMPLIED&gt;
     *            &lt;!-- A list of comma-separated byte values --&gt;
     *            &lt;!-- Data type: String --&gt;
     *]&gt;
     * 
     */
    public IIOMetadataNode getIIOMetadataTree(String formatName, int imageIndex) {
        if (formatName != null && !formatName.equals("com_sun_media_imageio_plugins_tiff_image_1.0")) {
            throw new IllegalArgumentException("Unsupported formatName:" + formatName);
        }
        IIOMetadataNode iioRoot = new IIOMetadataNode("com_sun_media_imageio_plugins_tiff_image_1.0");
        TIFFNode imageRoot = root.getChildAt(imageIndex);
        for (TIFFNode node : imageRoot.getChildren()) {
            addIIOMetadataNode(iioRoot, node);
        }
        return iioRoot;
    }

    private void addIIOMetadataNode(IIOMetadataNode iioParent, TIFFNode node) {
        if (node instanceof TIFFDirectory) {
            TIFFDirectory dir = (TIFFDirectory) node;
            IIOMetadataNode iioNode = new IIOMetadataNode("TIFFIFD");
            TagSet tagSet = dir.getTagSet();
            iioNode.setAttribute("tagSets", dir == null ? "" : tagSet.getName());
            if (dir.getTag() != null) {
                iioNode.setAttribute("parentTagNumber", Integer.toString(dir.getTagNumber()));
                iioNode.setAttribute("parentTagName", dir.getTag().getName());
            }
            iioParent.appendChild(iioNode);
            for (int i = 0; i < node.getChildCount(); i++) {
                addIIOMetadataNode(iioNode, node.getChildAt(i));
            }
        } else if (node instanceof TIFFField) {
            TIFFField field = (TIFFField) node;
            IIOMetadataNode iioNode = new IIOMetadataNode("TIFFField");
            iioNode.setAttribute("number", Integer.toString(field.getTagNumber()));
            if (field.getTagName() != null && !field.getTagName().equals("unknown")) {
                iioNode.setAttribute("name", field.getTagName());
            }
            IIOMetadataNode iioSequence = null;
            String description = field.getDescription();
            switch (field.getType()) {
                case ASCII: {
                    iioSequence = new IIOMetadataNode("TIFFAsciis");
                    IIOMetadataNode iioValue = new IIOMetadataNode("TIFFAscii");
                    iioValue.setAttribute("value", (String) field.getData());
                    iioSequence.appendChild(iioValue);
                    break;
                }
                case BYTE: {
                    iioSequence = new IIOMetadataNode("TIFFBytes");
                    short[] value = (field.getData() instanceof Short) ? new short[]{(Short) field.getData()} : (short[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFByte");
                        iioValue.setAttribute("value", Short.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case SBYTE: {
                    iioSequence = new IIOMetadataNode("TIFFSBytes");
                    byte[] value = (field.getData() instanceof Byte) ? new byte[]{(Byte) field.getData()} : (byte[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFSByte");
                        iioValue.setAttribute("value", Byte.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case DOUBLE: {
                    iioSequence = new IIOMetadataNode("TIFFDoubles");
                    double[] value = (field.getData() instanceof Double) ? new double[]{(Double) field.getData()} : (double[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFDouble");
                        iioValue.setAttribute("value", Double.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                    }
                    break;
                }
                case FLOAT: {
                    iioSequence = new IIOMetadataNode("TIFFFloats");
                    float[] value = (field.getData() instanceof Float) ? new float[]{(Float) field.getData()} : (float[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFFloat");
                        iioValue.setAttribute("value", Double.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case LONG: {
                    iioSequence = new IIOMetadataNode("TIFFLongs");
                    long[] value = (field.getData() instanceof Long) ? new long[]{(Long) field.getData()} : (long[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFLong");
                        iioValue.setAttribute("value", Long.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case SLONG: {
                    iioSequence = new IIOMetadataNode("TIFFSLongs");
                    int[] value = (field.getData() instanceof Integer) ? new int[]{(Integer) field.getData()} : (int[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFSLong");
                        iioValue.setAttribute("value", Integer.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case RATIONAL: {
                    iioSequence = new IIOMetadataNode("TIFFRationals");
                    Rational[] value = (field.getData() instanceof Rational) ? new Rational[]{(Rational) field.getData()} : (Rational[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFRational");
                        iioValue.setAttribute("value", Long.toString(value[i].getNumerator()) + "/" + Long.toString(value[i].getDenominator()));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case SRATIONAL: {
                    iioSequence = new IIOMetadataNode("TIFFSRationals");
                    Rational[] value = (field.getData() instanceof Rational) ? new Rational[]{(Rational) field.getData()} : (Rational[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFSRational");
                        iioValue.setAttribute("value", Long.toString(value[i].getNumerator()) + "/" + Long.toString(value[i].getDenominator()));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case SHORT: {
                    iioSequence = new IIOMetadataNode("TIFFShorts");
                    int[] value = (field.getData() instanceof Integer) ? new int[]{(Integer) field.getData()} : (int[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFShort");
                        iioValue.setAttribute("value", Integer.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case SSHORT: {
                    iioSequence = new IIOMetadataNode("TIFFSShorts");
                    short[] value = (field.getData() instanceof Short) ? new short[]{(Short) field.getData()} : (short[]) field.getData();
                    for (int i = 0; i < value.length; i++) {
                        IIOMetadataNode iioValue = new IIOMetadataNode("TIFFSShort");
                        iioValue.setAttribute("value", Short.toString(value[i]));
                        iioSequence.appendChild(iioValue);
                        if (i == 0 && description != null) {
                            iioValue.setAttribute("description", description);
                        }
                    }
                    break;
                }
                case UNDEFINED: {
                    iioSequence = new IIOMetadataNode("TIFFUndefined");
                    byte[] value = (field.getData() instanceof Byte) ? new byte[]{(Byte) field.getData()} : (byte[]) field.getData();
                    StringBuilder iioValue = new StringBuilder();
                    for (int i = 0; i < value.length; i++) {
                        if (i != 0) {
                            iioValue.append(',');
                        }
                        iioValue.append(Integer.toString(value[i] & 0xff));
                    }
                    iioSequence.setAttribute("value", iioValue.toString());
                    if (description != null) {
                        iioSequence.setAttribute("description", description);
                    }
                    break;
                }
            }
            if (iioSequence != null) {
                iioNode.appendChild(iioSequence);
            }
            iioParent.appendChild(iioNode);
        }
    }

    public IIOMetadata getIIOMetadata(int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
