/* @(#)FileSegment.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.tiff;

/**
 * Holds offset and length of a TIFF file segment.
 * <p>
 * In a JPEG JFIF stream, a TIFF file can be segmented over multiple APP
 * markers.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-07-25 Created.
 */
public class FileSegment {

    private long offset;
    private long length;

    public FileSegment(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getOffset() {
        return offset;
    }
}
