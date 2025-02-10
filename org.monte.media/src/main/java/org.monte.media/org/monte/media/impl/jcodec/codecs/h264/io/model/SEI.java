package org.monte.media.impl.jcodec.codecs.h264.io.model;

import org.monte.media.impl.jcodec.common.io.BitWriter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.impl.jcodec.codecs.h264.io.write.CAVLCWriter.writeTrailingBits;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * <p>
 * Supplementary Enhanced Information entity of H264 bitstream
 * <p>
 * capable to serialize and deserialize with CAVLC bitstream
 *
 * @author The JCodec project
 */
public class SEI {

    public static class SEIMessage {
        public int payloadType;
        public int payloadSize;
        public byte[] payload;

        public SEIMessage(int payloadType2, int payloadSize2, byte[] payload2) {
            this.payload = payload2;
            this.payloadType = payloadType2;
            this.payloadSize = payloadSize2;
        }

    }

    public SEIMessage[] messages;

    public SEI(SEIMessage[] messages) {
        this.messages = messages;
    }

    public static SEI read(ByteBuffer is) {

        List<SEIMessage> messages = new ArrayList<SEIMessage>();
        SEIMessage msg;
        do {
            msg = sei_message(is);
            if (msg != null)
                messages.add(msg);
        } while (msg != null);

        return new SEI((SEIMessage[]) messages.toArray(new SEIMessage[]{}));
    }

    private static SEIMessage sei_message(ByteBuffer is) {
        int payloadType = 0;
        int b = 0;
        while (is.hasRemaining() && (b = (is.get() & 0xff)) == 0xff) {
            payloadType += 255;
        }
        if (!is.hasRemaining())
            return null;
        payloadType += b;
        int payloadSize = 0;
        while (is.hasRemaining() && (b = (is.get() & 0xff)) == 0xff) {
            payloadSize += 255;
        }
        if (!is.hasRemaining())
            return null;
        payloadSize += b;
        byte[] payload = sei_payload(payloadType, payloadSize, is);
        if (payload.length != payloadSize)
            return null;

        return new SEIMessage(payloadType, payloadSize, payload);

    }

    private static byte[] sei_payload(int payloadType, int payloadSize, ByteBuffer is) {
        byte[] res = new byte[payloadSize];
        is.get(res);
        return res;
    }

    public void write(ByteBuffer out) {
        BitWriter writer = new BitWriter(out);
        // TODO Auto-generated method stub

        writeTrailingBits(writer);
    }
}