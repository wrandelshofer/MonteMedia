/*
 * @(#)AppleClosedCaptionCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.codec.text.AbstractTextCodec;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.quicktime.QTFFImageInputStream;
import org.monte.media.quicktime.codec.text.cta608.Cta608Parser;
import org.monte.media.quicktime.codec.text.cta708.Cta708Parser;

import java.io.IOException;
import java.nio.ByteOrder;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.*;
import static org.monte.media.av.codec.text.TextFormatKeys.ENCODING_CLOSED_CAPTION;
import static org.monte.media.av.codec.text.TextFormatKeys.ENCODING_STRING;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

/**
 * Implements the Apple Closed Caption codec.
 * <p>
 * Each sample consists of a {@code cdat} chunk.
 * <pre>
 * typedef struct {
 *     uint32 size;
 *     uint8[4] type;           // Must contain the ASCII characters "cdat".
 *     byte[size - 8] caption;  // ANSI/CTA-608-E S-2019 encoded String
 * } cdatChunk;
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Line 21 Data Services. ANSI/CTA-608-E S-2019. April 2008.</dt>
 *     <dd><a href="https://shop.cta.tech/products/line-21-data-services">ANSI-CTA-608-E-S-2019-Final.pdf</a></dd>
 * </dl>
 * <dl>
 *     <dt>ANSI/CTA Standard. Digital Television (DTV) Closed Captioning. CTA-708-E S-2023. August 2013.</dt>
 *     <dd><a href="https://shop.cta.tech/collections/standards/products/digital-television-dtv-closed-captioning">
 *         ANSI CTA-708-E S-2023 + Errata Letter and Replacement Pages FINAL.pdf</a></dd>
 * </dl>
 */
public class AppleClosedCaptionCodec extends AbstractTextCodec {
    public AppleClosedCaptionCodec() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.TEXT, MimeTypeKey, MIME_QUICKTIME,
                                EncodingKey, ENCODING_CLOSED_CAPTION, DataClassKey, byte[].class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.TEXT, MimeTypeKey, MIME_JAVA,
                                EncodingKey, ENCODING_STRING, DataClassKey, String.class), //
                });
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (ENCODING_STRING.equals(outputFormat.get(EncodingKey))) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    /**
     * Decodes a byte array to a String.
     */
    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        byte[] data = in.data instanceof byte[] ? (byte[]) in.data : null;
        if (data == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        try {
            ByteArrayImageInputStream iis = new ByteArrayImageInputStream(data, in.offset, in.length, ByteOrder.BIG_ENDIAN);
            QTFFImageInputStream qtin = new QTFFImageInputStream(iis);

            long size = qtin.readUnsignedInt();
            String type = qtin.readType();
            if (size > in.length) {
                throw new IOException("Chunk is bigger than buffer. chunk=" + type + ", size=" + size + ", buffer length " + in.length);
            }

            switch (type) {
                case "cdat": {// CTA-608
                    String text = new Cta608Parser().parseToStringWithOpCodes(iis);
                    out.data = text;
                    break;
                }
                case "ccdp": {// CTA-708
                    String text = new Cta708Parser().parseToStringWithOpCodes(iis);
                    out.data = text;
                    break;
                }
                default:
                    throw new IOException("Unsupported chunk type. chunk=" + type + ", size=" + size + ", buffer length " + in.length);
            }
        } catch (IOException e) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        return CODEC_OK;
    }


    /**
     * Encodes a String to a byte array.
     */
    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        String data = in.data instanceof String ? (String) in.data : null;
        if (data == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        return CODEC_OK;
    }
}
