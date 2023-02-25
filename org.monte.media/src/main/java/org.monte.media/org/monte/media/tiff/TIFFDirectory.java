/* @(#)TIFFDirectory.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.util.ArrayList;

/**
 * A convenience class for working with TIFF IFD's.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class TIFFDirectory extends TIFFNode {
    /**
     * The tag set of this directory.
     */
    private TagSet tagSet;
    /**
     * The index of this directory.
     */
    private int index;


    /**
     * The IFD from which this directory was read.
     * ifd is null, if this directory has not been read from a TIFF file.
     */
    private IFD ifd;
    /**
     * Segments of the TIFF file which hold the IFD.
     * tiffSegments is null, if this directory has not been read from a TIFF file.
     */
    private ArrayList<FileSegment> fileSegments;
    /**
     * Offset of the directory in the file segment if ifd is null.
     */
    private long offset;
    /**
     * Length of the directory in the file segment if ifd is null.
     */
    private long length;

    /**
     * Creates a TIFFDirectory identified by the specified TIFFTag.
     */
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index) {
        super(tag);
        this.tagSet = tagSet;
        this.index = index;
    }

    /**
     * Creates a TIFFDirectory identified by the specified TIFFTag and associated
     * with the specified IFD and file segments.
     */
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, IFD ifd, IFDEntry parentEntry, ArrayList<FileSegment> fileSegments) {
        this(tagSet, tag, index);
        this.ifd = ifd;
        this.ifdEntry = parentEntry;
        this.fileSegments = fileSegments;
    }

    /**
     * Creates a TIFFDirectory identified by the specified TIFFTag and associated
     * with the specified IFD and file segments.
     */
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, IFD ifd, IFDEntry parentEntry, FileSegment fileSegment) {
        this(tagSet, tag, index);
        this.ifd = ifd;
        this.ifdEntry = parentEntry;
        this.fileSegments = new ArrayList<FileSegment>();
        fileSegments.add(fileSegment);
    }

    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, long offset, long length, FileSegment fileSegment) {
        this(tagSet, tag, index);
        this.offset = offset;
        this.length = length;
        this.fileSegments = new ArrayList<FileSegment>();
        fileSegments.add(fileSegment);
    }

    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, long offset, long length, ArrayList<FileSegment> fileSegments) {
        this(tagSet, tag, index);
        this.offset = offset;
        this.length = length;
        this.fileSegments = fileSegments;
    }

    /**
     * Returns the IFD from which this directory has been read.
     *
     * @return IFD or null.
     */
    public IFD getIFD() {
        return ifd;
    }

    /**
     * Returns the tag set used by this directory.
     */
    public TagSet getTagSet() {
        return tagSet;
    }

    public String getName() {
        return tagSet == null ? null : tagSet.getName();
    }

    public int getIndex() {
        return index;
    }

    public int getCount() {
        return getChildren().size();
    }

    public long getOffset() {
        return ifd != null ? ifd.getOffset() : offset;
    }

    public long getLength() {
        return ifd != null ? ifd.getLength() : length;
    }

    /**
     * Returns the segments of the TIFF file inside its parent file.
     * In a JPEG JFIF stream, a TIFF file can be segmented over multiple
     * APP markers.
     *
     * @return segment list or null.
     */
    public ArrayList<FileSegment> getFileSegments() {
        return fileSegments;
    }

    /**
     * Returns a TIFFField with the specified tag. If a child node with
     * this tag exists.
     *
     * @param tag
     */
    public TIFFField getField(TIFFTag tag) {
        for (TIFFNode node : getChildren()) {
            if (node instanceof TIFFField) {
                if (node.getTag() == tag) {
                    return (TIFFField) node;
                }
            }
        }
        return null;
    }

    /**
     * Returns the value of the TIFFField with the specified tag. If a child node with
     * this tag exists.
     *
     * @param tag
     */
    public Object getData(TIFFTag tag) {
        TIFFField field = getField(tag);
        return field == null ? null : field.getData();
    }

    @Override
    public String toString() {
        return "TIFFDirectory " + tagSet;
    }

}
