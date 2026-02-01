/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/// Provides readers and writers for the AVI file format.
///
/// @author Werner Randelshofer
module org.monte.media.avi {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.avi;
    exports org.monte.media.avi.codec.audio;
    exports org.monte.media.avi.codec.video;
    exports org.monte.media.riff;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.avi.codec.video.TechSmithDecoderSpi,
            org.monte.media.avi.codec.video.TechSmithEncoderSpi,
            org.monte.media.avi.codec.audio.AVIPCMAudioCodecSpi,
            org.monte.media.avi.codec.video.DIBDecoderSpi,
            org.monte.media.avi.codec.video.DIBEncoderSpi,
            org.monte.media.avi.codec.video.RunLengthDecoderSpi,
            org.monte.media.avi.codec.video.RunLengthEncoderSpi,
            org.monte.media.avi.codec.video.ZMBVDecoderSpi;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.avi.AVIWriterSpi;

    provides org.monte.media.av.MovieReaderSpi with
            org.monte.media.avi.AVIReaderSpi;

}
