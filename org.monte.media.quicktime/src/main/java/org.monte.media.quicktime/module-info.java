/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// Provides readers and writers for the QuickTime file format.
///
/// @author Werner Randelshofer
module org.monte.media.quicktime {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.quicktime;
    exports org.monte.media.quicktime.codec.audio;
    exports org.monte.media.quicktime.codec.sprite;
    exports org.monte.media.qtff;
    exports org.monte.media.qtff.atom;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.quicktime.codec.sprite.SpriteDecoderSpi,
            org.monte.media.quicktime.codec.sprite.SpriteEncoderSpi,
            org.monte.media.quicktime.codec.audio.QuickTimePCMAudioCodecSpi,
            org.monte.media.quicktime.codec.text.AppleClosedCaptionCodecSpi,
            org.monte.media.quicktime.codec.video.AnimationDecoderSpi,
            org.monte.media.quicktime.codec.video.AnimationEncoderSpi,
            org.monte.media.quicktime.codec.video.RawEncoderSpi,
            org.monte.media.quicktime.codec.video.RawDecoderSpi;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.quicktime.QuickTimeWriterSpi;

    provides org.monte.media.av.MovieReaderSpi with
            org.monte.media.quicktime.QuickTimeReaderSpi;
}
