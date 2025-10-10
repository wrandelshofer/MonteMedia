package org.monte.media.impl.jcodec.codecs.h264.io.model;

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
public final class SliceType {
    private final static SliceType _values[] = new SliceType[5];
    public final static SliceType P = new SliceType("P", 0);
    public final static SliceType B = new SliceType("B", 1);
    public final static SliceType I = new SliceType("I", 2);
    public final static SliceType SP = new SliceType("SP", 3);
    public final static SliceType SI = new SliceType("SI", 4);
    private String _name;
    private int _ordinal;

    private SliceType(String name, int ordinal) {
        this._name = name;
        this._ordinal = ordinal;
        _values[ordinal] = this;
    }

    public boolean isIntra() {
        return this == I || this == SI;
    }

    public boolean isInter() {
        return this != I && this != SI;
    }

    public static SliceType[] values() {
        return _values;
    }

    public int ordinal() {
        return _ordinal;
    }

    @Override
    public String toString() {
        return _name;
    }

    public String name() {
        return _name;
    }

    public static SliceType fromValue(int j) {
        return values()[j];
    }
}
