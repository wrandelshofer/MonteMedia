package org.monte.media.impl.jcodec.common.io;

import org.monte.media.impl.jcodec.common.IntArrayList;
import org.monte.media.impl.jcodec.common.IntIntMap;


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
 * <p>
 * prefix VLC reader builder
 *
 * @author The JCodec project
 */
public class VLCBuilder {

    public static VLCBuilder createVLCBuilder(int[] codes, int[] lens, int[] vals) {
        VLCBuilder b = new VLCBuilder();
        for (int i = 0; i < codes.length; i++) {
            b.setInt(codes[i], lens[i], vals[i]);
        }
        return b;
    }

    private IntIntMap forward;
    private IntIntMap inverse;
    private IntArrayList codes;
    private IntArrayList codesSizes;

    public VLCBuilder() {
        this.forward = new IntIntMap();
        this.inverse = new IntIntMap();
        this.codes = IntArrayList.createIntArrayList();
        this.codesSizes = IntArrayList.createIntArrayList();
    }

    public VLCBuilder set(int val, String code) {
        setInt(Integer.parseInt(code, 2), code.length(), val);

        return this;
    }

    public VLCBuilder setInt(int code, int len, int val) {
        codes.add(code << (32 - len));
        codesSizes.add(len);
        forward.put(val, codes.size() - 1);
        inverse.put(codes.size() - 1, val);

        return this;
    }

    public VLC getVLC() {
        final VLCBuilder self = this;
        return new VLC(codes.toArray(), codesSizes.toArray()) {
            public int readVLC(BitReader _in) {
                return self.inverse.get(super.readVLC(_in));
            }

            public int readVLC16(BitReader _in) {
                return self.inverse.get(super.readVLC16(_in));
            }

            public void writeVLC(BitWriter out, int code) {
                super.writeVLC(out, self.forward.get(code));
            }
        };
    }
}