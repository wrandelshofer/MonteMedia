/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// Provides readers and writers for the JPEG file format.
///
/// @author Werner Randelshofer
module org.monte.media.jpeg {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.exif;
    exports org.monte.media.jfif;
    exports org.monte.media.jpeg;
    exports org.monte.media.mjpg;
    exports org.monte.media.mpo;
    exports org.monte.media.jpeg.codec.video;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.jpeg.codec.video.JPEGDecoderSpi,
            org.monte.media.jpeg.codec.video.JPEGEncoderSpi;

    provides javax.imageio.spi.ImageReaderSpi with
            org.monte.media.jpeg.CMYKJPEGImageReaderSpi,
            org.monte.media.mpo.MPOImageReaderSpi;

}
