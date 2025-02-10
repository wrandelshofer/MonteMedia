package org.monte.media.impl.jcodec.common;

import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.model.Picture;

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
 * <p>
 * A generic interface for the video encoder.
 *
 * @author The JCodec project
 */
public abstract class VideoEncoder {
    /**
     * The bytes of this video frame with associated metadata.
     *
     * @author The JCodec project
     */
    public static class EncodedFrame {
        private ByteBuffer data;
        private boolean keyFrame;

        public EncodedFrame(ByteBuffer data, boolean keyFrame) {
            this.data = data;
            this.keyFrame = keyFrame;
        }

        public ByteBuffer getData() {
            return data;
        }

        public boolean isKeyFrame() {
            return keyFrame;
        }
    }

    /**
     * Encode one video frame.
     *
     * @param pic    The video frame to be encoded. Must be in one of the encoder's
     *               native color spaces.
     * @param buffer The buffer to store the encoded frame into. Note, only the
     *               storage of this buffer is used, the position and limit are
     *               kept untouched. Instead the returned value contains a
     *               duplicate of this buffer with the position and limit set
     *               correctly to the boundaries of the encoded frame. This buffer
     *               must be large enough to hold the encoded frame. It is
     *               undefined what will happen if the buffer is not large enough.
     *               Most commonly some exception will be thrown.
     * @return The bytes of an encoded frame with additional metadata. The bytes
     * are located in the storage supplied by the buffer argument, no
     * allocation happens.
     */
    public abstract EncodedFrame encodeFrame(Picture pic, ByteBuffer buffer);

    /**
     * Native color spaces of this video encoder, i.e. the color space the video
     * encoder uses internally to represent color. For example the native color
     * space of an h.264 encoder is most commonly yuv420. This information is
     * used to find the correct color transform. The encoder may return null
     * which means that ANY color space will be taken. This is useful in some
     * cases for instance for the raw video encoder.
     *
     * @return The array of this encoder's native color spaces.
     */
    public abstract ColorSpace[] getSupportedColorSpaces();

    /**
     * Estimate the output buffer size that will likely be needed for the
     * current instance of encoder to encode a given frame. Note: expect a very
     * coarse estimate that reflects the settings the encoder has been created
     * with as well as the input frame size.
     *
     * @param frame A frame in question.
     * @return The number of bytes the encoded frame will likely take.
     */
    public abstract int estimateBufferSize(Picture frame);

    public abstract void finish();
}