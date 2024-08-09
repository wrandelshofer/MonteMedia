/*
 * @(#)FileSegment.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

/**
 * Holds offset and length of a TIFF file segment.
 * <p>
 * In a JPEG JFIF stream, a TIFF file can be segmented over multiple APP
 * markers.
 *
 * @author Werner Randelshofer
 */
public record FileSegment(long offset, long length) {

}
