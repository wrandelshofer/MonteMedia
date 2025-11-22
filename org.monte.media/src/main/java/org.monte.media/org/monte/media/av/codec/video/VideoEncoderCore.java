/*
 * @(#)VideoEncoderCore.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/// `VideoEncoderCore`.
///
/// @author Werner Randelshofer
public class VideoEncoderCore {

    protected byte[] byteBuf = new byte[4];

    public void writeInt24LE(ByteBuffer out, int v) throws IOException {
        out.put((byte) (v));
        out.put((byte) (v >>> 8));
        out.put((byte) (v >>> 16));
    }

    public void writeInts24LE(ByteBuffer out, int[] i, int off, int len) throws IOException {
        for (int j = off, n = off + len; j < n; j++) {
            int v = i[j];
            out.put((byte) (v));
            out.put((byte) (v >>> 8));
            out.put((byte) (v >>> 16));
        }
    }

    public void writeInts16LE(ByteBuffer out, short[] i, int off, int len) throws IOException {
        for (int j = off, n = off + len; j < n; j++) {
            int v = i[j];
            out.put((byte) (v));
            out.put((byte) (v >>> 8));
        }
    }


    public void writeInt24(ImageOutputStream out, int v) throws IOException {
        byteBuf[0] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[2] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    public void writeInt24LE(ImageOutputStream out, int v) throws IOException {
        byteBuf[2] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[0] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    public void writeInts24(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++, boff += 3) {
            int v = i[off + j];
            b[boff] = (byte) (v >>> 16);
            b[boff + 1] = (byte) (v >>> 8);
            b[boff + 2] = (byte) (v);
        }

        out.write(b, 0, len * 3);
    }

    public void writeInts24LE(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++, boff += 3) {
            int v = i[off + j];
            b[boff] = (byte) (v);
            b[boff + 1] = (byte) (v >>> 8);
            b[boff + 2] = (byte) (v >>> 16);
        }

        out.write(b, 0, len * 3);
    }

}
