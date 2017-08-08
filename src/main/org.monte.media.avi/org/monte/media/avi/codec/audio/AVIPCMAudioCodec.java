/* @(#)AbstractPCMAudioCodec.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.avi.codec.audio;

import org.monte.media.av.codec.audio.AbstractPCMAudioCodec;
import org.monte.media.av.Format;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import static org.monte.media.av.codec.audio.AudioFormatKeys.*;

/**
 * {@code AbstractPCMAudioCodec} performs sign conversion, endian conversion and
 * quantization conversion of PCM audio data.
 * <p>
 * Does not perform sampling rate conversion or channel conversion.
 * <p>
 * FIXME Maybe create separate subclasses for AVI PCM and QuickTime PCM.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-10 Created.
 */
public class AVIPCMAudioCodec extends AbstractPCMAudioCodec {

    private final static HashSet<String> supportedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
               ENCODING_PCM_SIGNED, 
                ENCODING_PCM_UNSIGNED, ENCODING_AVI_PCM,}));

   public AVIPCMAudioCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_PCM_SIGNED,//
                            MimeTypeKey,MIME_JAVA,//
                            SignedKey,true),//
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_PCM_UNSIGNED,//
                            MimeTypeKey,MIME_JAVA,//
                            SignedKey,false),//
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_AVI_PCM,//
                            MimeTypeKey,MIME_AVI,//
                            SignedKey,false,SampleSizeInBitsKey,8),//
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_AVI_PCM,//
                            MimeTypeKey,MIME_AVI,//
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,16),//
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_AVI_PCM,//
                            MimeTypeKey,MIME_AVI,//
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,24),//
                    new Format(MediaTypeKey,MediaType.AUDIO,//
                            EncodingKey,ENCODING_AVI_PCM,//
                            MimeTypeKey,MIME_AVI,//
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,32),//
                });
        name="AVI PCM Codec";
    }

}
