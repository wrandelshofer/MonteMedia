/*
 * @(#)MinimalDurationCodec.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av.codec.time;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

import java.util.Arrays;

/**
 * Buffers at least the specified minimal duration.
 * <p>
 * This codec processes byte array data, that is typically used with audio codecs.
 */
public class MinimalDurationCodec extends org.monte.media.av.AbstractCodec {
    private final Rational minimalDuration;
    private Rational currentDuration = Rational.ZERO;

    public MinimalDurationCodec(Rational minimalDuration) {
        super(new Format[]{
                        new Format(), //
                },
                new Format[]{
                        new Format(), //
                });
        name = "BufferinCodec";
        this.minimalDuration = minimalDuration;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (currentDuration.isZero()) {
            out.setMetaTo(in);
            if (in.isFlag(BufferFlag.DISCARD)) {
                return CODEC_OK;
            }
            out.setDataTo(in);
            currentDuration = out.getBufferDuration();
            return currentDuration.compareTo(minimalDuration) < 0 ? CODEC_OUTPUT_NOT_FILLED : CODEC_OK;
        } else {
            if (in.isFlag(BufferFlag.DISCARD)) {
                currentDuration = Rational.ZERO;
                return CODEC_OK;
            }
            byte[] inArray = (byte[]) in.data;
            byte[] outArray = (byte[]) out.data;
            if (out.offset > 0) {
                System.arraycopy(outArray, out.offset, outArray, 0, out.length);
                out.offset = 0;
            }
            if (outArray.length < out.length + in.length) {
                outArray = Arrays.copyOf(outArray, out.length + in.length);
            }
            System.arraycopy(inArray, in.offset, outArray, out.length, in.length);
            out.data = outArray;
            out.length += in.length;
            out.sampleCount += in.sampleCount;
            currentDuration = currentDuration.add(in.getBufferDuration());
            if (currentDuration.compareTo(minimalDuration) < 0) {
                return CODEC_OUTPUT_NOT_FILLED;
            }
        }
        currentDuration = Rational.ZERO;
        return CODEC_OK;
    }
}
