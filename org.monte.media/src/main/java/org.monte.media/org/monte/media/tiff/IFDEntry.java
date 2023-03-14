/*
 * @(#)IFDEntry.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Represents a directory entry in a TIFF Image File Directory (IFD).
 * <p>
 * Each 12-byte IFD entry has the following format:
 * <ul>
 * <li>Bytes 0-1 The Tag that identifies the field.</li>
 * <li>Bytes 2-3 The field Type.</li>
 * <li>Bytes 4-7 The number of values, Count of the indicated Type.</li>
 * <li>Bytes 8-11 The Value Offset, the file offset (in bytes) of the Value for the
 * field. The Value is expected to begin on a word boundary; the corresponding
 * Value Offset will thus be an even number. This file offset may point anywhere
 * in the file, even after the image data.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public class IFDEntry {

    /**
     * The Tag number that identifies the field.
     */
    private int tagNumber;
    /**
     * The field Type.
     */
    private int typeNumber;
    /**
     * The number of values, Count of the indicated Type.
     */
    private long count;
    /**
     * The Value Offset stores the value or the offset of the value depending
     * on typeNumber and on count.
     */
    private long valueOffset;
    /**
     * The Entry Offset stores the location of the entry in the file.
     */
    private long entryOffset;
    /**
     * The IFD Offset stores the location of the IFD in the file.
     */
    private long ifdOffset;
    /* The entry data. */
    private Object data;

    public IFDEntry(int tagNumber, int typeNumber, long count, long valueOffset, long entryOffset) {
        this.tagNumber = tagNumber;
        this.typeNumber = typeNumber;
        this.count = count;
        this.valueOffset = valueOffset;
        this.entryOffset = entryOffset;
    }

    public long getCount() {
        return count;
    }

    public int getTagNumber() {
        return tagNumber;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    /**
     * The value offset may either contain the data or point to the data
     * depending on the type and the count.
     *
     * @return The value offset.
     */
    public long getValueOffset() {
        return valueOffset;
    }

    /**
     * The offset to the data.
     */
    public long getDataOffset() {
        return isDataInValueOffset() ? entryOffset + 8 : valueOffset + ifdOffset;
    }

    public void setIFDOffset(long newValue) {
        ifdOffset = newValue;
    }

    public long getEntryOffset() {
        return entryOffset;
    }

    public long getIFDOffset() {
        return ifdOffset;
    }

    public boolean isDataInValueOffset() {
        switch (IFDDataType.valueOf(typeNumber)) {
            case ASCII://8-bit byte that contains a 7-bit ASCII code; the last byte
                //must be NUL (binary zero).
                return false;
            case BYTE://8-bit unsigned integer.
                return count <= 4;
            case SHORT://16-bit (2-byte) unsigned integer.
                return count <= 2;
            case LONG://32-bit (4-byte) unsigned integer.
                return count <= 1;
            case RATIONAL://Two LONGs: the first represents the numerator of a fraction; the second, the denominator.
                return false;
            case SBYTE: //An 8-bit signed (twos-complement) integer.
                return count <= 4;
            case UNDEFINED://An 8-bit byte that may contain anything, depending on the definition of the field.
                return count <= 4;
            case SSHORT://A 16-bit (2-byte) signed (twos-complement) integer.
                return count <= 2;
            case SLONG://A 32-bit (4-byte) signed (twos-complement) integer.
                return count <= 1;
            case SRATIONAL://Two SLONG’s: the first represents the numerator of a fraction, the second the denominator.
                return false;
            case FLOAT://Single precision (4-byte) IEEE prettyFormat.
                return count <= 1;
            case DOUBLE:// Double precision (8-byte) IEEE prettyFormat.
                return false;
            default:
                return true;
        }
    }

    public long getLength() {
        switch (IFDDataType.valueOf(typeNumber)) {
            case ASCII://8-bit byte that contains a 7-bit ASCII code; the last byte
                return count;
            case BYTE://8-bit unsigned integer.
                return count;
            case SHORT://16-bit (2-byte) unsigned integer.
                return count * 2;
            case LONG://32-bit (4-byte) unsigned integer.
                return count * 4;
            case RATIONAL://Two LONGs: the first represents the numerator of a fraction; the second, the denominator.
                return count * 8;
            case SBYTE: //An 8-bit signed (twos-complement) integer.
                return count;
            case UNDEFINED://An 8-bit byte that may contain anything, depending on the definition of the field.
                return count;
            case SSHORT://A 16-bit (2-byte) signed (twos-complement) integer.
                return count * 2;
            case SLONG://A 32-bit (4-byte) signed (twos-complement) integer.
                return count * 4;
            case SRATIONAL://Two SLONG’s: the first represents the numerator of a fraction, the second the denominator.
                return count * 8;
            case FLOAT://Single precision (4-byte) IEEE prettyFormat.
                return count * 4;
            case DOUBLE:// Double precision (8-byte) IEEE prettyFormat.
                return count * 8;
            default:
                return 0;
        }
    }

    /**
     * Reads value data with ifdDataOffset=0
     */
    public Object readData(TIFFInputStream in) throws IOException {
        return readData(in, ifdOffset);
    }

    /**
     * Reads value data with the specified ifdDataOffset.
     */
    public Object readData(TIFFInputStream in, long ifdDataOffset) throws IOException {
        Object d = null;
        IFDDataType tt = IFDDataType.valueOf(typeNumber);
        if (tt != null) {
            switch (tt) {
                case ASCII://8-bit byte that contains a 7-bit ASCII code; the last byte
                    //must be NUL (binary zero).
                    if (count <= 4) {
                        StringBuilder buf = new StringBuilder();
                        int data = (int) valueOffset;
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            for (int i = 0; i < count - 1; i++) {
                                buf.append((char) (data & 0xff));
                                data >>= 8;
                            }
                        } else {
                            for (int i = 0; i < count - 1; i++) {
                                buf.append((char) (data >>> 24));
                                data <<= 8;
                            }
                        }
                        return buf.toString();
                    } else {
                        return in.readASCII(valueOffset + ifdDataOffset, count);
                    }
                case SHORT://16-bit (2-byte) unsigned integer.
                    if (count == 1) {
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            d = (int) (valueOffset & 0xffff);
                        } else {
                            d = (int) ((valueOffset >> 16) & 0xffff);
                        }
                    } else if (count == 2) {
                        d = new int[]{(int) (valueOffset & 0xffff), (int) ((valueOffset & 0xffff0000) >> 16)};
                    } else {
                        d = in.readSHORT(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case LONG://32-bit (4-byte) unsigned integer.
                    if (count == 1) {
                        d = valueOffset;
                    } else {
                        d = in.readLONG(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case RATIONAL://Two LONGs: the first represents the numerator of a fraction; the second, the denominator.
                    if (count == 1) {
                        d = in.readRATIONAL(valueOffset + ifdDataOffset);
                    } else {
                        d = in.readRATIONAL(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case BYTE://8-bit unsigned integer.
                    if (count == 1) {
                        d = (short) (valueOffset & 0xff);
                    } else if (count == 2) {
                        d = new short[]{(short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else if (count == 3) {
                        d = new short[]{(short) ((valueOffset & 0xff0000) >> 16), (short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else if (count == 4) {
                        d = new short[]{(short) ((valueOffset & 0xff000000) >> 24), (short) ((valueOffset & 0xff0000) >> 16), (short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else {
                        byte[] b = new byte[(int) count];
                        in.read(valueOffset + ifdDataOffset, b, 0, b.length);
                        short[] s = new short[(int) count];
                        for (int i = 0; i < b.length; i++) {
                            s[i] = (short) (b[i] & 0xff);
                        }
                        d = s;
                    }
                    break;
                case SBYTE: //An 8-bit signed (twos-complement) integer.
                case UNDEFINED://An 8-bit byte that may contain anything, depending on the definition of the field.
                    if (count == 1) {
                        d = (byte) valueOffset;
                    } else if (count == 2) {
                        d = new byte[]{(byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else if (count == 3) {
                        d = new byte[]{(byte) ((valueOffset & 0xff0000) >> 16), (byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else if (count == 4) {
                        d = new byte[]{(byte) ((valueOffset & 0xff000000) >> 24), (byte) ((valueOffset & 0xff0000) >> 16), (byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else {
                        byte[] b = new byte[(int) count];
                        in.read(valueOffset + ifdDataOffset, b, 0, b.length);
                        d = b;
                    }
                    break;
                case SSHORT://A 16-bit (2-byte) signed (twos-complement) integer.
                    if (count == 1) {
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            d = (short) (valueOffset & 0xffff);
                        } else {
                            d = (short) ((valueOffset >> 16) & 0xffff);
                        }
                    } else if (count == 2) {
                        d = new int[]{(short) (valueOffset & 0xffff), (short) ((valueOffset & 0xffff0000) >> 16)};
                    } else {
                        d = in.readSSHORT(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case SLONG://A 32-bit (4-byte) signed (twos-complement) integer.
                    if (count == 1) {
                        d = (int) valueOffset;
                    } else {
                        d = in.readSLONG(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case SRATIONAL://Two SLONG’s: the first represents the numerator of a fraction, the second the denominator.
                    if (count == 1) {
                        d = in.readSRATIONAL(valueOffset + ifdDataOffset);
                    } else {
                        d = in.readSRATIONAL(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case FLOAT://Single precision (4-byte) IEEE prettyFormat.
                case DOUBLE:// Double precision (8-byte) IEEE prettyFormat.
                default:
                    throw new IOException("Format FLOAT and DOUBLE " + typeNumber + " not implemented");
            }
        }
        return d;
    }

    public void loadData(TIFFInputStream in) throws IOException {
        data = readData(in);
    }

    public Object getData() {
        return data;
    }

    /**
     * FIXME Output is used by EXIFView
     */
    @Override
    public String toString() {
        return "IFD Entry: tag:0x" + Integer.toHexString(tagNumber) + " type:0x" + Integer.toHexString(typeNumber) + " count:0x" + Long.toHexString(count) + " valueOffset:0x" + Long.toHexString(valueOffset);
    }

    public String toString(Enum<?> tagName) {
        StringBuilder buf = new StringBuilder();
        buf.append(
                "Entry tag:" + tagName + "(" + Integer.toHexString(tagNumber) + "), type:" + IFDDataType.valueOf(typeNumber) + "(" + typeNumber + "), count:" + count + ", valueOffset:" + valueOffset);
        if (data != null) {
            buf.append(", data:");
            if (data instanceof byte[]) {
                byte[] d = (byte[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof short[]) {
                short[] d = (short[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof int[]) {
                int[] d = (int[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof long[]) {
                long[] d = (long[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof Object[]) {
                Object[] d = (Object[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else {
                buf.append(data);
            }
        }
        return buf.toString();
    }
}
