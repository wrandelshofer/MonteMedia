/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// Provides readers and writers for the mp4 file format.
///
/// @author Werner Randelshofer
@SuppressWarnings("module")
module org.monte.media.mp4 {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;
    requires org.monte.media.quicktime;

    exports org.monte.media.mp4;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.mp4.MP4WriterSpi;

}
