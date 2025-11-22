/*
 * @(#)NALUnit.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.codecs.h264.io.model;

import java.nio.ByteBuffer;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
///
/// Network abstraction layer (NAL) unit
///
/// @author The JCodec project
public class NALUnit {

    public NALUnitType type;
    public int nal_ref_idc;

    public NALUnit(NALUnitType type, int nal_ref_idc) {
        this.type = type;
        this.nal_ref_idc = nal_ref_idc;
    }

    public static NALUnit read(ByteBuffer _in) {
        int nalu = _in.get() & 0xff;
        int nal_ref_idc = (nalu >> 5) & 0x3;
        int nb = nalu & 0x1f;

        NALUnitType type = NALUnitType.fromValue(nb);
        return new NALUnit(type, nal_ref_idc);
    }

    public void write(ByteBuffer out) {
        int nalu = type.getValue() | (nal_ref_idc << 5);
        out.put((byte) nalu);
    }
}
