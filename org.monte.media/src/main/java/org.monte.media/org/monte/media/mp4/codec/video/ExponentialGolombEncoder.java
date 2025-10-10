/*
 * @(#)ExponentialGolombEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.mp4.codec.video;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * References:
 * <dl>
 *     <dt>Exponential-Golomb coding</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Exponential-Golomb_coding">wikipedia.org</a></dd>
 * </dl>
 * *
 */
public class ExponentialGolombEncoder {
    /**
     * To encode any non-negative integer x using the exp-Golomb code:
     * <ol>
     * <li>Write down x+1 in binary</li>
     * <li>Count the bits written, subtract one, and write that number of starting zero bits preceding the previous bit string.</li>
     * </ol>
     *
     * @param x   unsigned value
     * @param out output stream
     */
    public static void writeUnsigned(int x, ImageOutputStream out) throws IOException {
        long value = Math.abs(x) + 1;
        int numBits = 64 - Long.numberOfLeadingZeros(value);
        out.writeBits(value, numBits * 2 - 1);
    }

    /**
     * To encode a signed integer x using the exp-Golomb code:
     * <ol>
     * <li>If x ≤ 0 map it to an even integer −2x,<br>
     *     else if x &gt; 0 map it to an odd integer 2x−1. .</li>
     * <li>Count the bits written, subtract one, and write that number of starting zero bits preceding
     * the previous bit string.</li>
     * </ol>
     *
     * @param x   signed value
     * @param out output stream
     */
    public static void writeSigned(int x, ImageOutputStream out) throws IOException {
        long value = x <= 0 ? -2L * x : 2L * x - 1;
        int numBits = 64 - Long.numberOfLeadingZeros(value);
        out.writeBits(value, numBits * 2 - 1);
    }
}
