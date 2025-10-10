package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.MBType;
import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.model.Picture;

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
public class EncodedMB {
    public Picture pixels;
    public MBType type;
    public int qp;
    public int[] nc;
    public int[] mx;
    public int[] my;
    public int[] mr;
    public int mbX;
    public int mbY;

    public EncodedMB() {
        pixels = Picture.create(16, 16, ColorSpace.YUV420J);
        nc = new int[16];
        mx = new int[16];
        my = new int[16];
        mr = new int[16];
    }

    public Picture getPixels() {
        return pixels;
    }

    public MBType getType() {
        return type;
    }

    public void setType(MBType type) {
        this.type = type;
    }

    public int getQp() {
        return qp;
    }

    public void setQp(int qp) {
        this.qp = qp;
    }

    public int[] getNc() {
        return nc;
    }

    public int[] getMx() {
        return mx;
    }

    public int[] getMy() {
        return my;
    }

    public void setPos(int mbX, int mbY) {
        this.mbX = mbX;
        this.mbY = mbY;
    }

    public int[] getMr() {
        return mr;
    }
}
