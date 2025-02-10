package org.monte.media.impl.jcodec.common;

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
public class CodecMeta {
    private String fourcc;
    private ByteBuffer codecPrivate;

    public CodecMeta(String fourcc, ByteBuffer codecPrivate) {
        this.fourcc = fourcc;
        this.codecPrivate = codecPrivate;
    }

    public String getFourcc() {
        return fourcc;
    }

    public ByteBuffer getCodecPrivate() {
        return codecPrivate;
    }
}