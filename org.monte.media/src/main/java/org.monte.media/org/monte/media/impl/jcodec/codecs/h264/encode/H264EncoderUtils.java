package org.monte.media.impl.jcodec.codecs.h264.encode;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Contains utility functions commonly used in H264 encoder
 * <p>
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author Stanislav Vitvitskyy
 */
public class H264EncoderUtils {
    public static int median(int a, boolean ar, int b, boolean br, int c, boolean cr, int d, boolean dr, boolean aAvb,
                             boolean bAvb, boolean cAvb, boolean dAvb) {
        ar &= aAvb;
        br &= bAvb;
        cr &= cAvb;

        if (!cAvb) {
            c = d;
            cr = dr;
            cAvb = dAvb;
        }

        if (aAvb && !bAvb && !cAvb) {
            b = c = a;
            bAvb = cAvb = aAvb;
        }

        a = aAvb ? a : 0;
        b = bAvb ? b : 0;
        c = cAvb ? c : 0;

        if (ar && !br && !cr)
            return a;
        else if (br && !ar && !cr)
            return b;
        else if (cr && !ar && !br)
            return c;

        return a + b + c - min(min(a, b), c) - max(max(a, b), c);
    }

    public static int mse(int[] orig, int[] enc, int w, int h) {
        int sum = 0;
        for (int i = 0, off = 0; i < h; i++) {
            for (int j = 0; j < w; j++, off++) {
                int diff = orig[off] - enc[off];
                sum += diff * diff;
            }
        }
        return sum / (w * h);
    }
}
