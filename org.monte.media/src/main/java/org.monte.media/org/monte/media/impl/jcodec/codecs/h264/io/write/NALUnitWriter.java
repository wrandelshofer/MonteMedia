package org.monte.media.impl.jcodec.codecs.h264.io.write;

import org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnit;
import org.monte.media.impl.jcodec.common.io.NIOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

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
 * @author The JCodec project
 */
public class NALUnitWriter {
    private final WritableByteChannel to;
    private static ByteBuffer _MARKER = ByteBuffer.allocate(4);

    static {
        _MARKER.putInt(1);
        _MARKER.flip();
    }

    public NALUnitWriter(WritableByteChannel to) {
        this.to = to;
    }

    public void writeUnit(NALUnit nal, ByteBuffer data) throws IOException {
        ByteBuffer emprev = ByteBuffer.allocate(data.remaining() + 1024);
        NIOUtils.write(emprev, _MARKER);
        nal.write(emprev);
        emprev(emprev, data);
        emprev.flip();
        to.write(emprev);
    }

    private void emprev(ByteBuffer emprev, ByteBuffer data) {
        ByteBuffer dd = data.duplicate();
        int prev1 = 1, prev2 = 1;
        while (dd.hasRemaining()) {
            byte b = dd.get();
            if (prev1 == 0 && prev2 == 0 && ((b & 0x3) == b)) {
                prev2 = prev1;
                prev1 = 3;
                emprev.put((byte) 3);
            }

            prev2 = prev1;
            prev1 = b;
            emprev.put((byte) b);
        }
    }
}
