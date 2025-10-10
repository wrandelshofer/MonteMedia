package org.monte.media.impl.jcodec.common;

import org.monte.media.impl.jcodec.common.model.Picture;

import java.io.IOException;
import java.nio.ByteBuffer;

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
abstract public class VideoDecoder {
    private byte[][] byteBuffer;

    /**
     * Decodes a video frame to an uncompressed picture in codec native
     * colorspace
     *
     * @param data Compressed frame data
     * @throws IOException
     */
    public abstract Picture decodeFrame(ByteBuffer data, byte[][] buffer);

    public abstract VideoCodecMeta getCodecMeta(ByteBuffer data);


    protected byte[][] getSameSizeBuffer(int[][] buffer) {
        if (byteBuffer == null || byteBuffer.length != buffer.length || byteBuffer[0].length != buffer[0].length)
            byteBuffer = ArrayUtil.create2D(buffer[0].length, buffer.length);
        return byteBuffer;
    }

    /**
     * Returns a downscaled version of this decoder
     *
     * @param ratio
     * @return
     */
    public VideoDecoder downscaled(int ratio) {
        if (ratio == 1)
            return this;
        return null;
    }
}