/* @(#)ImageInputStreamImpl2.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.io;

import java.io.IOException;
import static java.nio.ByteOrder.BIG_ENDIAN;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * {@code ImageInputStreamImpl2} fixes bugs in ImageInputStreamImpl.
 * 
 * ImageInputStreamImpl uses read(byte[]) instead of readFully(byte[]) inside of
 * readShort. This results in corrupt data input if the underlying stream can
 * not fulfill the read operation in a single step.
 *
 * @author Werner Randelshofer
 * @version $Id: ImageInputStreamImpl2.java 364 2016-11-09 19:54:25Z werner $
 */
public abstract class ImageInputStreamImpl2 extends ImageInputStreamImpl {

    // Length of the buffer used for readFully(type[], int, int)
    private static final int BYTE_BUF_LENGTH = 8192;
    /**
     * Byte buffer used for readFully(type[], int, int). Note that this array is
     * also used for bulk reads in readShort(), readInt(), etc, so it should be
     * large enough to hold a primitive value (i.e. {@literal >}= 8 bytes). Also note that
     * this array is package protected, so that it can be used by
     * ImageOutputStreamImpl in a similar manner.
     */
    protected byte[] byteBuf = new byte[BYTE_BUF_LENGTH];

    @Override
    public short readShort() throws IOException {
        readFully(byteBuf, 0, 2);

        if (byteOrder == BIG_ENDIAN) {
            return (short) (((byteBuf[0] & 0xff) << 8) | ((byteBuf[1] & 0xff) << 0));
        } else {
            return (short) (((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0));
        }
    }

    public int readInt() throws IOException {
        readFully(byteBuf, 0, 4);

        if (byteOrder == BIG_ENDIAN) {
            return (((byteBuf[0] & 0xff) << 24) | ((byteBuf[1] & 0xff) << 16)
                  | ((byteBuf[2] & 0xff) << 8) | ((byteBuf[3] & 0xff) << 0));
        } else {
            return (((byteBuf[3] & 0xff) << 24) | ((byteBuf[2] & 0xff) << 16)
                  | ((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0));
        }
    }

}
