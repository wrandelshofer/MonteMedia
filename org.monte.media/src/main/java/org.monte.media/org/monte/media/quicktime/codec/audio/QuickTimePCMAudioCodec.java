/*
 * @(#)QuickTimePCMAudioCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime.codec.audio;

import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.codec.audio.AbstractPCMAudioCodec;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_UNSIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_IN24_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_IN32_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_RAW_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_SOWT_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_QUICKTIME_TWOS_PCM;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;

/**
 * {@code AbstractPCMAudioCodec} performs sign conversion, endian conversion and
 * quantization conversion of PCM audio data.
 * <p>
 * Does not perform sampling rate conversion or channel conversion.
 *
 * @author Werner Randelshofer
 */
public class QuickTimePCMAudioCodec extends AbstractPCMAudioCodec {

    private final static HashSet<String> signedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
                    ENCODING_PCM_SIGNED, ENCODING_QUICKTIME_TWOS_PCM, ENCODING_QUICKTIME_SOWT_PCM,
                    ENCODING_QUICKTIME_IN24_PCM, ENCODING_QUICKTIME_IN32_PCM,}));
    private final static HashSet<String> unsignedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
                    ENCODING_PCM_UNSIGNED, ENCODING_QUICKTIME_RAW_PCM}));

    public QuickTimePCMAudioCodec() {
        super(new Format[]{
                new Format(MediaTypeKey, MediaType.AUDIO,//
                        MimeTypeKey, MIME_JAVA,//
                        EncodingKey, ENCODING_PCM_SIGNED,
                        SignedKey, true),//
                new Format(MediaTypeKey, MediaType.AUDIO,//
                        MimeTypeKey, MIME_JAVA,//
                        EncodingKey, ENCODING_PCM_UNSIGNED,
                        SignedKey, false),//
                //
                // 8-bit unsigned has "raw " encoding regardless of endian
                new Format(MediaTypeKey, MediaType.AUDIO,//
                        EncodingKey, ENCODING_QUICKTIME_RAW_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        SignedKey, false, SampleSizeInBitsKey, 8),//
                //
                // 8-bit signed has "sowt" encoding for little endian
                new Format(MediaTypeKey, MediaType.AUDIO, //
                        EncodingKey, ENCODING_QUICKTIME_SOWT_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.LITTLE_ENDIAN,
                        SignedKey, true, SampleSizeInBitsKey, 8),//
                //
                // 8-bit signed has "twos" encoding for big endian
                new Format(MediaTypeKey, MediaType.AUDIO, //
                        EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.BIG_ENDIAN,
                        SignedKey, true, SampleSizeInBitsKey, 8),//

                // 16-bit, signed, little endian
                new Format(MediaTypeKey, MediaType.AUDIO,
                        EncodingKey, ENCODING_QUICKTIME_SOWT_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.LITTLE_ENDIAN,
                        SignedKey, true,
                        SampleSizeInBitsKey, 16),//
                //
                // 16-bit, signed, big endian
                new Format(MediaTypeKey, MediaType.AUDIO, //
                        EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.BIG_ENDIAN,
                        SignedKey, true,
                        SampleSizeInBitsKey, 16),//
                //
                new Format(MediaTypeKey, MediaType.AUDIO, //
                        EncodingKey, ENCODING_QUICKTIME_IN24_PCM,//
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.BIG_ENDIAN,
                        SignedKey, true, SampleSizeInBitsKey, 24),//
                //
                new Format(MediaTypeKey, MediaType.AUDIO,//
                        EncodingKey, ENCODING_QUICKTIME_IN32_PCM,
                        MimeTypeKey, MIME_QUICKTIME,//
                        ByteOrderKey, ByteOrder.BIG_ENDIAN,
                        SignedKey, true, SampleSizeInBitsKey, 32),//
        });
        name = "QuickTime PCM Codec";
    }
}
