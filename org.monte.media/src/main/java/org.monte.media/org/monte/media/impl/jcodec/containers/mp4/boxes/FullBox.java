package org.monte.media.impl.jcodec.containers.mp4.boxes;

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
public abstract class FullBox extends Box {

    public FullBox(Header atom) {
        super(atom);
    }

    protected byte version;
    protected int flags;

    public void parse(ByteBuffer input) {
        int vf = input.getInt();
        version = (byte) ((vf >> 24) & 0xff);
        flags = vf & 0xffffff;
    }

    protected void doWrite(ByteBuffer out) {
        out.putInt((version << 24) | (flags & 0xffffff));
    }

    public byte getVersion() {
        return version;
    }

    public int getFlags() {
        return flags;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}