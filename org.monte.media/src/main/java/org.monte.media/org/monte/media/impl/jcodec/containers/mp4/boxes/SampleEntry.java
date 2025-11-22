/*
 * @(#)SampleEntry.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.containers.mp4.boxes;

import java.nio.ByteBuffer;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
///
/// Creates MP4 file out of a set of samples
///
/// @author The JCodec project
public class SampleEntry extends NodeBox {

    protected short drefInd;

    public SampleEntry(Header header) {
        super(header);
    }

    public void parse(ByteBuffer input) {
        input.getInt();
        input.getShort();

        drefInd = input.getShort();
    }

    protected void parseExtensions(ByteBuffer input) {
        super.parse(input);
    }

    protected void doWrite(ByteBuffer out) {
        out.put(new byte[]{0, 0, 0, 0, 0, 0});
        out.putShort(drefInd); // data ref index
    }

    protected void writeExtensions(ByteBuffer out) {
        super.doWrite(out);
    }

    public short getDrefInd() {
        return drefInd;
    }

    public void setDrefInd(short ind) {
        this.drefInd = ind;
    }

    public void setMediaType(String mediaType) {
        header = new Header(mediaType);
    }

    @Override
    public int estimateSize() {
        return 8 + super.estimateSize();
    }
}
