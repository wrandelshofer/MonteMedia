/*
 * @(#)FormatKeys.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;

/**
 * Defines common {@code FormatKey}'s.
 *
 * @author Werner Randelshofer
 */
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

    /**
     * The type of the media.
     */
    public final static FormatKey<MediaType> MediaTypeKey = new FormatKey<MediaType>("mediaType", MediaType.class);
    /**
     * The encoding of the media.
     */
    public final static FormatKey<String> EncodingKey = new FormatKey<String>("encoding", String.class);
    /**
     * The format of the samples of the media.
     */
    public final static FormatKey<String> SampleFormatKey = new FormatKey<String>("sampleFormat", String.class);

    //
    public final static String MIME_AVI = "video/avi";
    public final static String MIME_QUICKTIME = "video/quicktime";
    public final static String MIME_MP4 = "video/mp4";
    public final static String MIME_JAVA = "Java";
    public final static String MIME_ANIM = "x-iff/anim";
    public final static String MIME_IMAGE_SEQUENCE = "ImageSequence";
    /**
     * The mime type.
     */
    public final static FormatKey<String> MimeTypeKey = new FormatKey<String>("mimeType", String.class);
    /**
     * The number of frames per second.
     */
    public final static FormatKey<Rational> FrameRateKey = new FormatKey<Rational>("frameRate", Rational.class);

    /**
     * The interval between key frames.
     * If this value is not specified, most codecs will use {@code FrameRateKey}
     * as a hint and try to produce one key frame per second.
     */
    public final static FormatKey<Integer> KeyFrameIntervalKey = new FormatKey<Integer>("keyFrameInterval", Integer.class);
}
