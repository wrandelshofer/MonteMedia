/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// Provides readers and writers for the mp3 file format.
///
/// @author Werner Randelshofer
@SuppressWarnings("module")
module org.monte.media.zipmovie {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;
    requires org.monte.media.jpeg;

    exports org.monte.media.imgseq;
    exports org.monte.media.zipmovie;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.zipmovie.ZipMovieWriterSpi;

}
