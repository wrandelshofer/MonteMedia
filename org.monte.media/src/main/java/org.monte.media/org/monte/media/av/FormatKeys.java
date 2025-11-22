/*
 * @(#)FormatKeys.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;

/// Defines common `FormatKey`'s.
///
/// @author Werner Randelshofer
public class FormatKeys {
    public static enum MediaType {
        AUDIO,
        VIDEO,
        MIDI,
        TEXT,
        META,
        SPRITE,
        FILE,
        UNKNOWN
    }

    /// The type of the media.
    public final static FormatKey<MediaType> MediaTypeKey = new FormatKey<>("mediaType", MediaType.class);
    /// The encoding of the media.
    public final static FormatKey<String> EncodingKey = new FormatKey<>("encoding", String.class);
    /// The data class.
    @SuppressWarnings("rawtypes")
    public final static FormatKey<Class> DataClassKey = new FormatKey<>("dataClass", Class.class);

    //
    public final static String MIME_AVI = "video/avi";
    public final static String MIME_QUICKTIME = "video/quicktime";
    public final static String MIME_ZIP = "application/zip";
    public final static String MIME_MP4 = "video/mp4";
    public final static String MIME_JAVA = "Java";
    public final static String MIME_ANIM = "x-iff/anim";
    public final static String MIME_IMAGE_SEQUENCE = "ImageSequence";
    /// The mime type.
    public final static FormatKey<String> MimeTypeKey = new FormatKey<>("mimeType", String.class);
    /// The number of frames per second.
    public final static FormatKey<Rational> FrameRateKey = new FormatKey<>("frameRate", Rational.class);
    /// The media timescale.
    public final static FormatKey<Long> MediaTimeScale = new FormatKey<>("mediaTimescale", Long.class);
    /// The movie timescale.
    public final static FormatKey<Long> MovieTimeScale = new FormatKey<>("movieTimescale", Long.class);

    /// The interval between key frames.
    /// If this value is not specified, most codecs will use `FrameRateKey`
    /// as a hint and try to produce one key frame per second.
    public final static FormatKey<Integer> KeyFrameIntervalKey = new FormatKey<>("keyFrameInterval", Integer.class);
}
