/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// A library for processing still images, video, audio and meta-data.
///
/// @author Werner Randelshofer
module org.monte.media {
    requires java.desktop;
    requires java.prefs;

    exports org.monte.media.beans;
    exports org.monte.media.av;
    exports org.monte.media.av.codec.audio;
    exports org.monte.media.av.codec.time;
    exports org.monte.media.av.codec.video;
    exports org.monte.media.exception;
    exports org.monte.media.image;
    exports org.monte.media.interpolator;
    exports org.monte.media.io;
    exports org.monte.media.math;
    exports org.monte.media.pgm;
    exports org.monte.media.tree;
    exports org.monte.media.util;
    exports org.monte.media.util.stream;
    exports org.monte.media.av.codec.text;
    exports org.monte.media.image.op;
    exports org.monte.media.image.algo;
    exports org.monte.media.image.colormodel;
    exports org.monte.media.quicktime.codec.sprite;

    uses org.monte.media.av.CodecSpi;
    uses org.monte.media.av.MovieWriterSpi;
    uses org.monte.media.av.MovieReaderSpi;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.av.codec.video.PNGDecoderSpi,
            org.monte.media.av.codec.video.PNGEncoderSpi;

    provides javax.imageio.spi.ImageReaderSpi with
            org.monte.media.pgm.PGMImageReaderSpi;
}
