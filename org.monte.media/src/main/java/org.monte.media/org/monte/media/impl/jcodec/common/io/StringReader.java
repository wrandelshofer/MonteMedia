package org.monte.media.impl.jcodec.common.io;

import org.monte.media.impl.jcodec.platform.Platform;

import java.io.IOException;
import java.io.InputStream;

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
public abstract class StringReader {
    public static String readString(InputStream input, int len) throws IOException {
        byte[] bs = _sureRead(input, len);
        return bs == null ? null : Platform.stringFromBytes(bs);
    }

    public static byte[] _sureRead(InputStream input, int len) throws IOException {
        byte[] res = new byte[len];
        if (sureRead(input, res, res.length) == len)
            return res;
        return null;
    }

    public static int sureRead(InputStream input, byte[] buf, int len) throws IOException {
        int read = 0;
        while (read < len) {
            int tmp = input.read(buf, read, len - read);
            if (tmp == -1)
                break;
            read += tmp;
        }
        return read;
    }

    public static void sureSkip(InputStream is, long l) throws IOException {
        while (l > 0)
            l -= is.skip(l);
    }
}
