/*
 * @(#)CodecMeta.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.common;

import java.nio.ByteBuffer;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
/// @author The JCodec project
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