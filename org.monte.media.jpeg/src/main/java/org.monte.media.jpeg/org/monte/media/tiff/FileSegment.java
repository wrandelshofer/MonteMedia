/*
 * @(#)FileSegment.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

/// Holds offset and length of a tiff file segment.
///
/// In a JPEG JFIF stream, a tiff file can be segmented over multiple APP
/// markers.
///
/// @param offset offset in number of bytes from the start of the file
/// @param length length in number of bytes
/// @author Werner Randelshofer
public record FileSegment(long offset, long length) {

}
